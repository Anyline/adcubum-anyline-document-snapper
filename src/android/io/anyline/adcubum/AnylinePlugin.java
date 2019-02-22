package io.anyline.adcubum;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;


public class AnylinePlugin extends CordovaPlugin{

    private static final String TAG = AnylinePlugin.class.getSimpleName();

    public static final String EXTRA_LICENSE_KEY = "EXTRA_LICENSE_KEY";
    public static final String EXTRA_CONFIG_JSON = "EXTRA_CONFIG_JSON";
    public static final String EXTRA_ERROR_MESSAGE = "EXTRA_ERROR_MESSAGE";

    public static final int RESULT_CANCELED = 0;
    public static final int RESULT_OK = -1;
    public static final int RESULT_ERROR = 2;
    public static final int RESULT_SWITCH = 3;

    public static final int REQUEST_DOCUMENT = 5;
    public static final int REQUEST_OVERVIEW = 6;

    private CallbackContext mCallbackContext;
    private String mAction;
    private JSONArray mArgs;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        mCallbackContext = callbackContext;
        mAction = action;
        mArgs = args;
        Log.d(TAG, "Starting action: " + action);

        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    checkPermission();
                } catch (Exception e) {
                    mCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Camera permission denied"));
                }
            }
        });

        return true;
    }

    private void checkPermission() {
        boolean result = cordova.hasPermission("android.permission.CAMERA");
        if (result) {
            startScanning(mAction, mArgs, null);
        } else {
            cordova.requestPermission(this, 55433, "android.permission.CAMERA");
        }
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                this.mCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Camera permission denied"));
                return;
            }
        }
        startScanning(mAction, mArgs, null);
    }

    private void startScanning(String action, JSONArray args, Serializable scanPages) {

        Intent intent = new Intent(cordova.getActivity(), DocumentActivity.class);

        try {
            intent.putExtra(EXTRA_LICENSE_KEY, args.getString(0));
            intent.putExtra(EXTRA_CONFIG_JSON, args.getString(1));

        } catch (JSONException e) {
            mCallbackContext.error(Resources.getString(cordova.getActivity(), "error_invalid_json_data"));
            return;
        }
        if (scanPages != null) {
            intent.putExtra(DocumentActivity.RESULT_PAGES, scanPages);
        }
        cordova.startActivityForResult(this, intent, REQUEST_DOCUMENT);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (resultCode) {
            case RESULT_OK:
                PluginResult pluginResult = new PluginResult(Status.OK, data.getStringExtra(OverviewActivity.RESULT_DOCUMENT_EXTRA));
                mCallbackContext.sendPluginResult(pluginResult);
                break;
            case RESULT_CANCELED:
                mCallbackContext.error("Canceled");
                break;
            case RESULT_ERROR:
                mCallbackContext.error(data.getStringExtra(EXTRA_ERROR_MESSAGE));
                break;
            case RESULT_SWITCH:
                switch (requestCode) {
                    case REQUEST_DOCUMENT:
                        Intent documentIntent = new Intent(cordova.getActivity(), OverviewActivity.class);
                        documentIntent.putExtra(DocumentActivity.RESULT_PAGES, data.getSerializableExtra(DocumentActivity.RESULT_PAGES));
                        cordova.startActivityForResult(this, documentIntent, REQUEST_OVERVIEW);
                        break;

                    case REQUEST_OVERVIEW:
                        startScanning(mAction, mArgs, data.getSerializableExtra(DocumentActivity.RESULT_PAGES));
                        break;
                }
                break;
        }


    }
}
