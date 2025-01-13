import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewLeadAppointmentsComponent } from './new-lead-appointments.component';

describe('NewLeadAppointmentsComponent', () => {
  let component: NewLeadAppointmentsComponent;
  let fixture: ComponentFixture<NewLeadAppointmentsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NewLeadAppointmentsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NewLeadAppointmentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
