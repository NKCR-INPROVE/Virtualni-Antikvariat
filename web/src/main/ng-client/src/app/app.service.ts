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
    return this.http.get<Demand[]>(`/api/demands/all`)
    .pipe(map(resp => {
      return resp['docs'];
    }));
  }


  saveView(v: View) {
    return this.http.post<View>(`/api/users/save_view`, v);
  }

}
