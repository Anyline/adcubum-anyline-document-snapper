//
//  UIImage+Transformations.h
//  Pods
//
//  Created by Milutin Tomic on 26/08/16.
//
//

#import <UIKit/UIKit.h>

@class ALRectangleFeature;
@interface UIImage (Transformations)

/**
 *  Returns the original image with a perspective correction applied to it
 *
 *  @param rectangleFeature - corners of the image
 *
 *  @return
 */
- (UIImage *)imageByCorrectingPerspectiveWithFeatures:(ALRectangleFeature *)rectangleFeature;

/**
 *  Rotates image clockwise by 90 degrees
 *
 *  @return
 */
- (UIImage *)imageByRotatingClockwise;

/**
 *  Returns the original image scaled by the passed factor
 *
 *  @param scale - scale factor
 *
 *  @return
 */
- (UIImage *)imageByScalingWithFactor:(CGFloat)scale;

@end
