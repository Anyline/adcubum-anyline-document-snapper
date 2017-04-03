//
//  UIImage+Transformations.m
//  Pods
//
//  Created by Milutin Tomic on 26/08/16.
//
//

#import "UIImage+Transformations.h"
#import "ALRectangleFeature.h"

@implementation UIImage (Transformations)

- (UIImage *)imageByCorrectingPerspectiveWithFeatures:(ALRectangleFeature *)rectangleFeature {
    // create input params
    NSDictionary *rectangleCoordinates = @{
        @"inputTopLeft": [CIVector vectorWithCGPoint:[self _convertCornerCoordinatesToCoreImageCoordinateSpace:rectangleFeature.topLeft]],
        @"inputTopRight": [CIVector vectorWithCGPoint:[self _convertCornerCoordinatesToCoreImageCoordinateSpace:rectangleFeature.topRight]],
        @"inputBottomLeft": [CIVector vectorWithCGPoint:[self _convertCornerCoordinatesToCoreImageCoordinateSpace:rectangleFeature.bottomLeft]],
        @"inputBottomRight": [CIVector vectorWithCGPoint:[self _convertCornerCoordinatesToCoreImageCoordinateSpace:rectangleFeature.bottomRight]],
    };
    
    // apply transformation
    CIImage *originalImage = [CIImage imageWithCGImage:self.CGImage];
    CIImage *transformedImage = [originalImage imageByApplyingFilter:@"CIPerspectiveCorrection" withInputParameters:rectangleCoordinates];

    // create cgimage from the transformed image
    CIContext *context = [CIContext contextWithOptions:nil];
    CGImageRef ref = [context createCGImage:transformedImage fromRect:transformedImage.extent];
    
    // create new UIImage from CGImage
    UIImage *uiImage = [UIImage imageWithCGImage:ref scale:self.scale orientation:self.imageOrientation];
    
    // release the CGImageRef
    CGImageRelease(ref);
    
    return uiImage;
}

- (UIImage *)imageByRotatingClockwise {
    UIImageOrientation newOrientation;
    switch (self.imageOrientation) {
        case UIImageOrientationUp:
            newOrientation = UIImageOrientationRight;
            break;
            
        case UIImageOrientationRight:
            newOrientation = UIImageOrientationDown;
            break;
            
        case UIImageOrientationDown:
            newOrientation = UIImageOrientationLeft;
            break;
            
        case UIImageOrientationLeft:
            newOrientation = UIImageOrientationUp;
            break;
            
        default:
            newOrientation = UIImageOrientationUp;
            break;
    }
    
    return [UIImage imageWithCGImage:self.CGImage scale:self.scale orientation:newOrientation];
}

- (UIImage *)imageByScalingWithFactor:(CGFloat)scale {
    CGSize newSize = CGSizeApplyAffineTransform(self.size, CGAffineTransformMakeScale(scale, scale));
    
    UIGraphicsBeginImageContextWithOptions(newSize, YES, self.scale);
    [self drawInRect:CGRectMake(0, 0, newSize.width, newSize.height)];
    UIImage *scaledImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return scaledImage;
}

#pragma mark - Private

/**
 *  Converts the point from the UIKit coordinate space (origin is top left corner) to the CoreImage coordinate space (origin is bottom left corner)
 *
 *  @param point         - point to convert
 *
 *  @return
 */
- (CGPoint)_convertCornerCoordinatesToCoreImageCoordinateSpace:(CGPoint)point {
    if (self.imageOrientation == UIImageOrientationUp || self.imageOrientation == UIImageOrientationDown) {
        return CGPointMake(point.x, self.size.height - point.y);
    }else{
        return CGPointMake(point.x, self.size.width - point.y);
    }
}

@end
