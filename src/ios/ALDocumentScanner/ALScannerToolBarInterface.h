//
//  ALScannerToolBarInterface.h
//  AnylineDocumentScanner
//
//  Created by Milutin Tomic on 18/08/16.
//  Copyright © 2016 9Y Media Group GmbH. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@protocol ALScannerToolBarInterface <NSObject>

/**
 *  Sets the number of scanned pages to be shown in the scanner tool bar
 *
 *  @param numberOfPages - number of scanned pages
 */
- (void)setNumberOfScannedPages:(NSInteger)numberOfPages;

/**
 *  Sets the thumbnail to be shown in the scanner tool bar
 *
 *  @param thumbnail - thumbnail
 */
- (void)setLastScanThumbnail:(UIImage*)thumbnail;

@end
