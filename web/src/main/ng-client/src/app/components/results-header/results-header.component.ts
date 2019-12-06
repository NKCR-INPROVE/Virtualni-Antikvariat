import { Component, OnInit, Input } from '@angular/core';
import { ResultsHeader } from 'src/app/models/results-header';
import { PageEvent } from '@angular/material';
import { Params, Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-results-header',
  templateUrl: './results-header.component.html',
  styleUrls: ['./results-header.component.scss']
})
export class ResultsHeaderComponent implements OnInit {
  
  @Input() resultsHeader: ResultsHeader;
  @Input() params: Params;

  // MatPaginator Output
  pageEvent: PageEvent;

  constructor(
    private route: ActivatedRoute,
    private router: Router) { }

  ngOnInit() {
  }

  onPage(e: PageEvent) {
    const queryParams = {rows: e.pageSize, offset: e.pageIndex};
    this.router.navigate([],
      {
        relativeTo: this.route,
        queryParams,
        queryParamsHandling: 'merge',
      });
  }

}
