package io.anyline.adcubum.util;

import android.graphics.PointF;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.imread;

public class TransformationUtil {


    public static boolean doTransformation(ArrayList<PointF> src, String fullImagePath, String targetImagePath) {
        List<PointF> dst = new ArrayList<>();

        Mat inputImage = imread(fullImagePath);


        Size size;

        //upLeft = src.get(0)
        //upRight = src.get(1)
        //downRight = src.get(2)
        //downLeft = src.get(3)

        int w1 = (int) Math.floor(
                Math.sqrt(Math.pow(src.get(2).x - src.get(3).x, 2) +
                        Math.pow(src.get(2).x - src.get(3).x, 2)
                ));

        int w2 = (int) Math.floor(
                Math.sqrt(Math.pow(src.get(1).x - src.get(0).x, 2) +
                        Math.pow(src.get(1).x - src.get(0).x, 2)
                ));

        int h1 = (int) Math.floor(
                Math.sqrt(Math.pow(src.get(1).y - src.get(2).y, 2) +
                        Math.pow(src.get(1).y - src.get(2).y, 2)
                ));

        int h2 = (int) Math.floor(
                Math.sqrt(Math.pow(src.get(0).y - src.get(3).y, 2) +
                        Math.pow(src.get(0).y - src.get(3).y, 2)
                ));

            int maxWidth = (w1 < w2) ? w1 : w2;
            int maxHeight = (h1 < h2) ? h1 : h2;

            size = new Size(maxWidth, maxHeight);


        dst.add(new PointF(0,0));
        dst.add(new PointF((float)size.width - 1,0));
        dst.add(new PointF((float)size.width-1,(float)size.height-1));
        dst.add(new PointF(0,(float)size.height-1));


        Mat transformedImage = Mat.eye(inputImage.rows(),inputImage.cols(),inputImage.type());

        Mat srcMat = arrayToMat(TransformationUtil.convertToOpenCVPointList(src));
        Mat dstMat = arrayToMat(TransformationUtil.convertToOpenCVPointList(dst));
        Imgproc.warpPerspective(inputImage, transformedImage, Imgproc.getPerspectiveTransform(srcMat, dstMat), size);

        boolean ret = Imgcodecs.imwrite(targetImagePath, transformedImage);
        srcMat.release();
        dstMat.release();
        inputImage.release();
        transformedImage.release();
        return ret;


    }

    public static Mat arrayToMat(List list) {
        return Converters.vector_Point2f_to_Mat(list);
    }

    private static List<Point> convertToOpenCVPointList (List<PointF> input){
        ArrayList<Point> ret = new ArrayList<>();

        for(PointF point : input){
            Point cvPoint =new Point();
            cvPoint.x = point.x;
            cvPoint.y = point.y;
            ret.add(cvPoint);
        }
        return ret;
    }


}
