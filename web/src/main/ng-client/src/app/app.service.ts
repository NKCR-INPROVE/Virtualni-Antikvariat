import { Injectable } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { DatePipe } from '@angular/common';
import { TranslateService } from '@ngx-translate/core';

import { Observable, BehaviorSubject, ReplaySubject } from 'rxjs';
import { Subject } from 'rxjs';
import { map } from 'rxjs/operators';

import { HttpClient, HttpParams, HttpHeaders, HttpErrorResponse } from '@angular/common/http';

import { AppState } from './app.state';
import { User } from './models/user';
import { View } from './models/view';
import { Demand } from './models/demand';
import { OfferRecord } from './models/offer-record';

@Injectable({
  providedIn: 'root'
})
export class AppService {


  // Observe language
  private langSubject: ReplaySubject<string> = new ReplaySubject(3);
  public currentLang: Observable<string> = this.langSubject.asObservable();

  constructor(
    private state: AppState,
    private translate: TranslateService,
    private http: HttpClient,
    private router: Router,
    private datePipe: DatePipe) {

  }

  changeLang(lang: string) {
    // console.log('lang changed to ' + lang);

    this.translate.use(lang).subscribe(val => {
      this.langSubject.next(lang);
    });
  }

  // public get currentLangValue(): string {
  //   return this.langSubject.;
  // }

  search(params: HttpParams) {
    // const params: HttpParams = new HttpParams().set('wt', 'json');
    return this.http.get<any>(`/api/search/query`, { params })
      .pipe(map(resp => {
        // store response
        this.state.setResults(resp);
        return resp;
      }));
  }

  getViews() {
    return this.http.get<any>(`/api/users/views`)
      .pipe(map(resp => {
        // store response
        this.state.setViews(resp);
        return resp;
      }));
  }

  getDemands() {
    return this.http.get<any>(`/api/demands/all`)
    .pipe(map(resp => {
      return resp.docs;
    }));
  }

  addToDemands(demand: Demand) {
    return this.http.post<any>(`/api/demands/add`, demand)
    .pipe(map(resp => {
      return resp.docs;
    }));
  }

  getOffers() {
    return this.http.get<any>(`/api/offers/all`)
    .pipe(map(resp => {
      this.state.setOffers(resp.docs);
      return resp.docs;
    }));
  }

  getOffer(id: string) {
    const params: HttpParams = new HttpParams().set('id', id);
    return this.http.get<any>(`/api/offers/byid`, {params})
    .pipe(map(resp => {
      return resp.docs;
    }));
  }

  addToOffer(offer: OfferRecord) {
    return this.http.post<any>(`/api/offers/addrecord`, offer)
    .pipe(map(resp => {
      return resp.docs;
    }));
  }


  saveView(v: View) {
    return this.http.post<View>(`/api/users/save_view`, v);
  }

}
