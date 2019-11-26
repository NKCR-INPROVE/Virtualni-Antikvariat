import { Component, OnInit, Input } from '@angular/core';
import { ResultsHeader } from 'src/app/models/results-header';

@Component({
  selector: 'app-results-header',
  templateUrl: './results-header.component.html',
  styleUrls: ['./results-header.component.scss']
})
export class ResultsHeaderComponent implements OnInit {
  @Input() resultsHeader: ResultsHeader;

  constructor() { }

  ngOnInit() {
  }

}
