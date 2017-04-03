//
//  ALMagnifyingGlass.m
//  AnylineDocumentScanner
//
//  Created by Milutin Tomic on 26/08/16.
//  Copyright © 2016 9Y Media Group GmbH. All rights reserved.
//

#import "ALMagnifyingGlass.h"

static NSTimeInterval const kFadeAnimationSpeed =   0.2;
static CGFloat const kDefaultMagnifierFactor =      1.5;

@interface ALMagnifyingGlass ()

@property (nonatomic, strong) UIView        *targetView;
@property (nonatomic, strong) UIImageView   *magnifyingImageView;

@end

@implementation ALMagnifyingGlass

#pragma mark - CA

- (void)setMagnifyingFactor:(CGFloat)magnifyingFactor {
    _magnifyingFactor = magnifyingFactor;
    
    [self _setupMagnifyingImageView];
}

#pragma mark - API

- (instancetype)initWithFrame:(CGRect)frame targetView:(UIView *)targetView {
    if (!targetView) @throw [NSException exceptionWithName:NSInvalidArgumentException reason:@"targetView must not be nil." userInfo:nil];
    
    self = [super initWithFrame:frame];
    if (self) {
        self.targetView = targetView;
        
        _magnifyingFactor = kDefaultMagnifierFactor;// we want to avoid the side effect here so we're avoiding the getter.
        
        // set up magnifying image view
        self.magnifyingImageView = [[UIImageView alloc] initWithImage:[self _targetViewSnapshot]];
        [self addSubview:self.magnifyingImageView];
        [self _setupMagnifyingImageView];
        
        // setup glass view
        self.clipsToBounds = YES;
        self.backgroundColor = [UIColor darkGrayColor];
        
        // add little focus point view
        UIView *focusPointView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 8, 8)];
        focusPointView.center = CGPointMake(frame.size.width/2.0, frame.size.height/2.0);
        focusPointView.backgroundColor = [UIColor clearColor];
        focusPointView.layer.cornerRadius = 4.0;
        focusPointView.layer.borderWidth = 1.0;
        focusPointView.layer.borderColor = [UIColor colorWithRed: 0.1887 green: 0.2281 blue: 0.6502 alpha: 1.0].CGColor;
        [self addSubview:focusPointView];
        
        // hide magnifying glass initially
        [self dismissAnimated:NO];
    }
    return self;
}

- (void)showAnimated:(BOOL)animated {
    __weak __typeof(self) weakSelf = self;
    [UIView animateWithDuration:(animated ? kFadeAnimationSpeed : 0) delay:0 options:UIViewAnimationOptionBeginFromCurrentState animations:^{
        weakSelf.alpha = 1.0;
        weakSelf.transform = CGAffineTransformMakeScale(1.0, 1.0);
    } completion:nil];
}

- (void)dismissAnimated:(BOOL)animated {
    __weak __typeof(self) weakSelf = self;
    [UIView animateWithDuration:(animated ? kFadeAnimationSpeed : 0) delay:0 options:UIViewAnimationOptionBeginFromCurrentState animations:^{
        weakSelf.alpha = 0.0;
        weakSelf.transform = CGAffineTransformMakeScale(.5, .5);
    } completion:nil];
}

- (void)magnify:(CGPoint)focusPoint {
    self.magnifyingImageView.center = CGPointMake(
        (- focusPoint.x * _magnifyingFactor) + self.magnifyingImageView.frame.size.width/2.0 + self.frame.size.width/2.0,
        (- focusPoint.y * _magnifyingFactor) + self.magnifyingImageView.frame.size.height/2.0 + self.frame.size.height/2.0
    );
}

- (void)refreshInput {
    self.magnifyingImageView.image = [self _targetViewSnapshot];
    [self _setupMagnifyingImageView];
}

#pragma mark - Private

/**
 *  Sets frame and center for the magnifying image view
 */
- (void)_setupMagnifyingImageView {
    self.magnifyingImageView.frame = CGRectMake(0.0, 0.0, self.targetView.frame.size.width * _magnifyingFactor,
                                                self.targetView.frame.size.height * _magnifyingFactor);
    self.magnifyingImageView.center = CGPointMake(self.frame.size.width/2.0, self.frame.size.height/2.0);
}

- (UIImage *)_targetViewSnapshot {
    UIGraphicsBeginImageContextWithOptions(self.targetView.bounds.size, self.targetView.opaque, 0.0);
    [self.targetView.layer renderInContext:UIGraphicsGetCurrentContext()];
    UIImage *snapshot = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return snapshot;
}

@end
