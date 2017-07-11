package io.anyline.adcubum;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import at.nineyards.anyline.camera.AnylineViewConfig;
import at.nineyards.anyline.camera.CameraController;
import at.nineyards.anyline.camera.CameraOpenListener;
import at.nineyards.anyline.models.AnylineImage;
import at.nineyards.anyline.modules.document.DocumentResult;
import at.nineyards.anyline.modules.document.DocumentResultListener;
import at.nineyards.anyline.modules.document.DocumentScanView;
import io.anyline.adcubum.util.FileUtil;
import io.anyline.adcubum.util.MiscUtil;
import io.anyline.adcubum.util.TransformationUtil;

/**
 * Example activity for the Anyline-Document-Detection-Module
 */
public class DocumentActivity extends AnylineBaseActivity implements CameraOpenListener {

    private static final String TAG = DocumentActivity.class.getSimpleName();


    public static Handler mManualButtonHandler = new Handler();
    private static final int CROP_DOCUMENT_REQUEST = 3;
    private static final String FULL_IMAGE_POSTFIX = "-Full.jpg";
    private static final String STATE_FILE_ID = "STATE_FILE_ID";
    private static final String STATE_CORNERS = "STATE_CORNERS";
    private static final String STATE_FULL_IMAGE_PATH = "STATE_FULL_IMAGE_PATH";
    private static final String SAVE_STATE_INTERMEDIATE_PAGES = "SAVE_STATE_INTERMEDIATE_PAGES";

    public static final String RESULT_TRANSFORM = "RESULT_TRANSFORM";
    public static final String SESSION_FOLDER_ORIGINALS = "ok";
    public static final String SESSION_FOLDER_ERROR = "error";
    public static final String TRANSFORMED_IMAGE_POSTFIX = "-Transformed.jpg";
    public static final String SESSION_FOLDER_TRANSFORMED = "transformed";
    public static final String SESSION_FOLDER_OCR_OPTIMIZED = "ocr";
    public static final String RESULT_PAGES = "RESULT_PAGES";
    public static Integer TOOLBARCOLOR = Color.BLACK;

    private DocumentScanView documentScanView;
    private ProgressDialog progressDialog;
    private TextView successfulScanCount;
    private FrameLayout errorMessageLayout;
    private ImageView imageViewFull;
    private TextView errorMessage;
    private ImageView triggerManualButton;
    private AVLoadingIndicatorView searchingButton;
    private GradientDrawable triggerManualshape;

    private ArrayList<PointF> corners;
    private File toTransformFullImageFile;
    private ArrayList<ScanPage> scanPagesForResult = new ArrayList<>();

    private ObjectAnimator errorMessageAnimator;
    private Toast notificationToast;

    private long lastErrorRecieved = 0;
    private boolean cropRequested = false;
    private int initialScanCount = 0;
    private JSONObject jsonConfig;
    private int manualScanButtonStartDuration;

    private android.os.Handler handler = new android.os.Handler();

    private String fileId;

    //Sensors
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mRotationVector;


    public int getAppResource(String name, String type) {
        return getResources().getIdentifier(name, type, getPackageName());
    }

    private Runnable errorMessageCleanup = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() > lastErrorRecieved + getResources().getIdentifier("delay_after_successful_scan", "integer", getPackageName())) {
                if (errorMessage == null || errorMessageAnimator == null) return;
                if (errorMessage.getAlpha() == 0f) {
                    errorMessage.setText("");
                } else {
                    errorMessageAnimator = ObjectAnimator.ofFloat(errorMessage, "alpha", errorMessage.getAlpha(), 0f);
                    errorMessageAnimator.setDuration(1000);
                    errorMessageAnimator.setInterpolator(new AccelerateInterpolator());
                    errorMessageAnimator.start();
                }
            }
            errorMessageCleanupDelayed(getResources().getIdentifier("delay_after_successful_scan", "integer", getPackageName()));

        }
    };

    private Runnable setManualScanButton = new Runnable() {
        @Override
        public void run() {
            searchingButton.hide();
            triggerManualButton.setVisibility(View.VISIBLE);
        }
    };


    private void errorMessageCleanupDelayed(int delayResourceId) {
        handler.postDelayed(errorMessageCleanup, getResources().getInteger(delayResourceId));
    }

    public void startManualButtonTimer() {
        mManualButtonHandler.postDelayed(setManualScanButton, manualScanButtonStartDuration);
    }

    public void stopManualButtonTimer() {
        mManualButtonHandler.removeCallbacks(setManualScanButton);
    }

    public void restartManualButtonTimer() {
        if (searchingButton.getVisibility() != View.VISIBLE) {
            searchingButton.smoothToShow();
            triggerManualButton.setVisibility(View.GONE);
        }
        mManualButtonHandler.removeCallbacks(setManualScanButton);
        mManualButtonHandler.postDelayed(setManualScanButton, manualScanButtonStartDuration);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResources().getIdentifier("activity_scan_document", "layout", getPackageName()));
        //Set the flag to keep the screen on (otherwise the screen may go dark during scanning)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        //restore intermediate Scans if Activity was killed during cropping
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVE_STATE_INTERMEDIATE_PAGES)) {
            this.scanPagesForResult = savedInstanceState.getParcelableArrayList(SAVE_STATE_INTERMEDIATE_PAGES);
        } else if (getIntent().getSerializableExtra(DocumentActivity.RESULT_PAGES) != null) {
            this.scanPagesForResult = (ArrayList<ScanPage>) getIntent().getSerializableExtra(DocumentActivity.RESULT_PAGES);
        }

        initialScanCount = scanPagesForResult.size();
        initActivityState(savedInstanceState);

        try {
            jsonConfig = new JSONObject(configJson);
            this.manualScanButtonStartDuration = Integer.parseInt(jsonConfig.getString("manualScanButtonStartDuration"));
        } catch (Exception e) {
            //JSONException or IllegalArgumentException is possible, return it to javascript
            this.manualScanButtonStartDuration = 3;
            finishWithError(Resources.getString(this, "error_invalid_json_data") + "\n" + e.getLocalizedMessage());
            return;
        }


        findViewsById();
        updateScanCount();
        startManualButtonTimer();

        setStyleOfSearchAndScanButton();
        searchingButton.smoothToShow();
        triggerManualButton.setVisibility(View.GONE);


        documentScanView.setCameraOpenListener(this);

        documentScanView.setConfig(new AnylineViewConfig(this, jsonConfig));

        // Optional: Set a ratio you want the documents to be restricted to. default is set to DIN_AX
        documentScanView.setDocumentRatios(DocumentScanView.DocumentRatio.DIN_AX_PORTRAIT.getRatio());

        // Optional: Set a maximum deviation for the ratio. 0.15 is the default
        documentScanView.setMaxDocumentRatioDeviation(0.15);


        // initialize Anyline with the license key and a Listener that is called if a result is found
        documentScanView.initAnyline(licenseKey, new DocumentResultListener() {
            @Override
            public void onResult(DocumentResult documentResult) {

                Log.d(TAG, "onResult");
                stopScanning(true);
                AnylineImage fullFrame = documentResult.getFullImage();
                AnylineImage transformedImage = documentResult.getResult();
                List<PointF> corners = documentResult.getOutline();

                File outFileFull = saveFullImageToDisk(fullFrame);
                File outFileTransformed = saveTransformedImageToDisk(transformedImage);

                // automatically taken picture, corners should be good -> do not show the crop view
                closeProgressDialog();

                // release the images
                fullFrame.release(); // important!
                transformedImage.release(); // important!
                Log.d(TAG, "onResult corners: " + corners);
                if (outFileFull != null && outFileTransformed != null) {
                    saveResultForReturn(outFileFull.getAbsolutePath(), outFileTransformed.getAbsolutePath(), corners);
                    updateScanCount();
                } else {
                    showErrorMessageFor(DocumentScanView.DocumentError.DOCUMENT_OUTLINE_NOT_FOUND, true);
                }

                JSONObject jsonResult = new JSONObject();
                try {
                    jsonResult.put("imagePath", outFileTransformed.getAbsolutePath());
                    jsonResult.put("outline", jsonForOutline(documentResult.getOutline()));
                    jsonResult.put("confidence", documentResult.getConfidence());

                } catch (Exception jsonException) {
                    //should not be possible
                    Log.e(TAG, "Error while putting image path to json.", jsonException);
                }

                try{

                    if(Boolean.parseBoolean(jsonConfig.getJSONObject("multipage").getString("multipageEnabled"))){
                        startScanningDelayed(getResources().getIdentifier("delay_after_successful_scan", "integer", getPackageName()));
                    } else {
                        finishAndReturnResult();
                    }
                }catch (JSONException e){
                     e.printStackTrace();
                }

            }


            @Override
            public void onPictureTransformError(DocumentScanView.DocumentError error) {
                Log.d(TAG, error.toString());
            }

            @Override
            public void onPictureTransformed(AnylineImage image) {
                Log.d(TAG, "onPictureTransformed");
            }

            @Override
            public void onPictureCornersDetected(AnylineImage fullFrame, List<PointF> corners) {
                Log.d(TAG, "onPictureCornersDetected " + MiscUtil.toPrettyString(corners));
                // gets called after manual trigger button was pressed
                // if corners could not be detected -> the corners are the outline of the image

                File fullImageFile = saveFullImageToDisk(fullFrame);
                Log.d(TAG, "imagePath " + fullImageFile.getAbsolutePath());
                startCropViewFor(fullImageFile.getAbsolutePath(), corners);
                //fullFrame.release();
            }


            @Override
            public void onPreviewProcessingSuccess(AnylineImage anylineImage) {
                // this is called after the preview of the document is completed, and a full picture will be
                // processed automatically
            }

            @Override
            public void onPreviewProcessingFailure(DocumentScanView.DocumentError documentError) {
                // this is called on any error while processing the document image
                // Note: this is called every time an error occurs in a run, so that might be quite often
                // An error message should only be presented to the user after some time

                showErrorMessageFor(documentError);
            }

            @Override
            public void onPictureProcessingFailure(DocumentScanView.DocumentError documentError) {

                showErrorMessageFor(documentError, true);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

            }

            @Override
            public boolean onDocumentOutlineDetected(List<PointF> list, boolean documentShapeAndBrightnessValid) {
                // is called when the outline of the document is detected. return true if the outline is consumed by
                // the implementation here, false if the outline should be drawn by the DocumentScanView
                List<PointF> lastOutline = list; // saving the outline for the animations
                return false;
            }

            @Override
            public void onTakePictureSuccess() {
                // this is called after the image has been captured from the camera and is about to be processed
                progressDialog = ProgressDialog.show(DocumentActivity.this, getString(getResources().getIdentifier("document_processing_picture_header", "string", getPackageName())),
                        getString(getResources().getIdentifier("document_processing_picture", "string", getPackageName())),
                        true);

                if (errorMessageAnimator != null && errorMessageAnimator.isRunning()) {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            errorMessageAnimator.cancel();
                            errorMessageLayout.setVisibility(View.GONE);
                        }
                    });

                }
            }

            @Override
            public void onTakePictureError(Throwable throwable) {
                // This is called if the image could not be captured from the camera (most probably because of an
                // OutOfMemoryError)
                throw new RuntimeException(throwable);
            }

        });

    }

    private void saveResultForReturn(String filenameOriginalImage, String filenameTransformedImage, List<PointF> corners) {
        Log.d(TAG, "saveResultForReturn:" + filenameTransformedImage);
        ScanPage scanPage = new ScanPage(filenameOriginalImage, filenameTransformedImage, (ArrayList<PointF>) corners);
        scanPagesForResult.add(scanPage);
        fileId += 1;
    }

    private void updateScanCount() {
        int successfulScanCounter = this.scanPagesForResult.size();
        Log.d(TAG, "successfulScanCounter:" + successfulScanCounter +
                " initialScanCount:" + initialScanCount +
                " mScanPagesForResult:" + this.scanPagesForResult.size());
        if (successfulScanCounter < 1) {
            successfulScanCount.setVisibility(View.INVISIBLE);
        } else if (successfulScanCounter == 1) {
            successfulScanCount.setVisibility(View.VISIBLE);
            successfulScanCount.setText(successfulScanCounter + " " + getResources().getString(getResources().getIdentifier("page", "string", getPackageName())) + " >");
        } else {
            successfulScanCount.setVisibility(View.VISIBLE);
            successfulScanCount.setText(successfulScanCounter + " " + getResources().getString(getResources().getIdentifier("pages", "string", getPackageName())) + " >");
        }

    }

    private void initActivityState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            fileId = savedInstanceState.getString(STATE_FILE_ID);
            corners = savedInstanceState.getParcelableArrayList(STATE_CORNERS);
            String fullImagePath = savedInstanceState.getString(STATE_FULL_IMAGE_PATH);
            if (fullImagePath != null) toTransformFullImageFile = new File(fullImagePath);

        } else {
            fileId = "" + System.currentTimeMillis();
        }
    }

    private File saveTransformedImageToDisk(AnylineImage transformedImage) {
        File outDirUi = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), SESSION_FOLDER_TRANSFORMED);
        String filenameUi = "" + fileId + TRANSFORMED_IMAGE_POSTFIX;
        return FileUtil.saveImage(outDirUi, filenameUi, transformedImage);
    }

    private void setStyleOfSearchAndScanButton() {
        triggerManualshape = new GradientDrawable();
        triggerManualshape.setShape(GradientDrawable.OVAL);

        try {
            triggerManualshape.setColor(Color.parseColor("#" + jsonConfig.getString("manualScanButtonColor")));
        } catch (JSONException e) {
            triggerManualshape.setColor(Color.parseColor("#004583"));
            e.printStackTrace();
        }
        triggerManualButton.setBackgroundDrawable(triggerManualshape);
        searchingButton.setBackgroundDrawable(triggerManualshape);

    }


    private void findViewsById() {

        imageViewFull = (ImageView) findViewById(getResources().getIdentifier("full_image", "id", getPackageName()));
        documentScanView = (DocumentScanView) findViewById(getResources().getIdentifier("document_scan_view", "id", getPackageName()));
        errorMessageLayout = (FrameLayout) findViewById(getResources().getIdentifier("error_message_layout", "id", getPackageName()));
        errorMessage = (TextView) findViewById(getResources().getIdentifier("error_message", "id", getPackageName()));
        View toolbar = (Toolbar) findViewById(getResources().getIdentifier("scan_toolbar", "id", getPackageName()));

        triggerManualButton = (ImageView) findViewById(getResources().getIdentifier("manual_trigger_button", "id", getPackageName()));
        triggerManualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "triggerPictureCornerDetection");
                stopScanning(true);
                documentScanView.triggerPictureCornerDetection(); // triggers corner detection -> callback on onPictureCornersDetected
                showProgressDialog();
            }
        });


        searchingButton = (AVLoadingIndicatorView) findViewById(getResources().getIdentifier("searching_button", "id", getPackageName()));
        searchingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "triggerPictureCornerDetection");
                stopScanning(true);
                documentScanView.triggerPictureCornerDetection(); // triggers corner detection -> callback on onPictureCornersDetected
                showProgressDialog();
            }
        });


        try {
            TOOLBARCOLOR = Color.parseColor("#" + jsonConfig.getJSONObject("multipage").getString("multipageTintColor"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        toolbar.setBackgroundColor(TOOLBARCOLOR);

        successfulScanCount = (TextView) findViewById(getResources().getIdentifier("successful_scan_count", "id", getPackageName()));
        successfulScanCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAndReturnResult();
            }
        });
        ImageView cancelScanAction = (ImageView) findViewById(getResources().getIdentifier("cancel_scan_action", "id", getPackageName()));
        cancelScanAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(documentScanView.getContext());

                builder.setTitle(getResources().getString(getResources().getIdentifier("confirm", "string", getPackageName())));
                builder.setMessage(getResources().getString(getResources().getIdentifier("force_cancel_app", "string", getPackageName())));

                builder.setPositiveButton(getResources().getString(getResources().getIdentifier("yes", "string", getPackageName())), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        finish();
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(getResources().getString(getResources().getIdentifier("no", "string", getPackageName())), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do Nothing
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void startCropViewFor(String fullFramePath, List<PointF> corners) {
        cropRequested = true;
        Log.d(TAG, "startCropViewFor " + fullFramePath + " Corners: " + MiscUtil.toPrettyString(corners));
        Log.d(TAG, "startCropViewFor " + fullFramePath + " Corners: " + MiscUtil.toPrettyString(corners));
        Intent intent = new Intent(DocumentActivity.this, CropDocumentActivity.class);
        intent.putExtra(CropDocumentActivity.EXTRA_FULL_IMAGE_PATH, fullFramePath);
        intent.putParcelableArrayListExtra(CropDocumentActivity.EXTRA_CORNERS, (ArrayList<PointF>) corners);
        startActivityForResult(intent, CROP_DOCUMENT_REQUEST);
    }

    private File saveFullImageToDisk(AnylineImage fullImage) {
        File outDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), SESSION_FOLDER_ORIGINALS);
        if (!outDir.exists()) {
            outDir.mkdir();
        }
        String filename = "" + fileId + FULL_IMAGE_POSTFIX;
        return FileUtil.saveImage(outDir, filename, fullImage);
    }


    private void finishAndReturnResult() {
        Intent data = new Intent();
        data.putExtra(RESULT_PAGES, scanPagesForResult);
        this.setResult(AnylinePlugin.RESULT_SWITCH, data);
        handler.removeCallbacks(this.errorMessageCleanup);
        handler.removeCallbacks(this.delayedStartScanning);

        this.finish();
    }


    private boolean showProgressDialog() {
        Log.d(TAG, "showProgressDialog");
        if (progressDialog != null) {
            Log.w(TAG, "dialog already shown");
            return false;
        }
        progressDialog = ProgressDialog.show(
                DocumentActivity.this,
                getString(getResources().getIdentifier("document_processing_picture_header", "string", getPackageName())),
                getString(getResources().getIdentifier("document_processing_picture", "string", getPackageName())),
                true);

        if (notificationToast != null) {
            notificationToast.cancel();
        }
        if (errorMessageAnimator != null && errorMessageAnimator.isRunning()) {
            errorMessageAnimator.cancel();
            errorMessageLayout.setVisibility(View.GONE);
        }
        return true;
    }

    private void showErrorMessageFor(DocumentScanView.DocumentError documentError) {
        showErrorMessageFor(documentError, false);
    }

    private void showErrorMessageFor(DocumentScanView.DocumentError documentError, boolean highlight) {
        String text = getString(getResources().getIdentifier("document_picture_error", "string", getPackageName()));
        switch (documentError) {
            case DOCUMENT_NOT_SHARP:
                text += getString(getResources().getIdentifier("document_error_not_sharp", "string", getPackageName()));
                break;
            case DOCUMENT_SKEW_TOO_HIGH:
                text += getString(getResources().getIdentifier("document_error_skew_too_high", "string", getPackageName()));
                break;
            case DOCUMENT_OUTLINE_NOT_FOUND:
                //text += getString(getResources().getIdentifier("document_error_outline_not_found", "string", getPackageName()));
                return; // exit and show no error message for now!
            case IMAGE_TOO_DARK:
                text += getString(getResources().getIdentifier("document_error_too_dark", "string", getPackageName()));
                break;
            case SHAKE_DETECTED:
                text += getString(getResources().getIdentifier("document_error_shake", "string", getPackageName()));
                restartManualButtonTimer();
                break;
            case DOCUMENT_BOUNDS_OUTSIDE_OF_TOLERANCE:
                text += getString(getResources().getIdentifier("document_error_closer", "string", getPackageName()));
                break;
            case DOCUMENT_RATIO_OUTSIDE_OF_TOLERANCE:
                text += getString(getResources().getIdentifier("document_error_format", "string", getPackageName()));
                break;
            case UNKNOWN:
                break;
            default:
                text += getString(getResources().getIdentifier("document_error_unknown", "string", getPackageName()));
                return; // exit and show no error message for now!
        }

        if (highlight) {
            showHighlightErrorMessageUiAnimated(text);
        } else {
            showErrorMessageUiAnimated(text);
        }
    }


    private void closeProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = null;
    }

    private void showErrorMessageUiAnimated(String message) {
        if (lastErrorRecieved == 0) {
            // the cleanup takes care of removing the message after some time if the error did not show up again
            handler.post(errorMessageCleanup);
        }
        lastErrorRecieved = System.currentTimeMillis();
        if (errorMessageAnimator != null && (errorMessageAnimator.isRunning() || errorMessage.getText().equals(message))) {
            return;
        }

        errorMessageLayout.setVisibility(View.VISIBLE);
        errorMessage.setBackgroundColor(ContextCompat.getColor(this, getResources().getIdentifier("anyline_blue_darker", "color", getPackageName())));
        errorMessage.setAlpha(0f);
        errorMessage.setText(message);
        errorMessageAnimator = ObjectAnimator.ofFloat(errorMessage, "alpha", 0f, 1f);
        errorMessageAnimator.setDuration(getResources().getInteger(getResources().getIdentifier("error_message_delay", "integer", getPackageName())));
        errorMessageAnimator.setInterpolator(new DecelerateInterpolator());
        errorMessageAnimator.start();
    }

    private void showHighlightErrorMessageUiAnimated(String message) {
        lastErrorRecieved = System.currentTimeMillis();
        errorMessageLayout.setVisibility(View.VISIBLE);
        errorMessage.setBackgroundColor(ContextCompat.getColor(this, getResources().getIdentifier("anyline_red", "color", getPackageName())));
        errorMessage.setAlpha(0f);
        errorMessage.setText(message);

        if (errorMessageAnimator != null && errorMessageAnimator.isRunning()) {
            errorMessageAnimator.cancel();
        }

        errorMessageAnimator = ObjectAnimator.ofFloat(errorMessage, "alpha", 0f, 1f);
        errorMessageAnimator.setDuration(getResources().getInteger(getResources().getIdentifier("error_message_delay", "integer", getPackageName())));
        errorMessageAnimator.setInterpolator(new DecelerateInterpolator());
        errorMessageAnimator.setRepeatMode(ValueAnimator.REVERSE);
        errorMessageAnimator.setRepeatCount(1);
        errorMessageAnimator.start();
    }

    private void startScanning() {
        Log.d(TAG, "startScanning");
        triggerManualButton.setEnabled(true);
        searchingButton.setEnabled(true);
        documentScanView.startScanning();
    }


    private void stopScanning(boolean disableManualScanButton) {
        Log.d(TAG, "stopScanning");
        if (disableManualScanButton) {
            triggerManualButton.setEnabled(false);
            searchingButton.setEnabled(false);
        }
        documentScanView.cancelScanning();
    }

    private Runnable delayedStartScanning = new Runnable() {
        @Override
        public void run() {
            startScanning();
        }
    };


    private void showToast(String text) {
        try {
            notificationToast.setText(text);
        } catch (Exception e) {
            notificationToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        }
        notificationToast.show();
    }

    private void startScanningDelayed(int delayResourceId) {
        handler.postDelayed(delayedStartScanning, getResources().getInteger(delayResourceId));
    }

    @Override
    protected void onResume() {
        super.onResume();
        closeProgressDialog();
        restartManualButtonTimer();
        handler.post(errorMessageCleanup);
        if (!cropRequested) {
            startScanning();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stop the scanning
        stopScanning(true);
        stopManualButtonTimer();

        //release the camera (must be called in onPause, because there are situations where
        // it cannot be auto-detected that the camera should be released)
        documentScanView.releaseCameraInBackground();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeProgressDialog();
        stopManualButtonTimer();
        handler.removeCallbacks(errorMessageCleanup);
    }

    @Override
    public void onCameraOpened(CameraController cameraController, int width, int height) {
        //the camera is opened async and this is called when the opening is finished
        Log.d(TAG, "Camera opened successfully. Frame resolution " + width + " x " + height);
    }

    @Override
    public void onCameraError(Exception e) {
        //This is called if the camera could not be opened.
        // (e.g. If there is no camera or the permission is denied)
        // This is useful to present an alternative way to enter the required data if no camera exists.
        throw new RuntimeException(e);
    }

    private void transformFullImage(final File fullImageFile, final ArrayList<PointF> corners) {

        showProgressDialog();
        stopScanning(true);
        toTransformFullImageFile = fullImageFile;
        if (!toTransformFullImageFile.exists())
            Log.w(TAG, "mToTransformFullImageFile file does not exist!");
        this.corners = corners;
        Log.d(TAG, "fullImageFile Path:" + fullImageFile.getAbsolutePath() + " corners: " + corners.toString());

        File targetDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), DocumentActivity.SESSION_FOLDER_TRANSFORMED);
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        String filename = "" + System.currentTimeMillis() + "" + DocumentActivity.TRANSFORMED_IMAGE_POSTFIX;
        final File outFile = new File(targetDir, filename);

        Log.d(TAG, "TransformationUtil.doTransformation");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                TransformationUtil.doTransformation(corners, fullImageFile.getAbsolutePath(), outFile.getAbsolutePath());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                onTransformDone(outFile);
            }
        }.execute();

    }


    private void onTransformDone(File outFile) {

        if (outFile != null) {
            Log.d(TAG, "transformed picture saved:" + outFile.getAbsolutePath());
            String fullImagePath = toTransformFullImageFile.getAbsolutePath();
            saveResultForReturn(fullImagePath, outFile.getAbsolutePath(), corners);
            finishAndReturnResult();
        } else {
            Log.d(TAG, "transformed picture not saved " + outFile);
        }
        cropRequested = false;
//        startScanningDelayed(getResources().getIdentifier("delay_after_successful_scan", "integer", getPackageName()));

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        closeProgressDialog();
        if (requestCode == CROP_DOCUMENT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Log.d(TAG, "onActivityResult CROP_DOCUMENT_REQUEST");
                    ArrayList<PointF> corners = extras.getParcelableArrayList(CropDocumentActivity.RESULT_CORNERS);
                    String fullImagePath = extras.getString(CropDocumentActivity.RESULT_FULL_IMAGE_PATH);
                    transformFullImage(new File(fullImagePath), corners);
                } else {
                    throw new RuntimeException("cropping failed");
                }
            } else {
                Log.d(TAG, "Cropping canceled by user");
                closeProgressDialog();
                cancelDialogAndAnimation();
                startScanning();
            }
        } else {
            throw new RuntimeException("Unknown RequestCode");
        }

    }

    private void cancelDialogAndAnimation() {
        // cancel the animation on error
        imageViewFull.clearAnimation();
        imageViewFull.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_STATE_INTERMEDIATE_PAGES, this.scanPagesForResult);
    }
}