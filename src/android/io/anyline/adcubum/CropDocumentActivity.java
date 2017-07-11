package io.anyline.adcubum;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.WindowManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class CropDocumentActivity extends AppCompatActivity {

    private final String TAG = CropDocumentActivity.class.getSimpleName();

    public static final String EXTRA_FULL_IMAGE_PATH = "EXTRA_FULL_IMAGE_PATH";
    public static final String EXTRA_CORNERS = "EXTRA_CORNERS";
    public static final String RESULT_FULL_IMAGE_PATH = "RESULT_FULL_IMAGE_PATH";
    public static final String RESULT_CORNERS = "RESULT_CORNERS";
    private static final String SAVE_STATE_INTERMEDIATE_CORNERS = "SAVE_STATE_INTERMEDIATE_CORNERS";

    private ImageView document;

    private MagnifyImageView magnifyImageView;

    private TetragonView cropView;

    private String filePath;

    private float documentScaleFactor;
    private TextView successfulScanCount;
    private ImageView cancelScanAction;
    private View toolbar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(getResources().getIdentifier("activity_crop", "layout", getPackageName()));
        document = (ImageView) findViewById(getResources().getIdentifier("crop_activity_image", "id", getPackageName()));
        cropView = (TetragonView) findViewById(getResources().getIdentifier("tetragon_view", "id", getPackageName()));
        magnifyImageView = (MagnifyImageView) findViewById(getResources().getIdentifier("crop_activity_magnify", "id", getPackageName()));
        toolbar = findViewById(getResources().getIdentifier("standart_toolbar", "id", getPackageName()));

        final ArrayList<PointF> corners = getIntent().getExtras().getParcelableArrayList(CropDocumentActivity.EXTRA_CORNERS);
        filePath = getIntent().getExtras().getString(CropDocumentActivity.EXTRA_FULL_IMAGE_PATH);

        toolbar.setBackgroundColor(DocumentActivity.TOOLBARCOLOR);

        ViewTreeObserver vto = document.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                // Remove after the first run so it doesn't fire forever
                document.getViewTreeObserver().removeOnPreDrawListener(this);

                // Get height in view
                int documentViewHeight = document.getMeasuredHeight();
                int documentViewWidth = document.getMeasuredWidth();

                // Get Real Height
                int[] originalBounds = getOriginalHeight(filePath);
                int originalHeight = originalBounds[0];

                // Get scale factor
                documentScaleFactor = (((float) documentViewHeight) / ((float) originalHeight));
                float differenceViewToBitmapWidth = documentViewWidth - ((float) originalBounds[1]) * documentScaleFactor;
                CropDocumentActivity.this.cropView.setImageOffset(documentViewHeight, documentViewWidth - Math.round(differenceViewToBitmapWidth));
                magnifyImageView.setScaleFactor(documentScaleFactor);

                if (savedInstanceState != null) {
                    ArrayList<PointF> intermediate = savedInstanceState.getParcelableArrayList(SAVE_STATE_INTERMEDIATE_CORNERS);
                    if (!intermediate.isEmpty()) {
                        cropView.setCorners(intermediate);
                        return true;
                    }
                }
                indentCorners(corners, originalBounds);
                // scale Corners
                ArrayList<PointF> scaledCorners = new ArrayList<PointF>();
                for (PointF corner : corners) {
                    //Log.d(TAG, corner.toString());
                    scaledCorners.add(new PointF(corner.x * documentScaleFactor, corner.y * documentScaleFactor));
                }

                cropView.setCorners(scaledCorners);


                return true;
            }
        });
        cropView.setMagnifyView(magnifyImageView);

        document.setImageURI(Uri.parse(filePath));

        magnifyImageView.setMagnifyTarget(document);
        magnifyImageView.setVisibility(View.INVISIBLE);


        successfulScanCount = (TextView) findViewById(getResources().getIdentifier("finish_activity", "id", getPackageName()));
        successfulScanCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAndProcessResult();
            }
        });
        cancelScanAction = (ImageView) findViewById(getResources().getIdentifier("cancel_scan_action_overview", "id", getPackageName()));
        cancelScanAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }


    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    public boolean finishAndProcessResult() {


        ArrayList<PointF> currentCorners = this.cropView.getCorners();
        ArrayList<PointF> scaledCorners = scaleCorners(currentCorners, documentScaleFactor);
        Intent data = new Intent();
        data.putParcelableArrayListExtra(RESULT_CORNERS, scaledCorners);
        data.putExtra(RESULT_FULL_IMAGE_PATH, filePath);
        this.setResult(RESULT_OK, data);
        finish();
        return true;

    }

    private ArrayList<PointF> scaleCorners(ArrayList<PointF> corners, float scaleFactor) {
        //Log.d(TAG, "scaleCorners");
        for (PointF point : corners) {
            point.x /= scaleFactor;
            point.y /= scaleFactor;
            //Log.d(TAG, "scaleCorners: " + point.toString());
        }
        return corners;
    }

    private int[] getOriginalHeight(String path) {
        //Log.d(TAG, path);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream input = null;
        try {

            BitmapFactory.decodeFile(path, options);

//        } catch (FileNotFoundException e) {
//            Log.d(TAG,e.getMessage());
//            e.printStackTrace();
        } finally {
            if (input != null)
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return new int[]{options.outHeight, options.outWidth};
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_STATE_INTERMEDIATE_CORNERS, cropView.getCorners());
    }

    private void indentCorners(ArrayList<PointF> originalPoints, int originalBounds[]) {

        //if any point is not on the border -> borders provided
        for (PointF point : originalPoints) {
            if (point.x != 0 && point.x != originalBounds[1])
                return;
            if (point.y != 0 && point.y != originalBounds[0])
                return;
        }
        //now we indent
        float indentation = (originalBounds[0] > originalBounds[1]) ? Math.round(originalBounds[1] / 12) : Math.round(originalBounds[0] / 12);
        for (PointF point : originalPoints) {
            if (point.x == 0)
                point.x = indentation;
            if (point.x == originalBounds[1])
                point.x = point.x - indentation;

            if (point.y == 0)
                point.y = indentation;
            if (point.y == originalBounds[0])
                point.y = point.y - indentation;
        }
    }
}
