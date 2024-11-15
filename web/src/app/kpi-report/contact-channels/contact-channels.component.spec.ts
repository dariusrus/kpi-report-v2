import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContactChannelsComponent } from './ghlContact-channels.component';

describe('LeadValuationComponent', () => {
  let component: ContactChannelsComponent;
  let fixture: ComponentFixture<ContactChannelsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ContactChannelsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ContactChannelsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
