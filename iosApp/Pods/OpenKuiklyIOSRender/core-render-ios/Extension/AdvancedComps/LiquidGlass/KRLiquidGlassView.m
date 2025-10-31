/*
 * Tencent is pleased to support the open source community by making KuiklyUI
 * available.
 * Copyright (C) 2025 Tencent. All rights reserved.
 * Licensed under the License of KuiklyUI;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://github.com/Tencent-TDS/KuiklyUI/blob/main/LICENSE
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#import "KRLiquidGlassView.h"
#import "KRConvertUtil.h"

@implementation KRLiquidGlassView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame: frame]) {
        if (@available(iOS 26.0, *)) {
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 260000
            self.effect = [UIGlassEffect effectWithStyle:UIGlassEffectStyleRegular];
#endif
        } else {
            // Fallback on earlier versions
        }
    }
    return self;
}

- (void)hrv_setPropWithKey:(NSString * _Nonnull)propKey propValue:(id _Nonnull)propValue {
    KUIKLY_SET_CSS_COMMON_PROP
}

- (void)hrv_insertSubview:(UIView *)subView atIndex:(NSInteger)index {
    [self.contentView insertSubview:subView atIndex:index];
}

#pragma mark - CSS properties

- (void)setCss_glassEffectTintColor:(NSNumber *)color {
    if (@available(iOS 26.0, *)) {
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 260000
        UIGlassEffect *effect = (UIGlassEffect *)self.effect;
        if (![effect.tintColor isEqual:color]) {
            effect.tintColor = [UIView css_color:color];
            self.effect = effect;
        }
#endif
    }
}

- (void)setCss_glassEffectInteractive:(NSNumber *)interactive {
    if (@available(iOS 26.0, *)) {
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 260000
        UIGlassEffect *effect = (UIGlassEffect *)self.effect;
        if (effect.isInteractive != [interactive boolValue]) {
            effect.interactive = [interactive boolValue];
            self.effect = effect;
        }
#endif
    }
}

- (void)setCss_glassEffectStyle:(NSString *)style {
    if (@available(iOS 26.0, *)) {
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 260000
        UIGlassEffectStyle glassStyle = [KRConvertUtil KRGlassEffectStyle:style];
        UIGlassEffect *newEffect = [UIGlassEffect effectWithStyle:glassStyle];
        UIGlassEffect *currentEffect = (UIGlassEffect *)self.effect;
        
        // Preserve existing properties
        newEffect.tintColor = currentEffect.tintColor;
        newEffect.interactive = currentEffect.isInteractive;
        
        self.effect = newEffect;
#endif
    }
}

@end