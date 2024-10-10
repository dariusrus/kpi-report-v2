export interface DevicePerformance {
  pcPerformance: string;
  tabletPerformance: string;
  mobilePerformance: string;
  otherPerformance: string;
}

export interface UrlEngagement {
  standoutPages: string;
  availableHomes: string;
  otherPages: string;
}

export interface UserBehavior {
  mobileBehavior: string;
  tabletBehavior: string;
  urlVariations: string;
}

export interface KeyTakeaways {
  devicePerformance: DevicePerformance[];
  urlEngagement: UrlEngagement[];
  userBehavior: UserBehavior[];
}

export interface InsightsAndSuggestions {
  focusOnMobileOptimization: string;
  enhanceVisualContent: string;
  optimizeTabletExperience: string;
}

export interface AnalyticsInsights {
  keyTakeaways: KeyTakeaways;
  insightsAndSuggestions: InsightsAndSuggestions[];
}
