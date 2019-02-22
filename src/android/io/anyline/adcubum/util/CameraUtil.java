package io.anyline.adcubum.util;

import android.hardware.Camera;

public class CameraUtil {

    public static boolean setParameter(Camera camera, String flashMode){
        if(camera != null){
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(flashMode);
            try {
                camera.setParameters(parameters);
            }
            catch (RuntimeException e){
                e.printStackTrace();
                // catch: Camera is being used after Camera.release()
            }
            return true;
        }
        else {
            return false;
        }
    }
}
