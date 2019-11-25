import { Component, OnInit, OnDestroy } from '@angular/core';

import { AppService } from 'src/app/app.service';
import { AppState } from 'src/app/app.state';
import { ActivatedRoute, Router, NavigationEnd, Params } from '@angular/router';
import { Subscription } from 'rxjs';
import { HttpParams } from '@angular/common/http';

@Component({
  selector: 'app-results',
  templateUrl: './results.component.html',
  styleUrls: ['./results.component.scss']
})
export class ResultsComponent implements OnInit, OnDestroy {

  params: Params;
  subscriptions: Subscription[] = [];

  constructor(public state: AppState,
              private service: AppService,
              private route: ActivatedRoute,
              private router: Router) { }

  ngOnInit() {
    this.params = this.route.snapshot.queryParams;
    this.getResults();
    this.subscriptions.push(this.router.events.subscribe(val => {
      if (val instanceof NavigationEnd) {
        this.params = this.route.snapshot.queryParams;
        this.getResults();
      }
    }));

  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(s => {
      s.unsubscribe();
      s = null;
    });

  }

  getResults() {
    this.service.search(this.params as HttpParams).subscribe(resp => {});
  }
}
