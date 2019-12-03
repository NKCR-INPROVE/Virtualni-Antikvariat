import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'app-search-to-offer-dialog',
  templateUrl: './search-to-offer-dialog.component.html',
  styleUrls: ['./search-to-offer-dialog.component.scss']
})
export class SearchToOfferDialogComponent implements OnInit {

  constructor(
    public dialogRef: MatDialogRef<SearchToOfferDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any) { }

  ngOnInit() {
  }

}
