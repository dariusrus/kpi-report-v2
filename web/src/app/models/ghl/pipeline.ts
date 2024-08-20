import {PipelineStage} from "./pipeline-stage";

export interface Pipeline {
  pipelineName: string;
  totalCount: number;
  pipelineStages: PipelineStage[];
}
