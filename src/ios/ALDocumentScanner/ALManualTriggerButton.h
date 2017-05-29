//
//  ALManualTriggerButton.h
//  Adcubum Document Snapper
//
//  Hingerotzt by Jonas Laux on 22.05.17. Beautifully build by Daniel Albertini on 22.05.17
//
//

#import <UIKit/UIKit.h>

typedef NS_ENUM(NSUInteger, ALManualCropButtonMode) {
    ALManualCropButtonModePhoto,
    ALManualCropButtonModeSearching,
};

@interface ALManualTriggerButton : UIControl

- (id)initWithFrame:(CGRect)frame tintColor:(UIColor *)tintColor;

@property (nonatomic, assign) ALManualCropButtonMode mode;

@end
