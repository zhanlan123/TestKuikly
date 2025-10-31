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

#import "KRPerformanceManager+LifeCircle.h"
#import "KRPerformanceManager.h"

@implementation KRPerformanceManager (LifeCircle)


- (void)viewDidAppear {
    self.pageState |= KRPageState_viewDidAppear;
    [self startMonitor];
}

- (void)viewWillDisappear {
    self.pageState &= ~KRPageState_viewDidAppear;
    [self endMonitor];
}

- (void)willFetchContextCode {
    [self endStage:KRLoadStage_initView];
    [self startStage:KRLoadStage_fetchContextCode];
}

- (void)didFetchContextCode {
    [self endStage:KRLoadStage_fetchContextCode];
    [self startStage:KRLoadStage_fristPaint];
}

- (void)contentViewDidLoad {
    [self endStage:KRLoadStage_fristPaint];
    self.pageState |= KRPageState_viewDidLoad;
    [self startMonitor];
}

-(void)delegatorDealloc {
    [self endMonitor];
}

- (void)onReceiveApplicationDidBecomeActive {
    self.pageState |= KRPageState_appActive;
    [self startMonitor];
}

- (void)onReceiveApplicationWillResignActive {
    self.pageState &= ~KRPageState_viewDidAppear;
    [self endMonitor];
}

@end
