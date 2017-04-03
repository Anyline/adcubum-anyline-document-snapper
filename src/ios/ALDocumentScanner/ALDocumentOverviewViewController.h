//
//  ALDocumentOverviewViewController.h
//  AnylineDocumentScanner
//
//  Created by Milutin Tomic on 16/08/16.
//  Copyright © 2016 9Y Media Group GmbH. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ALResultPage.h"

@class ALDocumentOverviewViewController, ALResultDocument;

@protocol ALDocumentOverviewViewControllerDelegate <NSObject>

@required

/**
 *  Called once the user completes the scanning process. The caller is responsible for dimissing the receiver.
 *
 *  @param documentScannerVC - document scanner view controller instance
 *  @param result            - scan results
 */
- (void)documentScanner:(nonnull ALDocumentOverviewViewController *)documentScannerVC didFinishScanWithResult:(nonnull ALResultDocument *)result;
- (void)documentScannerDidAbort:(nonnull ALDocumentOverviewViewController *)documentScannerVC;

@end


@interface ALDocumentOverviewViewController : UIViewController

/**
 *  Initializes document scanner with the passed anyline license key.
 *
 *  @param licenseKey - anyline license key
 *  @param subject    - document subject
 *  @param delegate   - delegate to receive scan results once the scan is finished
 */
- (nonnull instancetype)initWithAnylineLicenseKey:(nonnull NSString *)licenseKey documentSubject:(nullable NSString *)subject delegate:(nullable id<ALDocumentOverviewViewControllerDelegate>)delegate;

- (nonnull instancetype)initWithAnylineLicenseKey:(nonnull NSString *)licenseKey documentSubject:(nonnull NSString *)subject delegate:(nullable id<ALDocumentOverviewViewControllerDelegate>)delegate scannedPages:(nonnull NSArray<ALResultPage *> *)scannedPages;
@end
