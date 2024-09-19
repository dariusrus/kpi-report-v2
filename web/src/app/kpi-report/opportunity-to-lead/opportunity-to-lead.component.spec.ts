import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OpportunityToLeadComponent } from './opportunity-to-lead.component';

describe('OpportunityToLeadComponent', () => {
  let component: OpportunityToLeadComponent;
  let fixture: ComponentFixture<OpportunityToLeadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [OpportunityToLeadComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OpportunityToLeadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
