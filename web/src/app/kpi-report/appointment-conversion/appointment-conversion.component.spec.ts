import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppointmentConversionComponent } from './appointment-conversion.component';

describe('AppointmentConversionComponent', () => {
  let component: AppointmentConversionComponent;
  let fixture: ComponentFixture<AppointmentConversionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AppointmentConversionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AppointmentConversionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
