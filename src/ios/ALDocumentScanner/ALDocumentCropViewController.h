//
//  ALDocumentCropViewController.h
//  Pods
//
//  Created by Milutin Tomic on 28/08/16.
//
//

#import <UIKit/UIKit.h>

@class ALDocumentCropViewController, ALResultPage;

@protocol ALDocumentCropViewControllerDelegate <NSObject>

/**
 *  Called once the user taps the done button
 *
 *  @param cropVC
 */
- (void)cropViewControllerDidFinishCropping:(nonnull ALDocumentCropViewController *)cropVC withResult:(nonnull ALResultPage *)resultPage;

@end


@interface ALDocumentCropViewController : UIViewController

- (nonnull instancetype)initWithPage:(nonnull ALResultPage *)page;


@property (nonatomic, assign, nullable) id<ALDocumentCropViewControllerDelegate> delegate;

@end
