//
//  ALResultDocument.m
//  AnylineDocumentScanner
//
//  Created by Milutin Tomic on 17/08/16.
//  Copyright © 2016 9Y Media Group GmbH. All rights reserved.
//

#import "ALResultDocument.h"

@implementation ALResultDocument

- (NSString *)subject {
    return _subject ?: @"";
}

- (NSArray<ALResultPage *> *)pages {
    return _pages ?: @[];
}

@end
