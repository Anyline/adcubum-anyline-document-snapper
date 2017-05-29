//
//  ALLocalizationMacro.h
//  Adcubum Document Snapper
//
//  Created by Daniel Albertini on 23/05/2017.
//
//

#ifndef ALLocalizationMacro_h
#define ALLocalizationMacro_h

#define ALLocalizedString(key, comment, language) \
[[NSDictionary dictionaryWithContentsOfFile:[[NSBundle bundleWithPath:[[NSBundle mainBundle] pathForResource:@"AnylineCordovaWrapperLocalizations" ofType:@"bundle"]] pathForResource:@"Localizable" ofType:@"strings" inDirectory:nil forLocalization:language]] objectForKey:key] ? [[NSDictionary dictionaryWithContentsOfFile:[[NSBundle bundleWithPath:[[NSBundle mainBundle] pathForResource:@"AnylineCordovaWrapperLocalizations" ofType:@"bundle"]] pathForResource:@"Localizable" ofType:@"strings" inDirectory:nil forLocalization:language]] objectForKey:key] : key

#endif /* ALLocalizationMacro_h */
