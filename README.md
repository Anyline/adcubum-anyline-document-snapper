## Adcubum Anyline SDK Cordova Plugin ##

Anyline provides an easy-to-use SDK for applications to enable Optical Character Recognition (OCR) on mobile devices.

### Release Notes 0.1.0
- multipage document Scan flow implemented
- manual shutter + (semi) manual crop View
- overview-view to edit / rotate / delete
- top-bar for navigation and page-counter
- scanning only supported in portrait mode

#### Known Issues
Android Known Issues:
- manual crop view overlay calculation offset
- sometimes after manual shutter scan, flow stuck in processing image

iOS Localization
- Localization string files needs to be added to the generated cordova project. Already localized strings available in the ios plugin folder.

```Depending on the current setup, it may be worthwhile to use a localization plugin to manage both iOS and Android strings in one place (JSON)```

### Available Documentation
- [**Anyline Document:**](https://documentation.anyline.io/toc/platforms/cordova/index.html) Welcome to the Anyline Cordova Developer Guide

### Requirements

#### Android
- Android device with SDK >= 15
- decent camera functionality (recommended: 720p and adequate auto focus)

#### iOS
- minimum iOS 8.2
- minimum iPhone4s


### Quick Start - Setup
This is just a simple setup guide to integrate the anylinesdk-plugin in an existing Cordova project.<br/>
For more information about Cordova, how to use plugins, etc. see <a target="_blank" href="https://cordova.apache.org/">https://cordova.apache.org/</a>.

###### 1. Add the anylinesdk-plugin to your existing cordova project
```
cordova plugin add io-anyline-cordova
```

Or use plugman. E.g. for android:

```
plugman install --platform android --project platforms/android --plugin io-anyline-cordova
```

If you'd like to clone the repository you will have to use git-lfs. Use the following commands to install git-lfs.
```
brew install git-lfs
git lfs install
```
If you prefer downloading a package, use the provided `zip` package on the [releases page](https://github.com/Anyline/anyline-ocr-cordova-module/releases). Be aware that the github download zip button does not work for projects with git-lfs.




###### 2. Plugin Usage

```javaScript
cordova.exec(onResult, onError, "AnylineSDK", scanMode, config);
```

- <b>onResult</b>: a function that is called on a scan result
- <b>onError</b>: a function that is called on error or when the user canceled the scanning
- <b>AnylineSDK</b>: add this *string* to make sure the anyline-sdk plugin is called
- <b>scanMode</b>: "<i>MRZ</i>", "<i>BARCODE</i>", "<i>ANYLINE_OCR</i>", "<i>ELECTRIC_METER</i>", "<i>GAS_METER</i>" (more Energy modes can be found [here](https://documentation.anyline.io/#energy))
- <b>config</b>: an array
    * <b>config[0]</b>: the license key
    * <b>config[1]</b>: the [view config](https://documentation.anyline.io/#anyline-config)
    * <b>config[2]</b>: the [ocr config](https://documentation.anyline.io/#anyline-ocr) (only uses with mode ANYLINE_OCR)


> Example for **config** from MRZ:

```json
[
    "YOUR_LICENSE_KEY",
    {
        "captureResolution": "1080p",
        "cutout": {
            "style": "rect",
            "maxWidthPercent": "90%",
            "maxHeightPercent": "90%",
            "alignment": "top_half",
            "strokeWidth": 2,
            "cornerRadius": 4,
            "strokeColor": "FFFFFF",
            "outerColor": "000000",
            "outerAlpha": 0.3
        },
        "flash": {
            "mode": "manual",
            "alignment": "bottom_right"
        },
        "beepOnResult": true,
        "vibrateOnResult": true,
        "blinkAnimationOnResult": true,
        "cancelOnResult": true
    }
]
```


###### 3. Run your cordova project: Enjoy scanning and have fun :)

Checkout our <a href="https://documentation.anyline.io/">online documentation</a>  for more details.


## License

See LICENSE file.
