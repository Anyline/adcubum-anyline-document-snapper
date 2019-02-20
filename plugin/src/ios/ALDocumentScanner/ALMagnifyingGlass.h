//
//  ALMagnifyingGlass.h
//  Pods
//
//  Created by Milutin Tomic on 26/08/16.
//
//

#import <UIKit/UIKit.h>

@interface ALMagnifyingGlass : UIView

/**
 *  The factor the target view needs to be scaled to.
 *
 *  Defaults to 1.5
 */
@property (nonatomic) CGFloat magnifyingFactor;

/**
 *  Initializes the magnifying glass view with the view that needs to be magnified
 *
 *  @param targetView - view to magnify
 *
 *  @return
 */
- (nonnull instancetype)initWithFrame:(CGRect)frame targetView:(nonnull UIView *)targetView;

/**
 *  Displays magnifying view
 *
 *  @param animated - enables/disables a fade animation
 */
- (void)showAnimated:(BOOL)animated;

/**
 *  Hides magnifying view
 *
 *  @param animated - enables/disables a fade animation
 */
- (void)dismissAnimated:(BOOL)animated;

/**
 *  Focuses the magnifying view to a point on the target view
 *
 *  @param focusPoint - point on target view to magnify
 */
- (void)magnify:(CGPoint)focusPoint;

/**
 *  Rerenders the input of the target image view
 */
- (void)refreshInput;

@end
