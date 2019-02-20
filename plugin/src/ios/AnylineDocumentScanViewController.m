//
//  AnylineDocumentScanViewController.m
//  Anyline Cordova Example
//
//  Created by Daniel Albertini on 23/06/16.
//
//

#import "AnylineDocumentScanViewController.h"
#import <Anyline/Anyline.h>
#import "ALDocumentScanner/ALDocumentCropViewController.h"
#import "ALDocumentScanner/ALResultPage.h"
#import "ALDocumentScanner/ALDocumentOverviewViewController.h"
#import "ALDocumentScanner/ALResultDocument.h"
#import "ALRoundedView.h"
#import "ALManualTriggerButton.h"
#import "ALLocalizationMacro.h"

static CGFloat const kLabelSidePadding =                                20;
static CGFloat const kLabelTopPadding =                                 115;
static CGFloat const kLabelHeight =                                     30;


@interface AnylineDocumentScanViewController ()<AnylineDocumentModuleDelegate, ALDocumentCropViewControllerDelegate, ALDocumentOverviewViewControllerDelegate>
@property (nonatomic, strong) ALRoundedView *roundedView;
@property (nonatomic, strong) ALRoundedView *processingLabel;

@property (nonatomic, assign) NSInteger showingLabel;

@property (nonatomic, strong) IBOutlet ALManualTriggerButton *triggerCameraButton;
@property (nonatomic, strong) NSMutableArray<ALResultPage *> *scannedPages;

@property (strong, nonatomic) NSTimer *scanDelayDebouncer;

@property (strong, nonatomic) NSTimer *triggerCameraButtonTimeout;

@end

@implementation AnylineDocumentScanViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    dispatch_async(dispatch_get_main_queue(), ^{
        AnylineDocumentModuleView *docModuleView = [[AnylineDocumentModuleView alloc] initWithFrame:self.view.bounds];
        
        NSError *error = nil;
        BOOL success = [docModuleView setupWithLicenseKey:self.key delegate:self error:&error];
        
        docModuleView.currentConfiguration = self.conf;
        [docModuleView setDocumentRatios:@[@(ALDocumentRatioDINAXPortrait)]];
        docModuleView.maxDocumentRatioDeviation = @0.15;
        docModuleView.postProcessingEnabled = YES;
        self.isMultipage = self.cordovaConfig.multipageEnabled;
        self.hasManualCrop = self.cordovaConfig.manualCrop;
        
        if (self.isMultipage) {
            docModuleView.currentConfiguration.cancelOnResult = false;
        }
        
        self.scannedPages = [[NSMutableArray alloc] init];
        
        self.moduleView = docModuleView;
        
        [self.view addSubview:self.moduleView];
        
        [self.view sendSubviewToBack:self.moduleView];
        
        // This view notifies the user of any problems that occur while he is scanning
        self.roundedView = [[ALRoundedView alloc] initWithFrame:CGRectMake(20, 115, self.view.bounds.size.width - 40, 30)];
        self.roundedView.fillColor = [UIColor colorWithRed:98.0/255.0 green:39.0/255.0 blue:232.0/255.0 alpha:0.6];
        self.roundedView.textLabel.text = @"";
        self.roundedView.alpha = 0;
        [self.view addSubview:self.roundedView];
        
        
        if (self.hasManualCrop) {
            self.triggerCameraButton = [[ALManualTriggerButton alloc] initWithFrame:CGRectMake(self.moduleView.center.x-25, self.moduleView.bounds.size.height-56, 52, 52) tintColor:self.cordovaConfig.manualScanButtonColor];
//            self.triggerCameraButton.color = self.cordovaConfig.manualScanButtonColor;
            
            [self.triggerCameraButton addTarget:self action:@selector(onManualTrigger:) forControlEvents:UIControlEventTouchUpInside];
            [self.view addSubview:self.triggerCameraButton];
            
            [self resetTriggerButtonTimer];
        }
        
        if (self.isMultipage) {
            UIBarButtonItem *cancelButton = [[UIBarButtonItem alloc]
                                             initWithTitle:ALLocalizedString(@"Cancel", @"Cancel title", self.cordovaConfig.languageKey)
                                             style:UIBarButtonItemStylePlain
                                             target:self
                                             action:@selector(cancelButtonPressed:)];
            
            self.navigationItem.leftBarButtonItem = cancelButton;
            
            //Remove default "OK" / done button of baseController
            [(UIButton*)[self.view viewWithTag:1] removeFromSuperview];
        }
    
        { // Processing Label
            self.processingLabel = [[ALRoundedView alloc] initWithFrame:CGRectMake(0, 0, self.view.frame.size.width-40, 30)];
            self.processingLabel.center = self.view.center;
            self.processingLabel.fillColor = [UIColor lightGrayColor];
            self.processingLabel.textLabel.textColor = [UIColor blackColor];
            self.processingLabel.textLabel.adjustsFontSizeToFitWidth = YES;
            self.processingLabel.textLabel.numberOfLines = 1;
            self.processingLabel.textLabel.minimumScaleFactor = 0.5;
            
            self.processingLabel.textLabel.text = ALLocalizedString(@"Processing", @"Processing label title", self.cordovaConfig.languageKey);
            self.processingLabel.alpha = 0;
            [self.view addSubview:self.processingLabel];
            
            self.processingLabel.translatesAutoresizingMaskIntoConstraints = NO;
            [[self view] addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|-padding-[processingLabel]-padding-|"
                                                                                options:0
                                                                                metrics:@{@"padding" : @(kLabelSidePadding)}
                                                                                  views:@{@"processingLabel" : self.processingLabel}]];
            [[self view] addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|-padding-[processingLabel(==height)]"
                                                                                options:0
                                                                                metrics:@{@"padding" : @(kLabelTopPadding), @"height" : @(kLabelHeight)}
                                                                                  views:@{@"processingLabel" : self.processingLabel}]];

        }
    });
}

#pragma mark - AnylineDocumentModuleDelegate method

- (void)anylineDocumentModuleView:(AnylineDocumentModuleView *)anylineDocumentModuleView
                        hasResult:(UIImage *)transformedImage
                        fullImage:(UIImage *)fullFrame
                  documentCorners:(ALSquare *)corners {
    
    if (!self.isMultipage) {
        NSMutableDictionary *dictResult = [NSMutableDictionary dictionaryWithCapacity:4];
        NSString *imagePath = [self saveImageToFileSystem:transformedImage];
        [dictResult setValue:imagePath forKey:[NSString stringWithFormat:@"imagePath"]];
        
        [self.delegate anylineBaseScanViewController:self didScan:dictResult continueScanning:!self.moduleView.cancelOnResult];
        
        if (self.moduleView.cancelOnResult) {
            [self dismissViewControllerAnimated:YES completion:NULL];
        }
    } else {
        [self updateResultDictionaryWithPage:[[ALResultPage alloc] initWithOriginalImage:fullFrame
                                                                        transformedImage:transformedImage
                                                                            imageCorners:corners]];
        
        // stops scanning and schedules the scanning to restart after our timeout. in case the view disappears the debounce is cleaned up and the scanner is restarted when the view appears again
        //[self _startScanDebounce];
        [self onFinishScanning:nil];    }
    
}

- (void)anylineDocumentModuleView:(AnylineDocumentModuleView *)anylineDocumentModuleView
             reportsPreviewResult:(UIImage *)image {
    [self showProcessing:YES];
}



- (void)_startScanDebounce {
    [self resetTriggerButtonTimer];
    // cancel any pending delayed execution
    [self _cancelScanDebounce];
    
    // schedule a new one at the appropriate time
    self.scanDelayDebouncer = [NSTimer scheduledTimerWithTimeInterval:2 target:self selector:@selector(_startScanning) userInfo:nil repeats:NO];
}


- (void)_startScanning {
    [self resetTriggerButtonTimer];
    /*
     This is the place where we tell Anyline to start receiving and displaying images from the camera.
     Success/error tells us if everything went fine.
     */
    NSError *error;
    BOOL success = [self.moduleView startScanningAndReturnError:&error];
    if (!success) {
        // Something went wrong. The error object contains the error description
        [[[UIAlertView alloc] initWithTitle:ALLocalizedString(@"Start Scanning Error", @"Title for the alert view if the scanner start action fails", self.cordovaConfig.languageKey)
                                    message:error.debugDescription
                                   delegate:self
                          cancelButtonTitle:ALLocalizedString(@"OK", @"Title for the alert view dismiss button for failed scan action start", self.cordovaConfig.languageKey)
                          otherButtonTitles:nil] show];
    }
}

- (void)_cancelScanning {
    // guard against the scanner still running
    if (self.moduleView.isRunning) {
        // Cancel scanning to allow the module to clean up
        NSError *error;
        BOOL success = [self.moduleView cancelScanningAndReturnError:&error];
        if (!success) {
            // Something went wrong. The error object contains the error description
            [[[UIAlertView alloc] initWithTitle:ALLocalizedString(@"Cancel Scanning Error", @"Title for the alert view if the scanner cancel action fails", self.cordovaConfig.languageKey)
                                        message:error.debugDescription
                                       delegate:self
                              cancelButtonTitle:ALLocalizedString(@"OK", @"Title for the alert view dismiss button for failed scan action start", self.cordovaConfig.languageKey)
                              otherButtonTitles:nil] show];
        }
    }
}


- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    [self _startScanning];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    [self _cancelScanDebounce];
    [self _cancelScanning];
}

- (void)_cancelScanDebounce {
    // cancel the delayed execution
    [self.scanDelayDebouncer invalidate];
    self.scanDelayDebouncer = nil;
}

/*
 This delegate will be called when a manual picture was taken.
 */
- (void)anylineDocumentModuleView:(AnylineDocumentModuleView *)anylineDocumentModuleView
           detectedPictureCorners:(ALSquare *)corners
                          inImage:(UIImage *)image {
    
    ALResultPage *scannedPage = [[ALResultPage alloc] initWithOriginalImage:image imageCorners:corners];
    
    ALDocumentCropViewController *cropVC = [[ALDocumentCropViewController alloc] initWithPage:scannedPage cordovaConfiguration:self.cordovaConfig];
    cropVC.delegate = self;
    UINavigationController *nav = [[UINavigationController alloc]initWithRootViewController:cropVC];
    [nav.navigationBar setBarTintColor:self.navigationController.navigationBar.barTintColor];
    [nav.navigationBar setTranslucent:self.navigationController.navigationBar.translucent];

    [self presentViewController:nav animated:YES completion:nil];
}

/*
 This method receives errors that occured during the scan.
 */
- (void)anylineDocumentModuleView:(AnylineDocumentModuleView *)anylineEnergyModuleView reportsPictureProcessingFailure:(ALDocumentError)error {
    [self showProcessing:NO];
    [self showUserLabel:error];
}

/*
 This method receives errors that occured during the scan.
 */
- (void)anylineDocumentModuleView:(AnylineDocumentModuleView *)anylineEnergyModuleView reportsPreviewProcessingFailure:(ALDocumentError)error {
    [self showProcessing:NO];
    [self showUserLabel:error];
}

- (BOOL)anylineDocumentModuleView:(AnylineDocumentModuleView *)anylineDocumentModuleView
          documentOutlineDetected:(ALSquare *)outline
                      anglesValid:(BOOL)anglesValid {
    return NO;
}

/*
 This method receives the output ALResult page from the cropView
 */
- (void)cropViewControllerDidFinishCropping:(ALDocumentCropViewController *)cropVC withResult:(ALResultPage *)resultPage {
    [self updateResultDictionaryWithPage:resultPage];
    [self onFinishScanning:nil];
}

/*
 Delegate method will receive an ALResultDocument form the Overview/Gallerie. It contains a ALResultPage per image.
 */
- (void)documentScanner:(ALDocumentOverviewViewController *)documentScannerVC didFinishScanWithResult:(ALResultDocument *)result {
    
    
    
    [(AnylineDocumentModuleView *) self.moduleView stopListeningForMotion];
    [(AnylineDocumentModuleView *) self.moduleView cancelScanningAndReturnError:nil];
    
    NSMutableArray *resultArray = [[NSMutableArray alloc] init];
    
    for(ALResultPage *page in result.pages) {
        NSDictionary * dictResult = [NSMutableDictionary dictionary];
        NSString *imagePath = page.ocrOptimisedImagePath;
        NSString *fullImagePath = page.originalImagePath;
        
        [dictResult setValue:imagePath forKey:@"imagePath"];
        [dictResult setValue:fullImagePath forKey:@"fullImagePath"];
        
        NSMutableArray * outlineArr = [NSMutableArray array];
        [outlineArr addObject:[self _dictforPoint:page.imageCorners.topLeft]];
        [outlineArr addObject:[self _dictforPoint:page.imageCorners.topRight]];
        [outlineArr addObject:[self _dictforPoint:page.imageCorners.bottomRight]];
        [outlineArr addObject:[self _dictforPoint:page.imageCorners.bottomLeft]];
        
        [dictResult setValue:outlineArr forKey:@"outline"];
        [resultArray addObject:dictResult];
    }
    
    [self.delegate anylineBaseScanViewController:self didScan:resultArray continueScanning:false];
    [self.presentingViewController dismissViewControllerAnimated:YES completion:nil];
}



- (NSDictionary*)_dictforPoint:(CGPoint)point {
    NSMutableDictionary * dict = [NSMutableDictionary dictionary];
    [dict setValue:[NSString stringWithFormat:@"%f", point.x] forKey:@"x"];
    [dict setValue:[NSString stringWithFormat:@"%f", point.y] forKey:@"y"];
    return dict;
}


#pragma mark -- Helper Methods

- (void)updateResultDictionaryWithPage: (ALResultPage *)page {
    [self.scannedPages addObject:page];
}

/*
 Shows a little round label at the bottom of the screen to inform the user what happended
 */
- (void)showUserLabel:(ALDocumentError)error {
    NSString *helpString = nil;
    switch (error) {
        case ALDocumentErrorNotSharp:
            helpString = ALLocalizedString(@"Document not Sharp", @"Document not sharp warning", self.cordovaConfig.languageKey);
            break;
        case ALDocumentErrorSkewTooHigh:
            helpString = ALLocalizedString(@"Wrong Perspective", @"Document has wrong perspective warning", self.cordovaConfig.languageKey);
            break;
        case ALDocumentErrorImageTooDark:
            helpString = ALLocalizedString(@"Too Dark", @"Document is too dark warning", self.cordovaConfig.languageKey);
            break;
        case ALDocumentErrorShakeDetected:
            //helpString = ALLocalizedString(@"Too much shaking", @"Device is shaking too much warning", self.cordovaConfig.languageKey);
            //[self resetTriggerButtonTimer];
            break;
        default:
            break;
    }
    
    // The error is not in the list above or a label is on screen at the moment
    if(!helpString || self.showingLabel == 1) {
        return;
    }
    
    self.showingLabel = 1;
    self.roundedView.textLabel.text = helpString;
    
    
    // Animate the appearance of the label
    CGFloat fadeDuration = 0.8;
    [UIView animateWithDuration:fadeDuration animations:^{
        self.roundedView.alpha = 1;
    } completion:^(BOOL finished) {
        [UIView animateWithDuration:fadeDuration animations:^{
            self.roundedView.alpha = 0;
        } completion:^(BOOL finished) {
            self.showingLabel = 0;
        }];
    }];
}

- (void)resetTriggerButtonTimer {
    [self.triggerCameraButtonTimeout invalidate];
    self.triggerCameraButton.mode = ALManualCropButtonModeSearching;
    self.triggerCameraButtonTimeout = [NSTimer scheduledTimerWithTimeInterval:3 repeats:NO block:^(NSTimer * _Nonnull timer) {
        self.triggerCameraButton.mode = ALManualCropButtonModePhoto;
    }];
}

#pragma mark - IBAction

- (IBAction)onManualTrigger:(id)sender {
    [(AnylineDocumentModuleView *) self.moduleView triggerPictureCornerDetectionAndReturnError:nil];
}

- (IBAction)onFinishScanning:(id)sender {
    ALDocumentOverviewViewController *vc = [[ALDocumentOverviewViewController alloc] initWithAnylineLicenseKey:self.key
                                                                                               documentSubject:@""
                                                                                                      delegate:self
                                                                                          cordovaConfiguration:self.cordovaConfig
                                                                                                  scannedPages:self.scannedPages];
    
    UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController:vc];
    [nav.navigationBar setBarTintColor:self.navigationController.navigationBar.barTintColor];
    [nav.navigationBar setTranslucent:self.navigationController.navigationBar.translucent];

    [self presentViewController:nav animated:YES completion:nil];
}

- (void)cancelButtonPressed:(id)sender {
        __weak __typeof(self) weakSelf = self;
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:ALLocalizedString(@"Are you sure you want to exit?", @"Asks the user if he wants to cancel scanning", self.cordovaConfig.languageKey)
                                                                         message:ALLocalizedString(@"This scan will be deleted!", @"Asks the user if he wants to cancel scanning (message)", self.cordovaConfig.languageKey)
                                                                  preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:ALLocalizedString(@"Continue scanning", @"Continue scanning", self.cordovaConfig.languageKey) style:UIAlertActionStyleCancel handler:^(UIAlertAction *action) {
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:ALLocalizedString(@"Cancel", @"Cancel scanning", self.cordovaConfig.languageKey) style:UIAlertActionStyleDestructive handler:^(UIAlertAction *action) {
        [weakSelf _abortScanningAnimated:YES];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}

- (void)documentScannerDidAbort:(nonnull ALDocumentOverviewViewController *)documentScannerVC; {
    [self _abortScanningAnimated:YES];
}


- (void)documentScannerDeleteAllPages {
    self.scannedPages = [[NSMutableArray alloc] init];
}

- (void)_abortScanningAnimated:(BOOL)animated {
    [self _cancelScanDebounce];
    [self.moduleView cancelScanningAndReturnError:nil];
    [self dismissViewControllerAnimated:animated completion:^{
        [self.delegate anylineBaseScanViewController:self didStopScanning:nil];
    }];
}

/**
 *  Shows processing label
 */
- (void)showProcessing:(BOOL)showProcessing {
    [UIView animateWithDuration:0.3 animations:^{
        self.processingLabel.alpha = showProcessing ? 0.8 : 0;
    }];
}

@end
