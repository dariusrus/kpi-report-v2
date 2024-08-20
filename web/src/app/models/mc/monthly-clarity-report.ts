import {DeviceClarityAggregate} from "./device-clarity-aggregate";
import {UrlMetric} from "./url-metric";

export interface MonthlyClarityReport {
  id: number;
  deviceClarityAggregate: DeviceClarityAggregate[];
  urls: UrlMetric[];
}
