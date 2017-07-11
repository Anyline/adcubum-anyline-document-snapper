//
//  ALDocumentOverviewViewController.m
//  AnylineDocumentScanner
//
//  Created by Milutin Tomic on 16/08/16.
//  Copyright © 2016 9Y Media Group GmbH. All rights reserved.
//

#import "ALDocumentOverviewViewController.h"
#import "ALPagesCollectionViewLayout.h"
#import "ALResultDocument.h"
#import "ALPageCollectionViewCell.h"
#import "ALDocumentOverviewToolBar.h"
#import "UIImage+Transformations.h"
#import "ALDocumentCropViewController.h"
#import "ALLocalizationMacro.h"

static NSString * const kPageCollectionViewCellReuseIdentifier = @"ALPageCollectionViewCellID";

@interface ALDocumentOverviewViewController () <UICollectionViewDelegate, UICollectionViewDataSource,  UITextFieldDelegate, UIScrollViewDelegate, ALDocumentCropViewControllerDelegate>

@property (nonatomic, strong, nonnull) IBOutlet ALDocumentOverviewToolBar   *toolBar;
@property (weak, nonatomic) IBOutlet UILabel                                *numberOfPagesLabel;
@property (weak, nonatomic) IBOutlet UICollectionView                       *pagesCollectionView;
@property (weak, nonatomic) IBOutlet UIView                                 *toolbarContainer;

@property (nonatomic, strong) ALResultDocument                              *resultDocument;
@property (nonatomic, assign) id <ALDocumentOverviewViewControllerDelegate> delegate;
@property (nonatomic, strong) NSString                                      *anylineLicenseKey;
@property (nonatomic, strong) NSString                                      *documentSubject;

@property (nonatomic, strong) ALCordovaUIConfiguration *cordovaConfig;

@end


@implementation UINavigationController (RotationAll)
-(NSUInteger)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAll;
}
@end

@implementation ALDocumentOverviewViewController

#pragma mark - API

- (instancetype)initWithAnylineLicenseKey:(NSString *)licenseKey
                          documentSubject:(NSString *)subject
                                 delegate:(id)delegate
                     cordovaConfiguration:(ALCordovaUIConfiguration *)cordovaConfig {
    return [self initWithAnylineLicenseKey:licenseKey
                           documentSubject:subject
                                  delegate:delegate
                      cordovaConfiguration:cordovaConfig
                              scannedPages:nil];
}

- (instancetype)initWithAnylineLicenseKey:(NSString *)licenseKey
                          documentSubject:(NSString *)subject
                                 delegate:(id)delegate
                     cordovaConfiguration:(ALCordovaUIConfiguration *)cordovaConfig
                             scannedPages:(NSArray<ALResultPage *> *)scannedPages {
    if (!licenseKey) @throw [NSException exceptionWithName:NSInvalidArgumentException reason:@"License key must not be nil." userInfo:nil];
    
    self = [super init];
    if (self) {
        self.documentSubject = subject;
        self.delegate = delegate;
        self.anylineLicenseKey = licenseKey;
        self.cordovaConfig = cordovaConfig;
        
        if (scannedPages) {
            self.resultDocument = [ALResultDocument new];
            self.resultDocument.pages = scannedPages;
        }
        //self.title = ALLocalizedString(@"Scan", @"Title for document scanner VC");
    }
    
    return self;
}


- (NSUInteger)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAll;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations.
    //return (interfaceOrientation == UIInterfaceOrientationPortrait);
    return YES;
}

#pragma mark - Lifecycle

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // setup navigation bar
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:ALLocalizedString(@"Done", @"Done button in document scanner", self.cordovaConfig.languageKey) style:UIBarButtonItemStylePlain target:self action:@selector(finishScanAction)];
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:ALLocalizedString(@"Cancel", @"Cancel button in document scanner", self.cordovaConfig.languageKey) style:UIBarButtonItemStylePlain target:self action:@selector(cancelScanning:)];
    
    // we don't want to interfere with the collection view
    self.automaticallyAdjustsScrollViewInsets = NO;
    
    // this is the buffer where we will add in our result pages as they are created
    if (!self.resultDocument) {
        self.resultDocument = [ALResultDocument new];
    }
    
    // styling
    self.pagesCollectionView.backgroundColor = [UIColor clearColor];
    self.toolbarContainer.backgroundColor = [UIColor clearColor];
   
    // setup collection view
    self.pagesCollectionView.decelerationRate = UIScrollViewDecelerationRateFast;
    [self.pagesCollectionView setCollectionViewLayout:[ALPagesCollectionViewLayout new]];
    [self.pagesCollectionView registerNib:[UINib nibWithNibName:NSStringFromClass(ALPageCollectionViewCell.class) bundle:nil] forCellWithReuseIdentifier:kPageCollectionViewCellReuseIdentifier];
    
    // setup toolbar
    self.toolBar = [[NSBundle mainBundle] loadNibNamed:NSStringFromClass([ALDocumentOverviewToolBar class]) owner:self options:nil].firstObject;
    self.toolBar.translatesAutoresizingMaskIntoConstraints = NO;
    [self.toolbarContainer addSubview:self.toolBar];
    [self.toolbarContainer addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|[toolBar]|" options:0 metrics:nil views:@{@"toolBar" : self.toolBar}]];// full width
    [self.toolbarContainer addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|[toolBar]|" options:0 metrics:nil views:@{@"toolBar" : self.toolBar}]];// full height
    
    // have to do this to initialise the ui to our starting state
    [self _updateUI];
}

#pragma mark - Private

- (void)_updateUI {
    // updates the page label. If we have no pages, don't show anything
    NSInteger selectedPage = [self _selectedPageIndex];
    self.numberOfPagesLabel.text = self.resultDocument.pages.count == 0 ? @"" : [NSString stringWithFormat:@"%li/%lu", (long)selectedPage + 1, (unsigned long)self.resultDocument.pages.count];
}

/**
 *  Returns the index of the page currently selected (the page in the middle of the collection view
 *
 *  @return - returns page index or NSNotFound if no page currently selected
 */
- (NSInteger)_selectedPageIndex {
    NSIndexPath *indexPath = [self.pagesCollectionView indexPathForItemAtPoint:CGPointMake(self.pagesCollectionView.contentOffset.x + self.pagesCollectionView.frame.size.width/2,
                                                                                           self.pagesCollectionView.frame.size.height/2)];
    return indexPath ? indexPath.row : NSNotFound;
}


#pragma mark - ALDocumentCropViewControllerDelegate

- (void)cropViewControllerDidFinishCropping:(ALDocumentCropViewController *)cropVC withResult:(ALResultPage *)resultPage {
    // refresh page once crop is done
    NSInteger selectedPage = [self _selectedPageIndex];
    if (selectedPage == NSNotFound) return;
    
    // refresh collection view
    [self.pagesCollectionView reloadItemsAtIndexPaths:@[[NSIndexPath indexPathForItem:selectedPage inSection:0]]];
}

#pragma mark - UITextFieldDelegate

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
}

/**
 *  Called when the subject text is changed
 *
 *  @param sender
 */
- (IBAction)subjectDidChange:(id)sender {
    self.resultDocument.subject = ((UITextField *)sender).text;
}

#pragma mark - UICollectionViewDataSource

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView {
    return 1;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.resultDocument.pages.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    ALPageCollectionViewCell *cell = (ALPageCollectionViewCell *)[collectionView dequeueReusableCellWithReuseIdentifier:kPageCollectionViewCellReuseIdentifier forIndexPath:indexPath];
    
    ALResultPage *page = self.resultDocument.pages[indexPath.row];
    cell.page = page;
    
    return cell;
}

- (BOOL)collectionView:(UICollectionView *)collectionView shouldHighlightItemAtIndexPath:(NSIndexPath *)indexPath {
    return NO;
}

#pragma mark - UIScrollViewDelegate

/**
 *  Called once the collection view stopped scrolling
 */
- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView{
    [self _updateUI];
}

#pragma mark - Button Actions

/**
 *  Completes the scan process for the current document
 */
- (void)finishScanAction {
    if ([self.delegate respondsToSelector:@selector(documentScanner:didFinishScanWithResult:)]) {
        [self.delegate documentScanner:self didFinishScanWithResult:self.resultDocument];
    }
}

/**
 *  Opens up the document scanner allowing the user 
 *  to add new pages to the current document
 *
 *  @param sender
 */
- (IBAction)addNewPageToDocumentAction:(id)sender {
    [self.delegate documentScannerDeleteAllPages];
    [self dismissViewControllerAnimated:YES completion:nil];
}

/**
 *  Rotates currently selected/focused page by 90 degrees clockwise
 *
 *  @param sender
 */
- (IBAction)rotatePageAction:(id)sender {
    NSInteger selectedPage = [self _selectedPageIndex];
    
    // nothing to rotate
    if (selectedPage == NSNotFound) return;
    
    // rotate page
    [self.resultDocument.pages[selectedPage] rotatePageClockwise];
    
    // refresh collection view
    [self.pagesCollectionView reloadItemsAtIndexPaths:@[[NSIndexPath indexPathForItem:selectedPage inSection:0]]];
}

/**
 *  Opens up the cropping view controller with the currently selected page as input
 *
 *  @param sender
 */
- (IBAction)cropPageAction:(id)sender {
    NSInteger selectedPage = [self _selectedPageIndex];
    
    // nothing to crop
    if (selectedPage == NSNotFound) return;
    
    ALDocumentCropViewController *cropVC = [[ALDocumentCropViewController alloc] initWithPage:self.resultDocument.pages[selectedPage]
                                                                         cordovaConfiguration:self.cordovaConfig];
    cropVC.delegate = self;
    UINavigationController *navC = [[UINavigationController alloc] initWithRootViewController:cropVC];
    [navC.navigationBar setBarTintColor:self.navigationController.navigationBar.barTintColor];
    [navC.navigationBar setTranslucent:self.navigationController.navigationBar.translucent];
    
    [self presentViewController:navC animated:YES completion:nil];
}

/**
 *  Removes currently selected/focused page from the document
 *
 *  @param sender
 */
- (IBAction)deletePageAction:(id)sender {
    __weak __typeof(self) weakSelf = self;
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:ALLocalizedString(@"Are you sure?", @"Asks the user if he wants to delete an image (title)", self.cordovaConfig.languageKey)
                                                                         message:ALLocalizedString(@"Do you really want to remove this image?", @"Asks the user if he wants to delete an image (message)", self.cordovaConfig.languageKey)
                                                                  preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:ALLocalizedString(@"Cancel", @"Cancel deletion", self.cordovaConfig.languageKey) style:UIAlertActionStyleCancel handler:^(UIAlertAction *action) {
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:ALLocalizedString(@"Delete", @"Delete image", self.cordovaConfig.languageKey) style:UIAlertActionStyleDestructive handler:^(UIAlertAction *action) {
            NSInteger selectedPage = [weakSelf _selectedPageIndex];
            
            // nothing to delete
            if (selectedPage == NSNotFound) return;
            
            // update result document
            NSMutableArray *resultingArray = [NSMutableArray arrayWithArray:self.resultDocument.pages];
            [resultingArray removeObjectAtIndex:selectedPage];
            weakSelf.resultDocument.pages = [resultingArray copy];
            
            // update UI
            [weakSelf.pagesCollectionView deleteItemsAtIndexPaths:@[[NSIndexPath indexPathForItem:selectedPage inSection:0]]];
            [weakSelf _updateUI];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
    
}




- (IBAction)cancelScanning:(id)sender {
    __weak __typeof(self) weakSelf = self;
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:ALLocalizedString(@"Are you sure?", @"Asks the user if he wants to cancel scanning", self.cordovaConfig.languageKey)
                                                                         message:ALLocalizedString(@"Are you sure you want to exit? Every Scan will be deleted!", @"Asks the user if he wants to cancel scanning (message)", self.cordovaConfig.languageKey)
                                                                  preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:ALLocalizedString(@"Continue scanning", @"Continue scanning", self.cordovaConfig.languageKey) style:UIAlertActionStyleCancel handler:^(UIAlertAction *action) {
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:ALLocalizedString(@"Cancel", @"Cancel scanning", self.cordovaConfig.languageKey) style:UIAlertActionStyleDestructive handler:^(UIAlertAction *action) {
        [weakSelf dismissViewControllerAnimated:YES completion:^{
            [weakSelf.delegate documentScannerDidAbort:self];
        }];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}

@end
