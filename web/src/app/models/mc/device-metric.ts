export interface DeviceMetric {
  id: number;
  deviceType: string;
  averageScrollDepth: number | null;
  totalTime: number | null;
  activeTime: number | null;
  totalSessionCount: number | null;
}
