import { Component, OnInit, Inject } from '@angular/core';
import { AppState } from 'src/app/app.state';
import { Offer } from 'src/app/models/offer';
import { MatDialogRef, MAT_DIALOG_DATA, MatSnackBar } from '@angular/material';
import { OfferRecord } from 'src/app/models/offer-record';
import { AppService } from 'src/app/app.service';

@Component({
  selector: 'app-add-to-offer-dialog',
  templateUrl: './add-to-offer-dialog.component.html',
  styleUrls: ['./add-to-offer-dialog.component.scss']
})
export class AddToOfferDialogComponent implements OnInit {

  constructor(
    public dialogRef: MatDialogRef<AddToOfferDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: OfferRecord,
    public state: AppState,
    private service: AppService
  ) { }

  ngOnInit() {
  }

  ok(id: string, title: string) {

    this.data.offer_id = id;
      this.dialogRef.close(this.data);
    
  }

}
