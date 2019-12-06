import { Component, OnInit } from '@angular/core';
import { Offer } from 'src/app/models/offer';
import { AppService } from 'src/app/app.service';
import { ArgumentOutOfRangeError } from 'rxjs';
import { OfferRecord } from 'src/app/models/offer-record';
import { PromptDialogComponent } from 'src/app/components/prompt-dialog/prompt-dialog.component';
import { MatDialog, MatSnackBar } from '@angular/material';
import { AppState } from 'src/app/app.state';
import { UploadToOfferDialogComponent } from 'src/app/components/upload-to-offer-dialog/upload-to-offer-dialog.component';
import { SearchToOfferDialogComponent } from 'src/app/components/search-to-offer-dialog/search-to-offer-dialog.component';
import { TemplateToOfferDialogComponent } from 'src/app/components/template-to-offer-dialog/template-to-offer-dialog.component';
import { DatePipe, formatDate } from '@angular/common';
import { Utils } from 'src/app/shared/utils';

@Component({
  selector: 'app-offers',
  templateUrl: './offers.component.html',
  styleUrls: ['./offers.component.scss']
})
export class OffersComponent implements OnInit {

  displayedColumns = ['text', 'price', 'button'];
  offers: Offer[] = [];
  records: OfferRecord[];
  currentOffer: Offer;

  constructor(
    public dialog: MatDialog,
    private snackBar: MatSnackBar,
    private service: AppService,
    private state: AppState) { }

  ngOnInit() {
    this.refresh();
  }

  remove(idx: number) {
    this.offers.splice(idx, 1);
  }

  removeFromOffer(idx: number) {
    this.records = this.records.filter((val, index) => index !== idx);
  }

  refresh() {
    this.service.getOffers().subscribe(resp => { 
      this.offers = resp;
      this.offers = this.offers.filter(o => o.knihovna === this.state.user.code);
    });
  }

  load(offer: Offer) {
    this.currentOffer = offer;
    this.service.getOffer(offer.id).subscribe(resp => { this.records = resp; });
  }

  add(): void {
    const dialogRef = this.dialog.open(PromptDialogComponent, {
      width: '350px',
      data: {title: 'offers.add'}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const offer: Offer = new Offer();
        offer.nazev = result;
        offer.knihovna = this.state.user.code;
        offer.created = formatDate(new Date(), 'yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'', 'cs');
        this.service.addOffer(offer).subscribe(resp => {
          if (resp.error) {
            this.snackBar.open(this.service.getTranslation('Error_adding_offer'), '', {
              duration: 2000,
              verticalPosition: 'top',
              panelClass: 'app-color-red'
            });
          } else {
            this.state.offers.push(resp);
            this.offers.push(resp);
            this.snackBar.open(this.service.getTranslation('offer_added'), '', {
              duration: 2000,
              verticalPosition: 'top',
              panelClass: 'app-color-green'
            });
          }
        });
      }
    });
  }

  refreshOffer() {

  }

  viewReport() {

  }

  uploadToOffer() {
    const dialogRef = this.dialog.open(UploadToOfferDialogComponent, {
      width: '350px',
      data: {title: 'offers.add'}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        
      }
    });

  }

  searchToOffer() {
    const dialogRef = this.dialog.open(SearchToOfferDialogComponent, {
      width: '600px',
      data: {title: 'offers.add'}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        
      }
    });


  }

  templateToOffer() {
    const dialogRef = this.dialog.open(TemplateToOfferDialogComponent, {
      width: '600px',
      data: {title: 'offers.add'}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        
      }
    });


  }

}
