import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AddToOfferDialogComponent } from './add-to-offer-dialog.component';

describe('AddToOfferDialogComponent', () => {
  let component: AddToOfferDialogComponent;
  let fixture: ComponentFixture<AddToOfferDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AddToOfferDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AddToOfferDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
