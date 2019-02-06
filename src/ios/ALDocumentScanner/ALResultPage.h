//
//  ALResultPage.h
//  AnylineDocumentScanner
//
//  Created by Milutin Tomic on 17/08/16.
//  Copyright © 2016 9Y Media Group GmbH. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "ALRectangleFeature.h"

@class ALSquare, ALResultPage;

@interface ALResultPage : NSObject

@property (nonatomic, strong, nullable) UIImage              *originalImage;
@property (nonatomic, strong, nullable) UIImage               *thumbnail;
@property (nonatomic, strong, nullable) UIImage               *ocrOptimisedImage;

@property (nonatomic, strong, nullable, readwrite) ALRectangleFeature   *imageCorners;

@end

@interface ALResultPage (Private)

- (nullable instancetype)initWithOriginalImage:(UIImage * _Nullable)originalImage
                                  imageCorners:(ALSquare * _Nullable)imageCorners;

- (nullable instancetype)initWithOriginalImage:(UIImage * _Nullable)originalImage
                              transformedImage:(UIImage * _Nullable)transformedImage
                                  imageCorners:(ALSquare * _Nullable)imageCorners;

/**
 *  Rotates the page's original image by 90 degrees clockwise
 */
- (void)rotatePageClockwise;

- (NSString *_Nullable)originalImagePath;

- (NSString *_Nullable)ocrOptimisedImagePath;

@end
