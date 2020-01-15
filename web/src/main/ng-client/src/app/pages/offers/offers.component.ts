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
import { ConfirmDialogComponent } from 'src/app/components/confirm-dialog/confirm-dialog.component';
import { Router } from '@angular/router';

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
    private router: Router,
    public dialog: MatDialog,
    private service: AppService,
    private state: AppState) { }

  ngOnInit() {
    if (this.state.isLibrary) {
      this.refresh();
    } else {
      this.router.navigate(['/home']);
    }
  }

  remove(idx: number) {


    const dialogRef = this.dialog.open(PromptDialogComponent, {
      width: '350px',
      data: { title: 'offers.remove' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const offer: Offer = this.offers[idx];
        this.service.removeOffer(offer).subscribe(resp => {
          if (resp.error) {
            this.service.showSnackBar('offer.remove_error', '', true);
          } else {
            this.state.offers.splice(idx, 1);
            this.offers.splice(idx, 1);
            this.service.showSnackBar('offer.remove_success');
          }
        });
      }
    });

  }

  removeFromOffer(idx: number) {
    // this.records = this.records.filter((val, index) => index !== idx);

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '350px',
      data: { title: 'offers.remove_from_offer' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.service.removeOfferRecord(this.records[idx].id).subscribe(resp => {
          if (resp.error) {
            this.service.showSnackBar('snack_bar.remove_from_offer_error', '', true);
          } else {
            this.load(this.currentOffer);
            this.service.showSnackBar('snack_bar.remove_from_offer_success');
          }
        });
      }
    });

  }

  refresh() {
    this.service.getOffers().subscribe(resp => {
      this.offers = resp;
      this.offers = this.offers.filter(o => o.knihovna === this.state.user.username);
      if (!this.currentOffer) {
        for (let i = 0; i < this.offers.length; i++) {
          if (!this.offers[i].closed) {
            this.load(this.offers[i]);
            return;
          }
        }
      }
    });
  }

  load(offer: Offer) {
    this.currentOffer = offer;
    this.service.getOfferRecords(offer.id).subscribe(resp => { this.records = resp; });
  }

  add(): void {
    const dialogRef = this.dialog.open(PromptDialogComponent, {
      width: '350px',
      data: { title: 'offers.add' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const offer: Offer = new Offer();
        offer.nazev = result;
        offer.knihovna = this.state.user.username;
        offer.created = formatDate(new Date(), 'yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'', 'cs');
        this.service.addOffer(offer).subscribe(resp => {
          if (resp.error) {
            this.service.showSnackBar('snack_bar.add_error', '', true);
          } else {
            this.state.offers.push(resp);
            this.offers.push(resp);
            this.service.showSnackBar('snack_bar.add_success');
          }
        });
      }
    });
  }

  refreshOffer() {
    this.load(this.currentOffer);
  }

  viewReport() {
    window.open('protocol?id=' + this.currentOffer.id, '_blank');
  }

  uploadToOffer() {
    const dialogRef = this.dialog.open(UploadToOfferDialogComponent, {
      width: '350px',
      data: { title: 'offers.add', offerId: this.currentOffer.id }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.refreshOffer();
      }
    });

  }

  searchToOffer() {
    const dialogRef = this.dialog.open(SearchToOfferDialogComponent, {
      width: '600px',
      data: { title: 'offers.add' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {

      }
    });


  }

  templateToOffer() {
    const dialogRef = this.dialog.open(TemplateToOfferDialogComponent, {
      width: '600px',
      data: new OfferRecord()
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const record = new OfferRecord();
        record.offer_id = this.currentOffer.id;
        record.title = result.title;
        record.cena = result.cena;
        record.comment = result.comment;
        record.fields = result;
        record.knihovna = this.currentOffer.knihovna;

        this.service.addToOffer(record).subscribe(resp => {
          if (resp.error) {
            this.service.showSnackBar('snack_bar.add_error', '', true);
          } else {
            this.refreshOffer();
            this.service.showSnackBar('snack_bar.add_success');
          }
        });

      }
    });


  }

  addToVA(record: OfferRecord) {

    if (!record.cena || record.cena === 0) {
      const dialogRef = this.dialog.open(PromptDialogComponent, {
        width: '350px',
        data: { title: 'offers.price_required' }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          record.cena = result;
          record.isVA = true;
          this.service.addToOffer(record).subscribe(resp => {
            if (resp.error) {
              this.service.showSnackBar('snack_bar.add_to_va_error', '', true);
            } else {
              this.service.showSnackBar('snack_bar.add_to_va_success');
            }
          });
        }


      });
    } else {
      record.isVA = true;
      this.service.addToOffer(record).subscribe(resp => {
        if (resp.error) {
          this.service.showSnackBar('snack_bar.add_to_va_error', '', true);
        } else {
          this.service.showSnackBar('snack_bar.add_to_va_success');
        }
      });
    }

  }

  removeFromVA(record: OfferRecord) {
    record.isVA = false;
    this.service.addToOffer(record).subscribe(resp => {
      if (resp.error) {
        this.service.showSnackBar('snack_bar.remove_from_va_error', '', true);
      } else {
        this.service.showSnackBar('snack_bar.remove_from_va_success');
      }
    });

  }

}
