import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplateToOfferDialogComponent } from './template-to-offer-dialog.component';

describe('TemplateToOfferDialogComponent', () => {
  let component: TemplateToOfferDialogComponent;
  let fixture: ComponentFixture<TemplateToOfferDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TemplateToOfferDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateToOfferDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
