import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'app-template-to-offer-dialog',
  templateUrl: './template-to-offer-dialog.component.html',
  styleUrls: ['./template-to-offer-dialog.component.scss']
})
export class TemplateToOfferDialogComponent implements OnInit {

  constructor(
    public dialogRef: MatDialogRef<TemplateToOfferDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any) { }

  ngOnInit() {
  }

  add() {

  }

  reset() {
    
  }

}
