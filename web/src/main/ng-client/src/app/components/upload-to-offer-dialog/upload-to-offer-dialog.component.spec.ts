import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UploadToOfferDialogComponent } from './upload-to-offer-dialog.component';

describe('UploadToOfferDialogComponent', () => {
  let component: UploadToOfferDialogComponent;
  let fixture: ComponentFixture<UploadToOfferDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UploadToOfferDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UploadToOfferDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
