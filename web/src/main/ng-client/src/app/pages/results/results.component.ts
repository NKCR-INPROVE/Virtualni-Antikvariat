import { Component, OnInit } from '@angular/core';

import { AppService } from 'src/app/app.service';
import { AppState } from 'src/app/app.state';
import { ActivatedRoute, Router, NavigationEnd } from '@angular/router';

@Component({
  selector: 'app-results',
  templateUrl: './results.component.html',
  styleUrls: ['./results.component.scss']
})
export class ResultsComponent implements OnInit {

  params;


  constructor(public state: AppState,
              private service: AppService,
              private route: ActivatedRoute,
              private router: Router) { }

  ngOnInit() {
    this.params = this.route.snapshot.queryParams;
    this.getResults();
    this.router.events.subscribe(val => {
      if (val instanceof NavigationEnd) {
        this.params = this.route.snapshot.queryParams;
        this.getResults();
      }
    });

  }

  getResults() {
    console.log(this.params);
    this.service.search(this.params).subscribe(resp => {

    });
  }
}
