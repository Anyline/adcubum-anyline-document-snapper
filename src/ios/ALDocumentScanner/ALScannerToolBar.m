//
//  ALScannerToolBar.m
//  AnylineDocumentScanner
//
//  Created by Milutin Tomic on 18/08/16.
//  Copyright © 2016 9Y Media Group GmbH. All rights reserved.
//

#import "ALScannerToolBar.h"

@interface ALScannerToolBar ()

@property (weak, nonatomic) IBOutlet UILabel *numberOfPagesLabel;
@property (weak, nonatomic) IBOutlet UIImageView *lastScanImageView;

@end

@implementation ALScannerToolBar

#pragma mark - ALScannerToolBarInterface

- (void)setNumberOfScannedPages:(NSInteger)numberOfPages{
    self.numberOfPagesLabel.text = numberOfPages == 0 ? @"" : [NSString stringWithFormat:@"%ld", (long)numberOfPages];
}

- (void)setLastScanThumbnail:(UIImage *)thumbnail{
    self.lastScanImageView.image = thumbnail;
}

@end
