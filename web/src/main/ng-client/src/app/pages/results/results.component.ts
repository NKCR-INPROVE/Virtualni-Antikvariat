import { Component, OnInit } from '@angular/core';

import { AppService } from 'src/app/app.service';
import { AppState } from 'src/app/app.state';

@Component({
  selector: 'app-results',
  templateUrl: './results.component.html',
  styleUrls: ['./results.component.scss']
})
export class ResultsComponent implements OnInit {

  constructor(public state: AppState,
              private service: AppService) { }

  ngOnInit() {
  }
}
