import { Component, OnInit } from '@angular/core';
import { Offer } from 'src/app/models/offer';
import { AppService } from 'src/app/app.service';
import { ArgumentOutOfRangeError } from 'rxjs';
import { OfferRecord } from 'src/app/models/offer-record';
import { PromptDialogComponent } from 'src/app/components/prompt-dialog/prompt-dialog.component';
import { MatDialog } from '@angular/material';
import { AppState } from 'src/app/app.state';
import { UploadToOfferDialogComponent } from 'src/app/components/upload-to-offer-dialog/upload-to-offer-dialog.component';
import { SearchToOfferDialogComponent } from 'src/app/components/search-to-offer-dialog/search-to-offer-dialog.component';
import { TemplateToOfferDialogComponent } from 'src/app/components/template-to-offer-dialog/template-to-offer-dialog.component';

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
        this.offers.push(offer);
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
