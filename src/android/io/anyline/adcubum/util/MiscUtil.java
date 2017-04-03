package io.anyline.adcubum.util;

import android.graphics.PointF;

import java.util.List;

public class MiscUtil {

    public static String toPrettyString(List<PointF> corners) {
        String prettyString = "";
        String prefix = "";
        for(int i = 0; i < corners.size(); i++) {

            prettyString += prefix + corners.get(i);
            if(i == 0) prefix = "; ";
        }
        return prettyString;
    }
}
