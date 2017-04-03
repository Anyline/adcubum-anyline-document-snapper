//
//  ALPageCollectionViewCell.h
//  AnylineDocumentScanner
//
//  Created by Milutin Tomic on 18/08/16.
//  Copyright © 2016 9Y Media Group GmbH. All rights reserved.
//

#import <UIKit/UIKit.h>

@class ALResultPage;

@interface ALPageCollectionViewCell : UICollectionViewCell

@property (nonatomic, strong) ALResultPage *page;

@end
