//
//  ALCroppingView.m
//  AnylineDocumentScanner
//
//  Created by Milutin Tomic on 28/08/16.
//  Copyright © 2016 9Y Media Group GmbH. All rights reserved.
//

#import "ALCroppingView.h"
#import "ALRectangleFeature.h"
#import "ALMagnifyingGlass.h"
#import "ALResultPage.h"

#import "UIImageView+GeometryConversion.h"

static CGFloat const kDraggableViewWidth =                      30;
static CGFloat const kPaddingAroundDragableArea =               10;
static CGFloat const kMagnifyingGlassInset =                    20;
static CGFloat const kDraggablePointsFallbackInset =            (kDraggableViewWidth + kPaddingAroundDragableArea) / 2.0;// puts the corners incl. their drag zones into the image
static CGFloat const kMagnifyingGlassRadius =                   50;
static NSTimeInterval const kMagnifierFadeAnimationDuration =   0.3;

typedef NS_ENUM(NSInteger, MagnifierPosition) {
    MagnifierPositionLeft,
    MagnifierPositionRight,
};

@interface ALCroppingView ()

@property (nonatomic, strong) UIView                            *topLeftView;
@property (nonatomic, strong) UIView                            *topRightView;
@property (nonatomic, strong) UIView                            *bottomLeftView;
@property (nonatomic, strong) UIView                            *bottomRightView;

@property (nonatomic, strong) UIView                            *currentlyDraggedView;
@property (nonatomic, strong) NSArray                           *dragableViews;
@property (nonatomic, assign) BOOL                              initialCroppingAreaChanged;
@property (nonatomic, strong) NSLayoutConstraint                *magnifierLeadingConstraint;
@property (nonatomic, strong) NSLayoutConstraint                *magnifierTrailingConstraint;

@property (nonatomic, assign) MagnifierPosition                 magnifierPosition;

@end

@implementation ALCroppingView

#pragma mark - Lifecycle

- (void)layoutSubviews {
    [super layoutSubviews];
    
    [self _setupDragableViews];
    [self _positionDraggableViewsAtImageCorners];
}

#pragma mark - API

- (ALRectangleFeature *)updatedImageCorners {
    ALRectangleFeature *updatedCorners = [ALRectangleFeature new];
    updatedCorners.topLeft = [self _convertPoint:self.topLeftView.center fromViewToImage:self.relatedImageView];
    updatedCorners.topRight = [self _convertPoint:self.topRightView.center fromViewToImage:self.relatedImageView];
    updatedCorners.bottomLeft = [self _convertPoint:self.bottomLeftView.center fromViewToImage:self.relatedImageView];
    updatedCorners.bottomRight = [self _convertPoint:self.bottomRightView.center fromViewToImage:self.relatedImageView];
    
    return updatedCorners;
}

#pragma mark - Overrides

- (void)drawRect:(CGRect)rect {
    ALResultPage *page = self.page;
    if (!page) return;
    
    UIColor *fillColor;
    UIColor *strokeColor;
    if ([self _isCroppingAreaConvex]) {// all good
        if (self.croppingViewStateChangedHandler) self.croppingViewStateChangedHandler(YES);
        fillColor = [UIColor colorWithRed: 0.1887 green: 0.2281 blue: 0.6502 alpha: 0.33];
        strokeColor = [UIColor colorWithRed: 0.1887 green: 0.2281 blue: 0.6502 alpha: 1.0];
    } else {// cropping area is in an invalid state
        if (self.croppingViewStateChangedHandler) self.croppingViewStateChangedHandler(NO);
        fillColor = [UIColor colorWithRed: 0.9865 green: 0.1331 blue: 0.4318 alpha: 0.33];
        strokeColor = [UIColor colorWithRed: 0.9865 green: 0.1331 blue: 0.4318 alpha: 1.0];
    }
    
    // draw canvas
    UIBezierPath *bezierPath = [UIBezierPath bezierPath];
    [bezierPath moveToPoint:self.topLeftView.center];
    [bezierPath addLineToPoint:self.topRightView.center];
    [bezierPath addLineToPoint:self.bottomRightView.center];
    [bezierPath addLineToPoint:self.bottomLeftView.center];
    [bezierPath addLineToPoint:self.topLeftView.center];
    
    [fillColor setFill];
    [bezierPath fill];
    [strokeColor setStroke];
    bezierPath.lineWidth = 1;
    [bezierPath stroke];
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    UITouch *touch = [touches anyObject];
    
    // check if any of the dragable views is touched
    NSArray *dragableViews = @[self.topLeftView, self.topRightView, self.bottomLeftView, self.bottomRightView];
    for (UIView *view in dragableViews) {
        if (CGRectContainsPoint(CGRectInset(view.frame, -kPaddingAroundDragableArea, -kPaddingAroundDragableArea), [touch locationInView:self])) {
            self.currentlyDraggedView = view;
            
            // show magnifier
            if (self.magnifyingGlass) {
                [self.magnifyingGlass refreshInput];
                [self.magnifyingGlass showAnimated:YES];
            }
            
            return;
        }
    }
    
    self.currentlyDraggedView = nil;
}

- (void)touchesMoved:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    if (self.currentlyDraggedView) {// if a view is beeing drag
        self.initialCroppingAreaChanged = YES;
        
        UITouch *touch = [touches anyObject];
        CGPoint currentPosition = [touch locationInView:self];
        
        // check if position valid
        if ([self _positionIsValid:currentPosition forView:self.currentlyDraggedView] &&
            ![self _doesViewIntersectWithOtherDragableViews:self.currentlyDraggedView withProposedCenter:currentPosition] &&
            !CGPointEqualToPoint([self _convertPoint:currentPosition fromViewToImage:self.relatedImageView], CGPointZero)) {
            self.currentlyDraggedView.center = currentPosition;
            [self setNeedsDisplay];
            
            // update magnifier
            if (self.magnifyingGlass) {
                [self.magnifyingGlass magnify:currentPosition];
                [self _positionMagnifierToAvoidView:self.currentlyDraggedView];
            }
        }
    }
}

- (void)touchesEnded:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    if (self.magnifyingGlass) {
        [self.magnifyingGlass dismissAnimated:YES];
    }
}

#pragma mark - CA

- (void)setMagnifierPosition:(MagnifierPosition)magnifierPosition {
    _magnifierPosition = magnifierPosition;
    
    // first remove the old ones
    [self removeConstraints:@[self.magnifierTrailingConstraint, self.magnifierLeadingConstraint]];
    
    // now add the one we need
    switch (magnifierPosition) {
        case MagnifierPositionLeft: {
            [self addConstraint:self.magnifierLeadingConstraint];
        } break;
            
        case MagnifierPositionRight: {
            [self addConstraint:self.magnifierTrailingConstraint];
        } break;
    }
}

- (void)setMagnifyingGlass:(ALMagnifyingGlass *)magnifyingGlass {
    _magnifyingGlass = magnifyingGlass;
    
    // add magnifier to view
    [self addSubview:magnifyingGlass];
    
    // setup constraints, but don't attach them to the view yet
    magnifyingGlass.translatesAutoresizingMaskIntoConstraints = NO;
    self.magnifierLeadingConstraint = [NSLayoutConstraint constraintWithItem:magnifyingGlass attribute:NSLayoutAttributeLeading relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeLeading multiplier:1 constant:kMagnifyingGlassInset];
    self.magnifierTrailingConstraint = [NSLayoutConstraint constraintWithItem:magnifyingGlass attribute:NSLayoutAttributeTrailing relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeTrailing multiplier:1 constant:-kMagnifyingGlassInset];
    
    [self addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:[magnifyingGlass(==magnifyingGlassWidth)]" options:0 metrics:@{@"magnifyingGlassWidth": @(kMagnifyingGlassRadius * 2.0)} views:NSDictionaryOfVariableBindings(magnifyingGlass)]];// width
    [self addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|-magnifyingGlassInset-[magnifyingGlass(==magnifyingGlassHeight)]" options:0 metrics:@{@"magnifyingGlassInset": @(kMagnifyingGlassInset), @"magnifyingGlassHeight": @(kMagnifyingGlassRadius * 2.0)} views:NSDictionaryOfVariableBindings(magnifyingGlass)]];// top constraint
    
    // initialise it at least once
    self.magnifierPosition = MagnifierPositionLeft;
}

- (NSArray *)dragableViews {
    if (!_dragableViews) {
        _dragableViews = @[self.topLeftView, self.topRightView, self.bottomRightView, self.bottomLeftView];
    }
    
    return _dragableViews;
}

#pragma mark - Private
#pragma mark magnifying glass

/**
 *  Animates magnifying glass position to avoid the currently dragged view
 */
- (void)_positionMagnifierToAvoidView:(UIView *)view {
    MagnifierPosition newPosition;
    if (CGRectContainsPoint(CGRectMake(0,
                                       0,
                                       self.magnifyingGlass.frame.size.width + kMagnifyingGlassInset,
                                       self.magnifyingGlass.frame.size.height + kMagnifyingGlassInset), view.center)) {
        newPosition = MagnifierPositionRight;
    } else {
        newPosition = MagnifierPositionLeft;
    }
    
    [self layoutIfNeeded];
    __weak __typeof(self) weakSelf = self;
    [UIView animateWithDuration:kMagnifierFadeAnimationDuration delay:0 options:UIViewAnimationOptionBeginFromCurrentState animations:^{
        weakSelf.magnifierPosition = newPosition;
        [weakSelf layoutIfNeeded];
    } completion:nil];
}

#pragma mark cropping points validation

/**
 *  Checks if a draggable view colides with any of the other dragable views
 *
 *  @param view - dragable view
 *
 *  @return
 */
- (BOOL)_doesViewIntersectWithOtherDragableViews:(UIView *)view withProposedCenter:(CGPoint)proposedCenter{
    for (UIView *otherView in self.dragableViews) {
        if (otherView != view && [self _doesView:view intersectWithView:otherView withProposedCenter:proposedCenter]) {
            return YES;
        }
    }
    
    // if we got here it means we couldn't find anything
    return NO;
}

/**
 *  Checks if two of the dragable views colide with one another, calculated as a circular collider. The view must be square (width==height).
 *
 *  @param view1 - first view
 *  @param view2 - second view
 *
 *  @return
 */
- (BOOL)_doesView:(UIView *)view1 intersectWithView:(UIView *)view2 withProposedCenter:(CGPoint)proposedCenter{
    CGFloat r1 = view1.frame.size.width/2.0;
    CGFloat r2 = view2.frame.size.width/2.0;
    CGFloat x1 = proposedCenter.x;
    CGFloat y1 = proposedCenter.y;
    CGFloat x2 = view2.center.x;
    CGFloat y2 = view2.center.y;
    
    if (powf((r1 - r2), 2) <= powf((x1 - x2), 2) + powf((y1 - y2), 2) &&
        powf((x1 - x2), 2) + powf((y1 - y2), 2) <= powf((r1 + r2), 2)) {
        return YES;
    }else{
        return NO;
    }
}

/**
 *  Returns the right neighbour view of the passed dragable view
 *
 *  @param view - view that need the right neighbour
 *
 *  @return
 */
- (UIView *)_rightNeighbourForView:(UIView *)view {
    NSUInteger viewIndex = [self.dragableViews indexOfObject:view];
    NSInteger rightNeighbourIndex = viewIndex == 0 ? self.dragableViews.count - 1 : viewIndex - 1;
    return self.dragableViews[rightNeighbourIndex];
}

/**
 *  Returns the left neighbour view of the passed dragable view
 *
 *  @param view - view that need the left neighbour
 *
 *  @return
 */
- (UIView *)_leftNeighbourForView:(UIView *)view {
    NSUInteger viewIndex = [self.dragableViews indexOfObject:view];
    NSInteger leftNeighbourIndex = (viewIndex + 1) % self.dragableViews.count;
    return self.dragableViews[leftNeighbourIndex];
}

/**
 *  Returns the opposing neighbour view of the passed dragable view
 *
 *  @param view - view that need the opposing neighbour
 *
 *  @return
 */
- (UIView *)_opposingNeighbourForView:(UIView *)view {
    NSUInteger viewIndex = [self.dragableViews indexOfObject:view];
    NSInteger opposingNeighbourIndex = (viewIndex + 2) % self.dragableViews.count;
    return self.dragableViews[opposingNeighbourIndex];
}

/**
 *  Checks if the proposed position for the passed view is a valid one
 *
 *  @param position - position to check for validity
 *  @param view     - view that needs the position validated
 *
 *  @return
 */
- (BOOL)_positionIsValid:(CGPoint)position forView:(UIView *)view {
    CGPoint leftPoint = [self _leftNeighbourForView:view].center;
    CGPoint rightPoint = [self _rightNeighbourForView:view].center;
    CGPoint oposingPoint = [self _opposingNeighbourForView:view].center;
    
    CGVector leftEdgeVector = CGVectorMake(leftPoint.x - oposingPoint.x, leftPoint.y - oposingPoint.y);
    CGVector rightEdgeVector = CGVectorMake(rightPoint.x - oposingPoint.x, rightPoint.y - oposingPoint.y);
    CGVector checkEdgeVector = CGVectorMake(position.x - oposingPoint.x, position.y - oposingPoint.y);
    
    if ([self _signOfCrossProductOfVector:leftEdgeVector withVector:rightEdgeVector] == [self _signOfCrossProductOfVector:leftEdgeVector withVector:checkEdgeVector] &&
        [self _signOfCrossProductOfVector:leftEdgeVector withVector:rightEdgeVector] * -1.0 == [self _signOfCrossProductOfVector:rightEdgeVector withVector:checkEdgeVector]) {
        return YES;
    }
    
    return NO;
}

/**
 *  Returns 1 if cross product is positive, -1 if it is negative, and 0 if the cross product is zero
 *
 *  @param vector1 - first vector
 *  @param vector2 - second vector
 *
 *  @return
 */
- (NSInteger)_signOfCrossProductOfVector:(CGVector)vector1 withVector:(CGVector)vector2 {
    CGFloat crossProduct = vector1.dx * vector2.dy - vector1.dy * vector2.dx;
    
    if (crossProduct > 0) {
        return 1;
    }else if (crossProduct < 0){
        return -1;
    }else{
        return 0;
    }
}

/**
 *  Checks if the cropping area/polygon is convex
 *
 *  @return
 */
- (BOOL)_isCroppingAreaConvex {
    NSInteger sign = 0;
    
    // calculate all edge vectors in polygon
    NSMutableArray *vectors = [NSMutableArray new];
    for (NSInteger viewIndex = 0; viewIndex < self.dragableViews.count; viewIndex++) {
        CGPoint nextViewCenter = [self.dragableViews[(viewIndex + 1) % self.dragableViews.count] center];
        CGPoint currentViewCenter = [self.dragableViews[viewIndex] center];
        [vectors addObject:[NSValue valueWithCGVector:CGVectorMake(nextViewCenter.x - currentViewCenter.x, nextViewCenter.y - currentViewCenter.y)]];
    }
    
    // check for convexity
    for (int vectorIndex = 0; vectorIndex < self.dragableViews.count; vectorIndex++) {
        CGVector currentVector = [vectors[vectorIndex] CGVectorValue];
        CGVector nextVector = [vectors[(vectorIndex + 1) % self.dragableViews.count] CGVectorValue];
        
        NSInteger currentSign = [self _signOfCrossProductOfVector:currentVector withVector:nextVector];
        
        if (vectorIndex == 0) {
            sign = currentSign;
            continue;
        }
        
        if (currentSign != sign) {
            return NO;
        }
    }
    
    return YES;
}

#pragma mark image/crop view point conversion

- (CGPoint)_convertPoint:(CGPoint)imageViewPoint fromViewToImage:(UIImageView *)imageView {
    return [imageView convertPointFromView:imageViewPoint];
}

- (CGPoint)_convertPoint:(CGPoint)pixelPoint fromImageToView:(UIImageView *)imageView {
    return [imageView convertPointFromImage:pixelPoint];
}

#pragma mark cropping point views

/**
 *  Creates dragable views and adds them to the superview
 */
- (void)_setupDragableViews {
    // return if views already initialised
    if (self.topLeftView) return;
    
    // create views
    self.topLeftView = [UIView new];
    self.topRightView = [UIView new];
    self.bottomLeftView = [UIView new];
    self.bottomRightView = [UIView new];
    
    // style views
    for (UIView *v in @[self.topRightView, self.topLeftView, self.bottomRightView, self.bottomLeftView]) {
        v.frame = CGRectMake(0, 0, kDraggableViewWidth, kDraggableViewWidth);
        v.backgroundColor = [UIColor whiteColor];
        v.layer.cornerRadius = kDraggableViewWidth / 2;
        v.clipsToBounds = YES;
        [self addSubview:v];
    }
}

- (void)_positionDraggableViewsAtImageCorners {
    if (self.initialCroppingAreaChanged) return;
    
    ALRectangleFeature *corners = self.page.imageCorners;
    CGSize imageSize = self.relatedImageView.image.size;
    
    
    // translate points to our view
    CGPoint topLeft = [self _convertPoint:corners.topLeft fromImageToView:self.relatedImageView];
    CGPoint topRight = [self _convertPoint:corners.topRight fromImageToView:self.relatedImageView];
    CGPoint bottomLeft = [self _convertPoint:corners.bottomLeft fromImageToView:self.relatedImageView];
    CGPoint bottomRight = [self _convertPoint:corners.bottomRight fromImageToView:self.relatedImageView];
    
    // if it's the corner points
    if ([self.class _isRectangleFeatureOnCorners:corners forImageSize:imageSize]) {
        // NSLog(@"shifting");
        
        // then shift them in by our amount
        topLeft = CGPointMake(topLeft.x + kDraggablePointsFallbackInset, topLeft.y + kDraggablePointsFallbackInset);
        topRight = CGPointMake(topRight.x - kDraggablePointsFallbackInset, topRight.y + kDraggablePointsFallbackInset);
        bottomLeft = CGPointMake(bottomLeft.x + kDraggablePointsFallbackInset, bottomLeft.y - kDraggablePointsFallbackInset);
        bottomRight = CGPointMake(bottomRight.x - kDraggablePointsFallbackInset, bottomRight.y - kDraggablePointsFallbackInset);
    }

    // move our points in the view
    self.topLeftView.center = topLeft;
    self.topRightView.center = topRight;
    self.bottomLeftView.center = bottomLeft;
    self.bottomRightView.center = bottomRight;
}

+ (BOOL)_isRectangleFeatureOnCorners:(ALRectangleFeature *)corners forImageSize:(CGSize)imageSize {
    return (CGPointEqualToPoint(corners.topLeft, CGPointMake(0, 0)) &&
            CGPointEqualToPoint(corners.topRight, CGPointMake(imageSize.width, 0)) &&
            CGPointEqualToPoint(corners.bottomLeft, CGPointMake(0, imageSize.height)) &&
            CGPointEqualToPoint(corners.bottomRight, CGPointMake(imageSize.width, imageSize.height)));
}

@end
