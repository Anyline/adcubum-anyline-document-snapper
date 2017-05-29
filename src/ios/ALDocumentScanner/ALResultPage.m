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
@property (nonatomic, copy, nullable) NSString                                  *correctedImagePath;
@end

@implementation ALResultPage

#pragma mark - CA



- (UIImage *)originalImage {
    NSData * data  = [NSData dataWithContentsOfFile:self.imagePath];
    UIImage * orig = [UIImage imageWithData:data];
    return orig;
}

- (UIImage *)ocrOptimisedImage {
    return [self.originalImage imageByCorrectingPerspectiveWithFeatures:self.imageCorners];
}

- (void)setImageCorners:(ALRectangleFeature *)imageCorners {
    _imageCorners = imageCorners;
    
    if(self.imageKey) { // is setup complete?
        [self _updateOCRImages];
    }
}

- (void)_updateOCRImages {
    self.thumbnail = [self.ocrOptimisedImage imageByScalingWithFactor:0.25];

    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    NSString *cacheDirectory = [paths objectAtIndex:0];

    { // store corrected image
        UIImage * docImage = [self.originalImage imageByCorrectingPerspectiveWithFeatures:self.imageCorners];
        NSData *data = UIImageJPEGRepresentation(docImage, 1);
        self.correctedImagePath = [cacheDirectory stringByAppendingFormat:@"/%@-corrected.png", self.imageKey];
        [data writeToFile:self.correctedImagePath atomically:YES];
    }
}

#pragma mark - Life

- (instancetype)initWithOriginalImage:(UIImage *)originalImage imageCorners:(ALSquare *)imageCorners {
    if (self = [super init]) {
        // set image corners
        
        ALRectangleFeature *corners = [ALRectangleFeature new];
        corners.topLeft = imageCorners.upLeft;
        corners.topRight = imageCorners.upRight;
        corners.bottomLeft = imageCorners.downLeft;
        corners.bottomRight = imageCorners.downRight;
        self.imageCorners = corners;
        
        NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
        NSString *cacheDirectory = [paths objectAtIndex:0];
        self.imageKey = [[NSProcessInfo processInfo] globallyUniqueString];
    
        // store original image
        NSData *data = UIImageJPEGRepresentation(originalImage, 1);
        self.imagePath = [cacheDirectory stringByAppendingFormat:@"/%@.png", self.imageKey];
        [data writeToFile:self.imagePath atomically:YES];
        
        [self _updateOriginalImage:originalImage];
        [self _updateOCRImages];
    }
    return self;
}

- (NSString *)originalImagePath; {
    return self.imagePath;
}

- (NSString *)ocrOptimisedImagePath; {
    return self.correctedImagePath;
}

- (void)_updateOriginalImage:(UIImage*)originalImage; {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    NSString *cacheDirectory = [paths objectAtIndex:0];
    self.imageKey = [[NSProcessInfo processInfo] globallyUniqueString];
    
    // store original image
    NSData *data = UIImageJPEGRepresentation(originalImage, 1);
    self.imagePath = [cacheDirectory stringByAppendingFormat:@"/%@.png", self.imageKey];
    [data writeToFile:self.imagePath atomically:YES];
}

- (void)rotatePageClockwise {
    UIImage * rotatedImage = [self.originalImage imageByRotatingClockwise];
    [self _updateOriginalImage:rotatedImage];
    [self _updateOCRImages];
}

@end
