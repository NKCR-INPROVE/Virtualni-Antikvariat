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
  usedFilters: string[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private config: AppConfiguration,
    public state: AppState) { }

  ngOnInit() {
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
    this.searchParams = new SearchParams();
    this.advParams = new AdvancedParams();
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
    this.usedFilters = [];
    
    // Object.keys(this.filters).forEach(key => {
    //   console.log(key, this.filters[key]);
    //   if (this.filters[key] && this.filters[key] !== '') {
    //     this.usedFilters.push(key);
    //   }
    // });

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
    } else if (this.filters.zdroj.includes('-' + f.name)) {
      this.filters.zdroj = this.filters.zdroj.filter(z => z !== '-' + f.name);
    } else {
      this.filters.zdroj = [f.name, ...this.filters.zdroj];
    }
    this.search();
  }

  addZdrojFilter(f: Facet) {
    if (this.filters.zdroj.includes(f.name)) {
      this.filters.zdroj = this.filters.zdroj.filter(z => z !== f.name);
    } else if (this.filters.zdroj.includes('-' + f.name)) {
      this.filters.zdroj = this.filters.zdroj.filter(z => z !== '-' + f.name);
      this.filters.zdroj = [f.name, ...this.filters.zdroj];
    } else {
      this.filters.zdroj = [f.name, ...this.filters.zdroj];
    }
    this.search();
  }

  excludeZdrojFilter(f: Facet) {
    if (this.filters.zdroj.includes('-' + f.name)) {
      this.filters.zdroj = this.filters.zdroj.filter(z => z !== '-' + f.name);
    } else if (this.filters.zdroj.includes(f.name)) {
      this.filters.zdroj = this.filters.zdroj.filter(z => z !== f.name);
      this.filters.zdroj = ['-' + f.name, ...this.filters.zdroj];
    } else {
      this.filters.zdroj = ['-' + f.name, ...this.filters.zdroj];
    }
    this.search();
  }

  search() {
    const queryParams = Object.assign({}, { ...this.searchParams, ...this.advParams, ...this.filters });
    this.router.navigate(['/results'], { queryParams });
  }

  getOfferName(id: string): string {
    const o = this.state.offers.find(offer => offer.id === id);
    return o ? o.nazev : id;
  }

  toggleOffers() {
this.filters.offers ? this.filters.offers = null : this.filters.offers = true;
this.search();
  }

  toggleDemands() {
    this.filters.demands ? this.filters.demands = null : this.filters.demands = true;
    this.search();
  }


  toggleWanted() {
    this.filters.wanted ? this.filters.wanted = null : this.filters.wanted = true;
    this.search();
  }


  toggleMatches() {
    this.filters.matches ? this.filters.matches = null : this.filters.matches = true;
    this.search();
  }



}
