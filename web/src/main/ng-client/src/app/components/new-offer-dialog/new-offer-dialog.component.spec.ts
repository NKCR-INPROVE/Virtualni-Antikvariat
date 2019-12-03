import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NewOfferDialogComponent } from './new-offer-dialog.component';

describe('NewOfferDialogComponent', () => {
  let component: NewOfferDialogComponent;
  let fixture: ComponentFixture<NewOfferDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NewOfferDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NewOfferDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
