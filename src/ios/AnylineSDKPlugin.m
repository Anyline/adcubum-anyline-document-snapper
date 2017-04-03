
#import "AnylineSDKPlugin.h"
#import <Anyline/Anyline.h>
#import "AnylineDocumentScanViewController.h"
#import "ALCordovaUIConfiguration.h"


@interface AnylineSDKPlugin()<AnylineBaseScanViewControllerDelegate>

@property (nonatomic, strong) AnylineBaseScanViewController *baseScanViewController;
@property (nonatomic, strong) ALUIConfiguration *conf;

@property (nonatomic, strong) NSString *callbackId;
@property (nonatomic, strong) NSString *appKey;

@property (nonatomic, strong) ALCordovaUIConfiguration *cordovaUIConf;

@end


@implementation AnylineSDKPlugin

- (void)DOCUMENT:(CDVInvokedUrlCommand *)command {
    [self processCommandArguments:command];
    
    [self.commandDelegate runInBackground:^{
        AnylineDocumentScanViewController *docScanViewController = [[AnylineDocumentScanViewController alloc] initWithKey:self.appKey configuration:self.conf cordovaConfiguration:self.cordovaUIConf delegate:self];
        
        self.baseScanViewController = docScanViewController;
        
        if(self.cordovaUIConf.multipageEnabled) {
            UINavigationController *navC = [[UINavigationController alloc] initWithRootViewController:docScanViewController];
            [navC.navigationBar setBarTintColor: self.cordovaUIConf.multipageTintColor];
            [navC.navigationBar setTranslucent:self.cordovaUIConf.multipageTranslucent];
            
            [self presentNavigationController:navC];
        } else {
            [self presentViewController];
        }
        
    }];
}

- (void)processCommandArguments:(CDVInvokedUrlCommand *)command {
    self.callbackId = command.callbackId;
    self.appKey = [command.arguments objectAtIndex:0];
    
    NSDictionary *options = [command.arguments objectAtIndex:1];
    self.conf = [[ALUIConfiguration alloc] initWithDictionary:options bundlePath:nil];
    
    self.cordovaUIConf = [[ALCordovaUIConfiguration alloc] initWithDictionary:options];
}

- (void)presentViewController {
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.viewController respondsToSelector:@selector(presentViewController:animated:completion:)]) {
            [self.viewController presentViewController:self.baseScanViewController animated:YES completion:NULL];
        } else {
            // ignore warning
            [self.viewController presentModalViewController:self.baseScanViewController animated:NO];
        }
    });
}

- (void)presentNavigationController:(UINavigationController *)navCon {
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.viewController respondsToSelector:@selector(presentViewController:animated:completion:)]) {
            [self.viewController presentViewController:navCon animated:YES completion:NULL];
        } else {
           //ignore warning
            [self.viewController presentModalViewController:navCon animated:NO];
        }
    });
}

#pragma mark - AnylineBaseScanViewControllerDelegate

- (void)anylineBaseScanViewController:(AnylineBaseScanViewController *)baseScanViewController didScan:(id)scanResult continueScanning:(BOOL)continueScanning {
    CDVPluginResult *pluginResult;
    if ([scanResult isKindOfClass:[NSString class]]) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:scanResult];
    } else {
        NSError * error;
        NSString * jsonString = @"";
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:scanResult
                                                           options:NSJSONWritingPrettyPrinted 
                                                             error:&error];
        if (! jsonData) {
            NSLog(@"Cloud not convert result to JSON: %@", error);
        } else {
            jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        }
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:jsonString];
    }
    if (continueScanning) {
        [pluginResult setKeepCallback:@YES];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
}


- (void)anylineBaseScanViewController:(AnylineBaseScanViewController *)baseScanViewController didStopScanning:(id)sender {
    CDVPluginResult *pluginResult;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Canceled"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
}

@end
