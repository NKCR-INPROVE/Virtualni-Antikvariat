import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { OfferRecord } from 'src/app/models/offer-record';

@Component({
  selector: 'app-template-to-offer-dialog',
  templateUrl: './template-to-offer-dialog.component.html',
  styleUrls: ['./template-to-offer-dialog.component.scss']
})
export class TemplateToOfferDialogComponent implements OnInit {

  fields: any = {};

  constructor(
    public dialogRef: MatDialogRef<TemplateToOfferDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: OfferRecord) { }

  ngOnInit() {
  }

  add() {
    this.fields.title = this.fields['245a'];
    console.log(this.fields);
  
    this.dialogRef.close(this.fields);
  }

  reset() {
    this.fields = {};
  }

}
