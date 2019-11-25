import { Component, OnInit } from '@angular/core';
import { AppConfiguration } from 'src/app/app-configuration';
import { AppState } from 'src/app/app.state';
import { Filters } from 'src/app/models/filters';
import { Facet } from 'src/app/models/facet';

@Component({
  selector: 'app-facets',
  templateUrl: './facets.component.html',
  styleUrls: ['./facets.component.scss']
})
export class FacetsComponent implements OnInit {

  fields: string[];
  facets: any;

  constructor(
    private config: AppConfiguration,
    public state: AppState) { }

  ngOnInit() {
    // console.log(this.config.facets);
    this.state.facets.subscribe(f => {
      console.log(f);
      this.fields = Object.keys(f);
      this.facets = Object.assign({}, f);
    });
  }

  toggleFilter(f: Facet) {
    
  }

  addFilter(f: Facet) {
    
  }

  excludeFilter(f: Facet) {
    
  }

}
