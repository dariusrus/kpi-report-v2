import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PipelineStagesComponent } from './pipeline-stages.component';

describe('PipelineStagesComponent', () => {
  let component: PipelineStagesComponent;
  let fixture: ComponentFixture<PipelineStagesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PipelineStagesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PipelineStagesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
