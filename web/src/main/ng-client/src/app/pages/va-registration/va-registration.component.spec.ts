import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { VaRegistrationComponent } from './va-registration.component';

describe('VaRegistrationComponent', () => {
  let component: VaRegistrationComponent;
  let fixture: ComponentFixture<VaRegistrationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VaRegistrationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VaRegistrationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
