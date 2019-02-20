//
//  ALManualTriggerButton.m
//  Adcubum Document Snapper
//
//  Created by Jonas Laux on 22.05.17.
//
//

#import "ALManualTriggerButton.h"
#import "DGActivityIndicatorView.h"

@interface ALManualTriggerButton ()

@property (nonatomic, strong) UIImageView *cameraImageView;
@property (nonatomic, strong) DGActivityIndicatorView *activityIndicatorView;

@end

@implementation ALManualTriggerButton


// A custom designated initializer for an UIView subclass.
- (id)initWithFrame:(CGRect)frame tintColor:(UIColor *)tintColor {
    self = [super initWithFrame:frame];
    if (self) {
        _mode = ALManualCropButtonModeSearching;
        
        UIImage *image = [UIImage imageNamed:@"cam_trigger"];
        self.cameraImageView = [[UIImageView alloc] initWithImage:[image imageWithRenderingMode:UIImageRenderingModeAlwaysTemplate]];
        self.cameraImageView.tintColor = tintColor;
        self.cameraImageView.hidden = YES;
        self.cameraImageView.alpha = 0;
        
        self.activityIndicatorView = [[DGActivityIndicatorView alloc] initWithType:DGActivityIndicatorAnimationTypeBallClipRotateMultiple
                                                                         tintColor:[UIColor whiteColor]
                                                                              size:52.0f];
        self.activityIndicatorView.tintColor = tintColor;
        [self.activityIndicatorView startAnimating];
        
        [self addSubview:self.cameraImageView];
        [self addSubview:self.activityIndicatorView];
    }
    return self;
}

- (void)layoutSubviews {
    self.cameraImageView.frame = CGRectMake(0,0,self.frame.size.width,self.frame.size.height);
    self.activityIndicatorView.size = self.bounds.size.width;
    self.activityIndicatorView.center = CGPointMake(self.frame.size.width/2,self.frame.size.height/2);
}

- (void)setMode:(ALManualCropButtonMode)mode {
    if (mode == _mode) {
        return;
    }
    _mode = mode;
    
    CGFloat cameraImageAlpha = 0;
    CGFloat activityIndicatorAlpha = 0;
    
    BOOL cameraImageHidden = YES;
    BOOL activityIndicatorHidden = YES;
    switch (mode) {
        case ALManualCropButtonModePhoto:
            cameraImageAlpha = 1;
            cameraImageHidden = NO;
            break;
        case ALManualCropButtonModeSearching:
            activityIndicatorAlpha = 1;
            activityIndicatorHidden = NO;
            [self.activityIndicatorView startAnimating];
        default:
            break;
    }
    self.activityIndicatorView.hidden = NO;
    self.cameraImageView.hidden = NO;
    [UIView animateWithDuration:0.5 animations:^{
        self.activityIndicatorView.alpha = activityIndicatorAlpha;
        self.cameraImageView.alpha = cameraImageAlpha;
    } completion:^(BOOL finished) {
        self.activityIndicatorView.hidden = activityIndicatorHidden;
        self.cameraImageView.hidden = cameraImageHidden;
        if (mode == ALManualCropButtonModePhoto) {
            [self.activityIndicatorView stopAnimating];
        }
    }];
}




@end
