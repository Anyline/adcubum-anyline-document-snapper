//
//  ALPagesCollectionViewLayout.m
//  AnylineDocumentScanner
//
//  Created by Milutin Tomic on 16/08/16.
//  Copyright © 2016 9Y Media Group GmbH. All rights reserved.
//

#import "ALPagesCollectionViewLayout.h"

static CGFloat const kCollectionViewItemSpacing = 30.0;
static UIEdgeInsets const kCollectionViewLayoutInset = {0.0, 50.0, 0.0, 50.0};
static CGFloat const kCollectionViewItemHorizontalPaddingToLayoutEdges = 100.0;

@implementation ALPagesCollectionViewLayout

/**
 *  Sets up layout properties
 */
- (void)prepareLayout {
    self.itemSize = CGSizeMake(self.collectionView.frame.size.width - kCollectionViewItemHorizontalPaddingToLayoutEdges,
                               self.collectionView.contentSize.height - kCollectionViewItemSpacing);
    self.minimumInteritemSpacing = kCollectionViewItemSpacing;
    self.minimumLineSpacing = kCollectionViewItemSpacing;
    self.scrollDirection = UICollectionViewScrollDirectionHorizontal;
    self.sectionInset = kCollectionViewLayoutInset;
}

/**
 *  Called after the user stopped scrolling.
 *  The proposed content offset is altered so the current collection view cell is centered
 */
- (CGPoint)targetContentOffsetForProposedContentOffset:(CGPoint)proposedContentOffset withScrollingVelocity:(CGPoint)velocity {
    CGFloat offsetAdjustment = MAXFLOAT;
    CGFloat horizontalOffset = proposedContentOffset.x + self.collectionView.frame.size.width/2.0 - self.itemSize.width/2.0;
    
    CGRect targetRect = CGRectMake(proposedContentOffset.x, 0, self.collectionView.bounds.size.width, self.collectionView.bounds.size.height);
    
    NSArray *array = [super layoutAttributesForElementsInRect:targetRect];
    
    for (UICollectionViewLayoutAttributes *layoutAttributes in array) {
        CGFloat itemOffset = layoutAttributes.frame.origin.x;
        if (ABS(itemOffset - horizontalOffset) < ABS(offsetAdjustment)) {
            offsetAdjustment = itemOffset - horizontalOffset;
        }
    }
    
    return CGPointMake(proposedContentOffset.x + offsetAdjustment, proposedContentOffset.y);
}

@end
