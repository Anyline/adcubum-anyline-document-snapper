//
//  ALResultDocument.h
//  AnylineDocumentScanner
//
//  Created by Milutin Tomic on 17/08/16.
//  Copyright © 2016 9Y Media Group GmbH. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ALResultPage.h"

@interface ALResultDocument : NSObject

@property (nonatomic, copy, nonnull) NSString                       *subject;
@property (nonatomic, strong, nonnull) NSArray<ALResultPage *>      *pages;

@end
