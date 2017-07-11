/*
 * Anyline Cordova Plugin
 * anyline.document.js
 *
 * Copyright (c) 2016 Anyline GmbH
 */

if (anyline === undefined) {
    var anyline = {};
}
anyline.document = {
    onResult: function (stringResult) {
        var result = JSON.parse(stringResult);
        var div = document.getElementById('results');
        result.map(function (singleResult) {
            div.innerHTML = "<p><b>Cropped image:</b><img src=\"" + singleResult.imagePath + "\" width=\"100%\" height=\"auto\"/><br/>" +
                "<br/><b>Full image:</b><img src=\"" + singleResult.fullImagePath + "\" width=\"100%\" height=\"auto\"/>"
                + "<br/><i><b>Outline Points:</b> " + singleResult.outline + "</i>"
                + "</p><hr />" + div.innerHTML;
        });
        document.getElementById("details_scan_modes").removeAttribute("open");
        document.getElementById("details_results").setAttribute("open", "");
        window.scrollTo(0, 0);
    },

    onError: function (error) {
        console.log("-------- ERROR");
        //called if an error occurred or the user canceled the scanning
        if (error == "Canceled") {
            //do stuff when user has canceled
            // this can be used as an indicator that the user finished the scanning if canclelOnResult is false
            console.log("AnylineOcr scanning canceled");
            return;
        }

        alert(error);
    },


    licenseKey: "eyAiYW5kcm9pZElkZW50aWZpZXIiOiBbICJpby5hbnlsaW5lLmFkY3VidW0iIF0s" +
    "ICJkZWJ1Z1JlcG9ydGluZyI6ICJvbiIsICJpb3NJZGVudGlmaWVyIjogWyAiaW8u" +
    "YW55bGluZS5hZGN1YnVtIiBdLCAibGljZW5zZUtleVZlcnNpb24iOiAyLCAibWFq" +
    "b3JWZXJzaW9uIjogIjMiLCAicGluZ1JlcG9ydGluZyI6IHRydWUsICJwbGF0Zm9y" +
    "bSI6IFsgImlPUyIsICJBbmRyb2lkIiBdLCAic2NvcGUiOiBbICJET0NVTUVOVCIg" +
    "XSwgInNob3dXYXRlcm1hcmsiOiBmYWxzZSwgInRvbGVyYW5jZURheXMiOiA2MCwg" +
    "InZhbGlkIjogIjIwMTctMDktMzAiIH0KbFo4bzVTVWZzcE5zYnYySlMyZlA3T3Vz" +
    "QlROSHJVNEVUTk5aVWY4MWZjc0ZscHY3ZzNxTDBKMkRGa3h2bk5rVQpCdGJtdFhN" +
    "S3hKd2p0c1k3Q0ZJTzd1aEpaVHVidG1SZUltNmo3WTk5Y3V2blBnRkxVU0Z4ZTlK" +
    "aTdTemxWTEJhCjBtZE1weEplZzR4QzdxVFg2UnNyaW9uekVCT205TUx5eW45SlBO" +
    "cjNLWWlxaXl0RkZRVFpoMzVvRURJcVF0WTEKdjVhRUpTWVRWTnhORHRCT0tnRnBM" +
    "TVVza2JxMlcwRWRaazBINnhTQVd6RWZ2MU1ERFhRek5HdVRGck5UeWs5bQpUeDFS" +
    "SDl3WWNlM0lKVXQvS09FcGtpaEcyQmFMQWw1SStFRWl3TXVBeDNWWE9mZjlxcUtn" +
    "WEpEZUs3T0tHTDFvClhYMGdxRWpWRkpjT3lRVTdsRVZBMGc9PQo=",
    viewConfig: {
        "captureResolution": "720p",
        "pictureResolution": "1080p",
        "cutout": {
            "style": "rect",
            "maxWidthPercent": "100%",
            "maxHeightPercent": "100%",
            "width": 720,
            "ratioFromSize": {
                "width": 10,
                "height": 15
            },
            "alignment": "center",
            "strokeWidth": 2,
            "cornerRadius": 0,
            "strokeColor": "00000000"
        },
        "flash": {
            "mode": "manual",
            "alignment": "bottom_right",
            "offset": {
                "x": 0,
                "y": 0
            }
        },
        "visualFeedback": {
            "style": "RECT",
            "strokeColor": "300099FF",
            "animationDuration": 150,
            "cornerRadius": 2
        },
        "beepOnResult": true,
        "vibrateOnResult": true,
        "blinkAnimationOnResult": true,
        "cancelOnResult": true,
        "multipage": {
            "multipageEnabled": false,
            "multipageTintColor": "FF004583",
            "multipageTranslucent": false
        },
        "manualCrop": true,

        // Hex Color Code as String
        "manualScanButtonColor" : "FF004583",
        // Integer
        "manualScanButtonStartDuration" : 3000,

        "languageKey" : "de"
    },

    scan: function () {
        cordova.exec(this.onResult, this.onError, "AnylineSDK", "DOCUMENT", [this.licenseKey, this.viewConfig]);
    }
};
