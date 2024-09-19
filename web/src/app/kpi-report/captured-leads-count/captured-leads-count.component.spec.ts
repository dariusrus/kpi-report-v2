import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CapturedLeadsCountComponent } from './captured-leads-count.component';

describe('CapturedLeadsCountComponent', () => {
  let component: CapturedLeadsCountComponent;
  let fixture: ComponentFixture<CapturedLeadsCountComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CapturedLeadsCountComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CapturedLeadsCountComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
