import { Component, OnInit } from '@angular/core';
import { Offer } from 'src/app/models/offer';
import { AppService } from 'src/app/app.service';
import { ArgumentOutOfRangeError } from 'rxjs';
import { OfferRecord } from 'src/app/models/offer-record';

@Component({
  selector: 'app-offers',
  templateUrl: './offers.component.html',
  styleUrls: ['./offers.component.scss']
})
export class OffersComponent implements OnInit {

  displayedColumns = ['text', 'button'];
  offers: Offer[] = [];
  records: OfferRecord[];
  currentOffer: Offer;

  constructor(private service: AppService) { }

  ngOnInit() {
    this.refresh();
  }

  add() {
    const d = new Offer();
    this.offers.push(d);
  }

  remove(idx: number) {
    this.offers.splice(idx, 1);
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

}
