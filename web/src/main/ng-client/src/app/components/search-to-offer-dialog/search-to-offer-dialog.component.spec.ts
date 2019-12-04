import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchToOfferDialogComponent } from './search-to-offer-dialog.component';

describe('SearchToOfferDialogComponent', () => {
  let component: SearchToOfferDialogComponent;
  let fixture: ComponentFixture<SearchToOfferDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SearchToOfferDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchToOfferDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
