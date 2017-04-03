//
//  ALPageCollectionViewCell.m
//  AnylineDocumentScanner
//
//  Created by Milutin Tomic on 18/08/16.
//  Copyright © 2016 9Y Media Group GmbH. All rights reserved.
//

#import "ALPageCollectionViewCell.h"

#import "ALResultPage.h"

@interface ALPageCollectionViewCell ()

@property (weak, nonatomic) IBOutlet UIImageView *pageImageView;

@end

@implementation ALPageCollectionViewCell

#pragma mark - Custom accessors

- (void)setPage:(ALResultPage *)page {
    // store ivar
    _page = page;

    [self _updateUI];
}

#pragma mark - Overrides

- (void)prepareForReuse {
    [super prepareForReuse];

    self.pageImageView.image = nil;
}

#pragma mark - Private

- (void)_updateUI {
    // update UI
    self.pageImageView.image = self.page.thumbnail;
}

@end
