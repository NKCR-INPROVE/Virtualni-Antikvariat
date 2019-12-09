import { Component, OnInit } from '@angular/core';
import { Offer } from 'src/app/models/offer';
import { User } from 'src/app/models/user';
import { OfferRecord } from 'src/app/models/offer-record';
import { AppService } from 'src/app/app.service';
import { ActivatedRoute } from '@angular/router';
import { AppState } from 'src/app/app.state';

@Component({
  selector: 'app-report',
  templateUrl: './report.component.html',
  styleUrls: ['./report.component.scss']
})
export class ReportComponent implements OnInit {

  offer: Offer;
  records: OfferRecord[];
  offering: User;
  receiverIds: string[] = [];
  receivers: User[] = [];

  constructor(
    private route: ActivatedRoute,
    private state: AppState,
    private service: AppService) { }

  ngOnInit() {
    this.load();
  }

  load() {
    console.log(this.route.snapshot.queryParams);
    const id = this.route.snapshot.queryParams.id;


    this.service.getOffer(id).subscribe(offer => {
      this.offer = offer;

      console.log(offer);
      this.service.getUser(this.offer.knihovna).subscribe(user => {
        console.log(user);
        this.offering = user;
        this.service.getOfferRecords(this.offer.id).subscribe(resp2 => {
          this.records = resp2;
          console.log(resp2);
          this.records.forEach(rec => {
            console.log(rec);
            if (rec.chci) {
              rec.chci.forEach(s => {
                if (!this.receiverIds.includes(s)) {
                  this.receiverIds.push(s);
                  this.addReceiving(s);
                }
              });
            }
          });
        });
      });
    });
  }

  addReceiving(code: string) {
    this.service.getUser(code).subscribe(resp => {
      this.receivers.push(resp);
    });

  }

}
