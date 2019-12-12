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
import { Offer } from './models/offer';
import { MatSnackBar } from '@angular/material';
import { Job } from './models/job';

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
    private snackBar: MatSnackBar,
    private datePipe: DatePipe) {

  }

  showSnackBar(s: string, r: string = '', p: string = '') {

    this.snackBar.open(this.getTranslation(s), r, {
      duration: 20000,
      verticalPosition: 'top',
      panelClass: p
    });
  }

  changeLang(lang: string) {
    // console.log('lang changed to ' + lang);

    this.translate.use(lang).subscribe(val => {
      this.langSubject.next(lang);
    });
  }

  getTranslation(s: string): string {
    return this.translate.instant(s);
  }

  // public get currentLangValue(): string {
  //   return this.langSubject.last();
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

  removeFromDemands(demand: Demand) {
    return this.http.post<any>(`/api/demands/remove`, demand)
      .pipe(map(resp => {
        return resp;
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
    return this.http.get<any>(`/api/offers/byid`, { params })
      .pipe(map(resp => {
        return resp.docs[0];
      }));
  }


  getOfferRecords(id: string) {
    const params: HttpParams = new HttpParams().set('id', id);
    return this.http.get<any>(`/api/offers/records`, { params })
      .pipe(map(resp => {
        return resp.docs;
      }));
  }

  addToOffer(offer: OfferRecord) {
    return this.http.post<any>(`/api/offers/addrecord`, offer)
      .pipe(map(resp => {
        return resp;
      }));
  }

  addOffer(offer: Offer) {
    return this.http.post<any>(`/api/offers/add`, offer)
      .pipe(map(resp => {
        return resp;
      }));
  }

  removeOffer(offer: Offer) {
    return this.http.post<any>(`/api/offers/remove`, offer)
      .pipe(map(resp => {
        return resp;
      }));
  }

  removeOfferRecord(id: string) {
    const params: HttpParams = new HttpParams().set('id', id);
    return this.http.get<any>(`/api/offers/removerecord`, {params})
      .pipe(map(resp => {
        return resp;
      }));
  }


  saveView(v: View) {
    return this.http.post<View>(`/api/users/save_view`, v);
  }

  getUser(code: string) {
    const params: HttpParams = new HttpParams().set('code', code);
    return this.http.get<any>(`/api/users/info`, {params})
      .pipe(map(resp => {
        return resp.response.docs[0];
      }));
  }

  addUser(user: User) {
    return this.http.post<any>(`/api/users/add`, user)
      .pipe(map(resp => {
        return resp;
      }));
  }

  saveUser(user: User) {
    return this.http.post<any>(`/api/users/save`, user)
      .pipe(map(resp => {
        return resp;
      }));
  }

  getUsers() {
    return this.http.get<any>(`/api/users/all`)
      .pipe(map(resp => {
        return resp.docs;
      }));
  }

  getJobs() {
    return this.http.get<any>(`/api/sched/getjobs`)
      .pipe(map(resp => {
        return resp;
      }));
  }

  startJob(job: Job) {
    const params: HttpParams = new HttpParams().set('key', job.jobKey);
    return this.http.get<any>(`/api/sched/startjob`, {params})
      .pipe(map(resp => {
        return resp;
      }));
  }


  stopJob(job: Job) {
    const params: HttpParams = new HttpParams().set('key', job.jobKey);
    return this.http.get<any>(`/api/sched/stopjob`, {params})
      .pipe(map(resp => {
        return resp;
      }));
  }

  checkUserExists(username: string): Observable<boolean> {
    const params: HttpParams = new HttpParams().set('username', username);
    return this.http.get<any>(`/api/users/check`, {params})
      .pipe(map(resp => {
        if (resp.error) {
          return false;
        } else {
          return resp.exists;
        }
      }));
  }

}
