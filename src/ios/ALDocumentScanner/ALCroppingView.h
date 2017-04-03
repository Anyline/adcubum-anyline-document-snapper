//
//  ALCroppingView.h
//  Pods
//
//  Created by Milutin Tomic on 28/08/16.
//
//

#import <UIKit/UIKit.h>

@class ALResultPage, ALRectangleFeature, ALMagnifyingGlass;

typedef void(^ALCroppingViewStateChangedHandler)(BOOL croppingAreaValid);

@interface ALCroppingView : UIView

@property (nonatomic, strong, nonnull) ALResultPage                         *page;
@property (nonatomic, strong, nullable) ALMagnifyingGlass                   *magnifyingGlass;
@property (nonatomic, copy, nullable) ALCroppingViewStateChangedHandler     croppingViewStateChangedHandler;
@property (weak, nonatomic, nullable) UIImageView                           *relatedImageView;

/**
 *  Returns the updates image corners
 *
 *  @return
 */
- (nonnull ALRectangleFeature *)updatedImageCorners;

@end
