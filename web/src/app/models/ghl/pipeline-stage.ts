import {SalesPersonConversion} from "./sales-person-conversion";

export interface PipelineStage {
  stageName: string;
  count: number;
  percentage: number;
  monetaryValue: number;
  salesPersonConversions: SalesPersonConversion[];
  previousMonthIndex: number;
}
