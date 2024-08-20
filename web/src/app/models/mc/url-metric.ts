import {DeviceMetric} from "./device-metric";

export interface UrlMetric {
  id: number;
  url: string;
  devices: DeviceMetric[];
  averageScrollDepth: number | null;
  activeTime: number | null;
  totalSessionCount: number | null;
}
