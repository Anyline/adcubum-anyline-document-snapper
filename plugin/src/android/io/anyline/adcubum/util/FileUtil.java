package io.anyline.adcubum.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import at.nineyards.anyline.models.AnylineImage;
import io.anyline.adcubum.DocumentActivity;

public class FileUtil {

    private static final String TAG = FileUtil.class.getSimpleName();

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static File getExternalStorageFile(String filename) {
        File sd = Environment.getExternalStorageDirectory();
        return new File(sd, filename);
    }

    public static void copyRessourceToExternalStorage(Context context, int resourceId, String filename) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
        File sd = Environment.getExternalStorageDirectory();
        File dest = new File(sd, filename);
        try {
            FileOutputStream out;
            out = new FileOutputStream(dest);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteExternalStoragePrivatePicture(Context context, String filename) {
        // Create a path where we will place our picture in the user's
        // public pictures directory and delete the file.  If external
        // storage is not currently mounted this will fail.
        File path = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (path != null) {
            File file = new File(path, filename);
            file.delete();
        }
    }

    public static boolean hasExternalStoragePrivatePicture(Context context, String filename) {
        // Create a path where we will place our picture in the user's
        // public pictures directory and check if the file exists.  If
        // external storage is not currently mounted this will think the
        // picture doesn't exist.
        File path = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (path != null) {
            File file = new File(path, filename);
            return file.exists();
        }
        return false;
    }

    public static File[] getExternalFiles(File parent, String child) {
        File outDir = new File(parent, child);
        outDir.mkdir();
        return outDir.listFiles();
    }

    public static File saveImage(File directory, String filename, AnylineImage image) {
        directory.mkdir();
        File outFile = new File(directory, filename);
        try {
            image.save(outFile, 100);
            return outFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean writeOrientationToFile(String filePath, float rotationAngle, Context context) {
        try {

            File targetDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), DocumentActivity.SESSION_FOLDER_TRANSFORMED);
            String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
            Log.d(TAG, "Orientation was: " + rotationAngle);

            Matrix matrix = new Matrix();
            matrix.postRotate(rotationAngle);
            Bitmap originalBitmap = BitmapFactory.decodeFile(filePath);

            Bitmap resizedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0,
                    originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);

            File outFile = new File(targetDir, filename);

            FileOutputStream fos;

            try {
                fos = new FileOutputStream(outFile);
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }
}
