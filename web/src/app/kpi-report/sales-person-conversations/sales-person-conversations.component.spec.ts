import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SalesPersonConversationsComponent } from './sales-person-conversations.component';

describe('PipelineStagesComponent', () => {
  let component: SalesPersonConversationsComponent;
  let fixture: ComponentFixture<SalesPersonConversationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SalesPersonConversationsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SalesPersonConversationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
