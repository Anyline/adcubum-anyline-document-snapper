//
//  ALResultPage.m
//  AnylineDocumentScanner
//
//  Created by Milutin Tomic on 17/08/16.
//  Copyright © 2016 9Y Media Group GmbH. All rights reserved.
//

#import "ALResultPage.h"
#import "UIImage+Transformations.h"
#import "ALRectangleFeature.h"
#import <Anyline/ALSquare.h>

#pragma mark - ALResultPage

@interface ALResultPage ()
@property (nonatomic, copy, nullable) NSString                                  *imageKey;
@property (nonatomic, copy, nullable) NSString                                  *imagePath;
@property (nonatomic, copy, nullable) NSString                                  *ocrOptimisedImagePath;
@end

@implementation ALResultPage

#pragma mark - CA

- (void)setImageCorners:(ALRectangleFeature *)imageCorners {
    _imageCorners = imageCorners;
    
    if(self.imageKey) { // is setup complete?
        self.ocrOptimisedImage = [self.originalImage imageByCorrectingPerspectiveWithFeatures:self.imageCorners];
    }
}

- (void)setOcrOptimisedImage:(UIImage *)ocrOptimisedImage {
    _ocrOptimisedImage = ocrOptimisedImage;
    
    self.thumbnail = [ocrOptimisedImage imageByScalingWithFactor:0.25];

    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    NSString *cacheDirectory = [paths objectAtIndex:0];

    { // store corrected image
        NSData *data = UIImageJPEGRepresentation(ocrOptimisedImage, 1);
        self.ocrOptimisedImagePath = [cacheDirectory stringByAppendingFormat:@"/%@-corrected.jpg", self.imageKey];
        [data writeToFile:self.ocrOptimisedImagePath atomically:YES];
    }
}

#pragma mark - Life

- (nullable instancetype)initWithOriginalImage:(UIImage * _Nullable)originalImage
                                  imageCorners:(ALSquare * _Nullable)imageCorners {
    ALRectangleFeature *corners = [ALRectangleFeature new];
    corners.topLeft = imageCorners.upLeft;
    corners.topRight = imageCorners.upRight;
    corners.bottomLeft = imageCorners.downLeft;
    corners.bottomRight = imageCorners.downRight;
    return [self initWithOriginalImage:originalImage
                      transformedImage:[originalImage imageByCorrectingPerspectiveWithFeatures:corners]
                          imageCorners:imageCorners];
}

- (instancetype)initWithOriginalImage:(UIImage *)originalImage
                     transformedImage:(UIImage *)transformedImage
                         imageCorners:(ALSquare *)imageCorners {
    if (self = [super init]) {
        // set image corners
        
        ALRectangleFeature *corners = [ALRectangleFeature new];
        corners.topLeft = imageCorners.upLeft;
        corners.topRight = imageCorners.upRight;
        corners.bottomLeft = imageCorners.downLeft;
        corners.bottomRight = imageCorners.downRight;
        self.imageCorners = corners;
        
        self.originalImage = originalImage;
        self.ocrOptimisedImage = transformedImage;
    }
    return self;
}

- (NSString *)originalImagePath; {
    return self.imagePath;
}

- (void)setOriginalImage:(UIImage *)originalImage {
    _originalImage = originalImage;
    
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    NSString *cacheDirectory = [paths objectAtIndex:0];
    self.imageKey = [[NSProcessInfo processInfo] globallyUniqueString];
    
    // store original image
    NSData *data = UIImageJPEGRepresentation(originalImage, 1);
    self.imagePath = [cacheDirectory stringByAppendingFormat:@"/%@.jpg", self.imageKey];
    [data writeToFile:self.imagePath atomically:YES];
}

- (void)rotatePageClockwise {
    UIImage * rotatedImage = [self.originalImage imageByRotatingClockwise];
    self.originalImage = rotatedImage;
    
    UIImage * rotatedOCRImage = [self.ocrOptimisedImage imageByRotatingClockwise];
    self.ocrOptimisedImage = rotatedOCRImage;
}

@end
