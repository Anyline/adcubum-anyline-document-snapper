package io.anyline.adcubum;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class ScanPage implements Parcelable {

    private static final String TAG = ScanPage.class.getSimpleName();

    // full res source image
    private File mFullImageFile = null;

    // cropped image
    private File mCroppedImageFile = null;

    private int mRotationInDegrees = 0;

    // contains the 4 points that are used for the transformation of the document
    private ArrayList<PointF> mTransformationPoints;

    public ScanPage(String fullImagePath, String croppedImagePath, ArrayList<PointF> corners){
        this(new File(fullImagePath), new File(croppedImagePath), corners);
    }

    public ScanPage(File fullImageFile, File croppedImageFile, ArrayList<PointF> corners){
        this.mFullImageFile = fullImageFile;
        this.mCroppedImageFile = croppedImageFile;
        this.mTransformationPoints = corners;
        if(!mCroppedImageFile.exists()) throw new RuntimeException("File does not exist");
        if(!mCroppedImageFile.exists()) throw new RuntimeException("File does not exist");
        if(mTransformationPoints == null) throw new RuntimeException("not transformation points defined");
        if(mTransformationPoints.size() != 4) throw new RuntimeException("need 4 transformation points");
        //TODO: advanced checks ...
    }

    public File getImage(){
        return mFullImageFile;
    }

    public void rotateCw() {
        Log.d(TAG, "rotateCw");
        mRotationInDegrees = (mRotationInDegrees + 90) % 360;
    }

    public String getFullImagePath() {
        if(mFullImageFile != null)
            return mFullImageFile.getAbsolutePath();
        else
            return null;
    }

    public String getCroppedImagePath() {
        if(mCroppedImageFile != null)
            return mCroppedImageFile.getAbsolutePath();
        else
            return null;
    }

    public File getFullImageFile() {
        return mFullImageFile;
    }

    public void setFullImageFile(File mImageFile) {
        this.mFullImageFile = mImageFile;
    }

    public File getCroppedImageFile() {
        return mCroppedImageFile;
    }

    public void setCroppedImageFile(File mImageFile) {
        this.mCroppedImageFile = mImageFile;
    }

    public int getRotationInDegrees() {
        return mRotationInDegrees;
    }

    public void setRotationInDegrees(int rotationInDegrees) {
        this.mRotationInDegrees = rotationInDegrees;
    }

    public ArrayList<PointF> getTransformationPoints() {
        return mTransformationPoints;
    }

    public void setTransformationPoints(ArrayList<PointF> transformationPoints) {
        this.mTransformationPoints = transformationPoints;
    }

    public void setCroppedImageFile(String filePath) {
        mCroppedImageFile = new File(filePath);
    }


    // Parcelling part
    public ScanPage(Parcel in){
        mFullImageFile = new File(in.readString());
        mCroppedImageFile = new File(in.readString());
        mRotationInDegrees = in.readInt();
        mTransformationPoints = new ArrayList<>();
        in.readTypedList(mTransformationPoints, PointF.CREATOR);
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFullImageFile.getAbsolutePath());
        dest.writeString(mCroppedImageFile.getAbsolutePath());
        dest.writeInt(mRotationInDegrees);
        dest.writeTypedList(mTransformationPoints);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ScanPage createFromParcel(Parcel in) {
            return new ScanPage(in);
        }

        public ScanPage[] newArray(int size) {
            return new ScanPage[size];
        }
    };

    public boolean isRotated() {
        return mRotationInDegrees != 0;
    }
}
