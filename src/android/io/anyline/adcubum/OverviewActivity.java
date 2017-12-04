package io.anyline.adcubum;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.WindowManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.anyline.adcubum.DocumentActivity;
import io.anyline.adcubum.PagerContainer;
import io.anyline.adcubum.ScanPage;
import io.anyline.adcubum.ScanPagePagerAdapter;
import io.anyline.adcubum.util.FileUtil;
import io.anyline.adcubum.util.PrefsUtil;
import io.anyline.adcubum.util.TransformationUtil;

public class OverviewActivity extends AppCompatActivity {


    private static final String TAG = OverviewActivity.class.getSimpleName();
    public static final String RESULT_DOCUMENT_EXTRA = "RESULT_DOCUMENT_EXTRA";
    private static final int MY_PERMISSIONS_REQUEST = 17;
    public static final String EXTRA_CLEAR_SESSION = "EXTRA_CLEAR_SESSION";
    private static final String SAVED_SCANPAGES = "SAVED_SCANPAGES";
    private static final String EDITING_SCANPAGE_INDEX = "EDITING_SCANPAGE_INDEX";

    TextView mPageNumber;
    ViewPager mViewPager;
    PagerContainer mContainer;
    TabLayout mTabs;
    TextView mNoItemView;
    ProgressBar mProgress;

    private ScanPagePagerAdapter mAdapter;
    private static final int CROP_DOCUMENT_REQUEST = 3;
    private static final int TRANSFORM_DOCUMENT_REQUEST = 4;

    private TabLayout.OnTabSelectedListener mOnTabChangedListener;
    private int mCurrentItemForCrop = -1;
    ArrayList<ScanPage> scanPages;
    private TextView successfulScanCount;
    private ImageView cancelScanAction;
    private View toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(getResources().getIdentifier("activity_main", "layout", getPackageName()));
        findViews();
        restoreInstanceState(savedInstanceState);

        scanPages = (ArrayList<ScanPage>) getIntent().getSerializableExtra(DocumentActivity.RESULT_PAGES);

        initAppBar();
        initAdapter(new ArrayList<ScanPage>());
        initViewPager();
        initTabView();
        if (scanPages != null) {
            addPages(scanPages);
        }
        mAdapter.refresh(mViewPager);

    }

    private void findViews() {
        mPageNumber = (TextView) findViewById(getResources().getIdentifier("nY_pageNumber", "id", getPackageName()));
        mContainer = (PagerContainer) findViewById(getResources().getIdentifier("nY_pagerContainer", "id", getPackageName()));
        mViewPager = mContainer.getViewPager();
        mTabs = (TabLayout) findViewById(getResources().getIdentifier("tabs", "id", getPackageName()));
        mNoItemView = (TextView) findViewById(getResources().getIdentifier("nY_noItemView", "id", getPackageName()));
        mProgress = (ProgressBar) findViewById(getResources().getIdentifier("progress", "id", getPackageName()));
        toolbar = findViewById(getResources().getIdentifier("standart_toolbar", "id", getPackageName()));

        toolbar.setBackgroundColor(DocumentActivity.TOOLBARCOLOR);
        getWindow().setStatusBarColor(DocumentActivity.TOOLBARCOLOR);
        getWindow().setNavigationBarColor(DocumentActivity.TOOLBARCOLOR);

        successfulScanCount = (TextView) findViewById(getResources().getIdentifier("finish_activity", "id", getPackageName()));
        successfulScanCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAndReturnResult();
            }
        });
        cancelScanAction = (ImageView) findViewById(getResources().getIdentifier("cancel_scan_action_overview", "id", getPackageName()));
        cancelScanAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OverviewActivity.this);

                builder.setTitle(getResources().getString(getResources().getIdentifier("confirm", "string", getPackageName())));
                builder.setMessage(getResources().getString(getResources().getIdentifier("force_cancel_app", "string", getPackageName())));

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        finish();
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

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

    private void finishAndReturnResult() {
        if (documentCanBeGenerated()) {
            JSONArray resultDocument = prepareResultJson();
            Intent data = new Intent();
            data.putExtra(RESULT_DOCUMENT_EXTRA, resultDocument.toString());
            this.setResult(RESULT_OK, data);
            finish();
        }
    }


    private void initAppBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDefaultDisplayHomeAsUpEnabled(true);
        }
    }

    private JSONArray prepareResultJson() {
        showProgress(true);
        JSONArray resultJson = new JSONArray();
        for (int i = 0; i < mAdapter.getCount(); i++) {
            ScanPage scanPage = mAdapter.getScanPage(i);
            if (scanPage.isRotated()) {
                FileUtil.writeOrientationToFile(scanPage.getCroppedImagePath(), scanPage.getRotationInDegrees(), getBaseContext());
            }
            try {
                JSONObject json = new JSONObject();
                json.put("imagePath", scanPage.getCroppedImagePath());
                json.put("fullImagePath", scanPage.getFullImagePath());
                json.put("outline", pointsToJson(scanPage.getTransformationPoints()).toString());
                resultJson.put(json);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        showProgress(false);
        return resultJson;
    }


    private JSONArray pointsToJson(ArrayList<PointF> corners) {
        JSONArray jsonCorners = new JSONArray();
        for(PointF corner : corners){
            JSONObject jsonCorner = new JSONObject();
            try {
                jsonCorner.put("x", corner.x);
                jsonCorner.put("y", corner.y);
            }catch (JSONException e){
                e.printStackTrace();
            }
            jsonCorners.put(jsonCorner);
        }
        return jsonCorners;
    }

    private void showProgress(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        mProgress.setVisibility(visibility);
    }

    private boolean isProgressVisible() {
        return mProgress.getVisibility() == View.VISIBLE;
    }

    private void cleanUpSessionData() {
        // remove local files
        PrefsUtil.clearSubject(this);
        if (FileUtil.isExternalStorageWritable()) {
            File[] files = FileUtil.getExternalFiles(getExternalFilesDir(Environment.DIRECTORY_PICTURES), DocumentActivity.SESSION_FOLDER_ORIGINALS);
            for (File file : files) {
                file.delete();
            }
            files = FileUtil.getExternalFiles(getExternalFilesDir(Environment.DIRECTORY_PICTURES), DocumentActivity.SESSION_FOLDER_TRANSFORMED);
            for (File file : files) {
                file.delete();
            }
            files = FileUtil.getExternalFiles(getExternalFilesDir(Environment.DIRECTORY_PICTURES), DocumentActivity.SESSION_FOLDER_OCR_OPTIMIZED);
            for (File file : files) {
                file.delete();
            }
            files = FileUtil.getExternalFiles(getExternalFilesDir(Environment.DIRECTORY_PICTURES), DocumentActivity.SESSION_FOLDER_ERROR);
            for (File file : files) {
                file.delete();
            }
        }
    }

    private boolean documentCanBeGenerated() {

        if (mAdapter.getCount() == 0) {
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getIdentifier("message_empty_document", "string", getPackageName()))
                    .setPositiveButton(getResources().getIdentifier("yes", "string", getPackageName()), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setNegativeButton(getResources().getIdentifier("continue_editing", "string", getPackageName()), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
            return false;
        }
        return true;
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (getIntent().getExtras() != null && getIntent().hasExtra(EXTRA_CLEAR_SESSION)) {
                if (getIntent().getBooleanExtra(EXTRA_CLEAR_SESSION, false))
                    cleanUpSessionData();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(EDITING_SCANPAGE_INDEX, mCurrentItemForCrop);
        outState.putParcelableArrayList(SAVED_SCANPAGES, mAdapter.getParcelableData());
        super.onSaveInstanceState(outState);
    }

    private void initViewPager() {
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setClipChildren(false);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateCurrentPageViews(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initAdapter(List<ScanPage> myScannedPages) {
        mAdapter = new ScanPagePagerAdapter(myScannedPages, getBaseContext(), mViewPager);
        if (mAdapter.getCount() > 0) {
            updateCurrentPageViews(0);
        } else {
            updateCurrentPageViews(-1);
        }
    }

    private void initTabView() {
        mOnTabChangedListener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                performTabAction(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                performTabAction(tab);
            }
        };
        mTabs.addOnTabSelectedListener(mOnTabChangedListener);
    }


    @Override
    protected void onDestroy() {
        mTabs.removeOnTabSelectedListener(mOnTabChangedListener);
        mAdapter.destroy();
        super.onDestroy();
    }

    private void updateCurrentPageViews(int position) {
        if (mAdapter.getCount() <= 0) {
            mPageNumber.setVisibility(View.INVISIBLE);
            mNoItemView.setVisibility(View.VISIBLE);
        } else {
            mPageNumber.setVisibility(View.VISIBLE);
            mNoItemView.setVisibility(View.GONE);
        }
        mPageNumber.setText(position + 1 + "/" + mAdapter.getCount());
    }

    private void performTabAction(TabLayout.Tab tab) {
        if (isProgressVisible()) {
            Toast.makeText(this, getResources().getIdentifier("error_wait_for_action", "string", getPackageName()), Toast.LENGTH_SHORT).show();
            return;
        }
        if (tab.getPosition() == 0) {
           startCropActivity();
        } else if (tab.getPosition() == 1) {
            rotateCurrentPage();
        } else if (tab.getPosition() == 2) {
            startDocumentScanActivity();
        }
//        else if (tab.getPosition() == 3) {
//            startDocumentScanActivity();
//        }
    }

    private void startCropActivity() {
        if (mAdapter.getCount() <= 0) return;
        Intent cropActivityIntent = new Intent(OverviewActivity.this, CropDocumentActivity.class);
        mCurrentItemForCrop = mViewPager.getCurrentItem();
        ScanPage scanPage = mAdapter.getScanPage(mCurrentItemForCrop);
        cropActivityIntent.putExtra(CropDocumentActivity.EXTRA_FULL_IMAGE_PATH, scanPage.getFullImagePath());
        cropActivityIntent.putParcelableArrayListExtra(CropDocumentActivity.EXTRA_CORNERS, scanPage.getTransformationPoints());
        startActivityForResult(cropActivityIntent, CROP_DOCUMENT_REQUEST);

    }

    private void rotateCurrentPage() {
        if ((mAdapter.getCount() <= 0)) return;
        int currentItem = mViewPager.getCurrentItem();
        mAdapter.getScanPage(currentItem).rotateCw();
        mAdapter.refresh(mViewPager);
    }


    private void addPages(List<ScanPage> scanPages) {
        mAdapter.addAllToList(scanPages, mViewPager);
        mViewPager.setCurrentItem(mAdapter.getCount() - 1);
        updateCurrentPageViews(mViewPager.getCurrentItem());

    }

    private void deletePage() {
        if (mAdapter.getCount() == 0) return;
        int currentItem = mViewPager.getCurrentItem();
        mAdapter.removeFromList(mViewPager, currentItem);
        updateCurrentPageViews(mViewPager.getCurrentItem());
    }

    private void startDocumentScanActivity() {
        Intent data = new Intent();

        // enable multiPageScanning!
        // data.putExtra(DocumentActivity.RESULT_PAGES, (ArrayList) mAdapter.getAllScanPages());

        this.setResult(AnylinePlugin.RESULT_SWITCH, data);
        finish();
    }


    private boolean checkCameraPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(OverviewActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {


            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(OverviewActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    startDocumentScanActivity();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, getResources().getIdentifier("error_no_camera_permission", "string", getPackageName()), Toast.LENGTH_LONG).show();
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if (requestCode == CROP_DOCUMENT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    final ArrayList<PointF> corners = extras.getParcelableArrayList(CropDocumentActivity.RESULT_CORNERS);
                    final String fullImagePath = extras.getString(CropDocumentActivity.RESULT_FULL_IMAGE_PATH);
                    File targetDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), DocumentActivity.SESSION_FOLDER_TRANSFORMED);
                    String filename = "" + System.currentTimeMillis() + "" + DocumentActivity.TRANSFORMED_IMAGE_POSTFIX;
                    final File outFile = new File(targetDir, filename);
                    performTransformAsync(corners, fullImagePath, outFile.getAbsolutePath());

                }
            }
        } else if (requestCode == TRANSFORM_DOCUMENT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    String ocrOptimizedFilePath = extras.getString(DocumentActivity.RESULT_TRANSFORM);
                    ScanPage scanPage = mAdapter.getScanPage(mViewPager.getCurrentItem());
                    scanPage.setCroppedImageFile(ocrOptimizedFilePath);
                    mAdapter.refresh(mViewPager);
                }
            }
        }
    }

    private void performTransformAsync(final ArrayList<PointF> corners, final String fullImagePath, final String outFilePath) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                return TransformationUtil.doTransformation(corners, fullImagePath, outFilePath); //outFile.toString()
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    onTransformDone(outFilePath, corners, mCurrentItemForCrop);
                } else {
                    // TODO: handle error
                }

                showProgress(false);
            }
        }.execute();
        showProgress(true);
    }

    private void onTransformDone(String outFilePath, final ArrayList<PointF> corners, int item) {
        ScanPage scanPage = mAdapter.getScanPage(item);
        if (scanPage == null) {
            Log.e(TAG, "Transformation Result cannot be saved, ScanPage no longer exists");
        }
        scanPage.setTransformationPoints(corners);
        scanPage.setCroppedImageFile(outFilePath);
        mAdapter.refresh(mViewPager);
        mCurrentItemForCrop = -1;
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentItemForCrop = savedInstanceState.getInt(EDITING_SCANPAGE_INDEX);
            ArrayList<ScanPage> saved = savedInstanceState.getParcelableArrayList(SAVED_SCANPAGES);
            this.mAdapter.updateList(saved, mViewPager);
            if (mCurrentItemForCrop == -1)
                mViewPager.setCurrentItem(mAdapter.getCount() - 1);
            else
                mViewPager.setCurrentItem(mCurrentItemForCrop);
            updateCurrentPageViews(mViewPager.getCurrentItem());
        }
    }
}
