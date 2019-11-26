import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { ActivatedRoute, Router, NavigationEnd, Params } from '@angular/router';
import { AppConfiguration } from 'src/app/app-configuration';
import { AppState } from 'src/app/app.state';
import { Filters } from 'src/app/models/filters';
import { Facet } from 'src/app/models/facet';
import { SearchParams, AdvancedParams } from 'src/app/models/search-params';
import { Utils } from 'src/app/shared/utils';

@Component({
  selector: 'app-facets',
  templateUrl: './facets.component.html',
  styleUrls: ['./facets.component.scss']
})
export class FacetsComponent implements OnInit, OnDestroy {

  subscriptions: Subscription[] = [];

  searchParams: SearchParams = new SearchParams();
  advParams: AdvancedParams = new AdvancedParams();

  fields: string[];
  facets: any;
  filters: Filters = new Filters();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private config: AppConfiguration,
    public state: AppState) { }

  ngOnInit() {
    // console.log(this.config.facets);
    this.subscriptions.push(this.state.facets.subscribe(f => {
      this.fields = Object.keys(f);
      this.facets = Object.assign({}, f);
    }));

    this.router.events.subscribe(val => {
      if (val instanceof NavigationEnd) {
        this.getParams(this.route.snapshot.queryParams);
      }
    });
    this.getParams(this.route.snapshot.queryParams);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(s => {
      s.unsubscribe();
      s = null;
    });

  }

  getParams(params: Params) {
    Utils.sanitize(params, this.searchParams);
    Utils.sanitize(params, this.advParams);
    this.filters = new Filters();
    for (const p in params) {
      if (this.filters.hasOwnProperty(p)) {
        if (p === 'zdroj') {
          if (params[p] instanceof Array) {
            this.filters[p] = params[p];
          } else {
            this.filters.zdroj.push(params[p]);
          }
        } else {
          this.filters[p] = params[p];
        }
      }
    }
  }

  hasZdroj(f: Facet, exclude: boolean): boolean {
    const prefix = exclude ? '-' : '';
    return this.filters.zdroj.includes(prefix + f.name);
  }

  toggleFilter(field: string, f: Facet) {
    if (this.filters[field] && this.filters[field] === f.name) {
      this.filters[field] = null;
    } else {
      this.filters[field] = f.name;
    }
    this.search();
  }

  toggleZdrojFilter(f: Facet) {
    if (this.filters.zdroj.includes(f.name)) {
      this.filters.zdroj = this.filters.zdroj.filter(z => z !== f.name);
    } else {
      this.filters.zdroj.push(f.name);
    }
    this.search();
  }

  addZdrojFilter(f: Facet) {
    if (this.filters.zdroj.includes(f.name)) {
      this.filters.zdroj = this.filters.zdroj.filter(z => z !== f.name);
    } else {
      this.filters.zdroj.push(f.name);
    }
    this.search();
  }

  excludeZdrojFilter(f: Facet) {
    if (this.filters.zdroj.includes(f.name)) {
      this.filters.zdroj = this.filters.zdroj.filter(z => z !== '-' + f.name);
    } else {
      this.filters.zdroj.push('-' + f.name);
    }
    this.search();
  }

  search() {
    const params = {...this.searchParams, ...this.advParams, ...this.filters};
    console.log(params);
    this.router.navigate(['/results'], {queryParams: params});
  }


}
