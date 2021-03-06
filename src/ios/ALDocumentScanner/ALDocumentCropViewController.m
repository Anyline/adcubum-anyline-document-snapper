//
//  ALDocumentCropViewController.m
//  AnylineDocumentScanner
//
//  Created by Milutin Tomic on 28/08/16.
//  Copyright © 2016 9Y Media Group GmbH. All rights reserved.
//

#import "ALDocumentCropViewController.h"
#import "ALCroppingView.h"
#import "ALResultPage.h"
#import "ALMagnifyingGlass.h"
#import "ALLocalizationMacro.h"

@interface ALDocumentCropViewController ()

@property (weak, nonatomic) IBOutlet UIView             *containerView;
@property (weak, nonatomic) IBOutlet UIImageView        *pageImageView;
@property (weak, nonatomic) IBOutlet ALCroppingView     *croppingView;

@property (nonatomic, strong) ALResultPage              *page;
@property (nonatomic, strong) ALCordovaUIConfiguration *cordovaConfig;
@property (nonatomic, strong) UIImage              *testImage;

@end

@implementation ALDocumentCropViewController

- (nonnull instancetype)initWithPage:(nonnull ALResultPage *)page cordovaConfiguration:(ALCordovaUIConfiguration *)cordovaConfig {
    self = [super init];
    if (self) {
        self.page = page;
        self.cordovaConfig = cordovaConfig;
        // setup navigation bar
        //self.title = ALLocalizedString(@"Crop", @"Title of the cropping view controller");
        self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:ALLocalizedString(@"Cancel", @"left navigation button title in the crop view controller", self.cordovaConfig.languageKey) style:UIBarButtonItemStylePlain target:self action:@selector(dismissAction)];
        self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:ALLocalizedString(@"Done", @"right navigation button title in the crop view controller", self.cordovaConfig.languageKey) style:UIBarButtonItemStylePlain target:self action:@selector(completeCroppingAction)];
        
    }
    return self;
}

#pragma mark - Lifycycle

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // styling
    self.containerView.backgroundColor = [UIColor clearColor];
    self.pageImageView.backgroundColor = [UIColor clearColor];
    
    // set image
    UIImage *originalImage = self.page.originalImage;
    self.pageImageView.image = [UIImage imageWithCGImage:originalImage.CGImage scale:originalImage.scale orientation:UIImageOrientationUp];
    
    // setup cropping view
    self.croppingView.page = self.page;
    self.croppingView.relatedImageView = self.pageImageView;
    __weak __typeof(self) weakSelf = self;
    self.croppingView.croppingViewStateChangedHandler = ^(BOOL croppingAreaValid) {
        if (croppingAreaValid) {
            [weakSelf.navigationItem setRightBarButtonItem:[[UIBarButtonItem alloc] initWithTitle:ALLocalizedString(@"Done", @"right navigation button title in the crop view controller", self.cordovaConfig.languageKey) style:UIBarButtonItemStylePlain target:weakSelf action:@selector(completeCroppingAction)] animated:YES];
        } else {
            [weakSelf.navigationItem setRightBarButtonItem:nil animated:YES];
            
        }
    };
    
    // setup magnifying glass
    ALMagnifyingGlass *glass = [[ALMagnifyingGlass alloc] initWithFrame:CGRectMake(0, 0, 100, 100) targetView:self.pageImageView];
    [glass setMagnifyingFactor:2.0];
    glass.layer.cornerRadius = 50;
    glass.layer.borderWidth = 2.0;
    glass.layer.borderColor = [UIColor whiteColor].CGColor;
    
    self.croppingView.magnifyingGlass = glass;
}

#pragma mark - Button Actions

- (void)dismissAction {
    [self dismissViewControllerAnimated:YES completion:nil];
}

/**
 *  Saves the changes and dismiss the cropping view controller
 */
- (void)completeCroppingAction {
    self.page.imageCorners = [self.croppingView updatedImageCorners];
    
    [self dismissAction];
    
    if ([self.delegate respondsToSelector:@selector(cropViewControllerDidFinishCropping:withResult:)]) {
        [self.delegate cropViewControllerDidFinishCropping:self withResult:self.page];
    }
}

@end
