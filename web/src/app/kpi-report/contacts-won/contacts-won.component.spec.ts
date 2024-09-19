import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContactsWonComponent } from './contacts-won.component';

describe('ContactsWonComponent', () => {
  let component: ContactsWonComponent;
  let fixture: ComponentFixture<ContactsWonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ContactsWonComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ContactsWonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
