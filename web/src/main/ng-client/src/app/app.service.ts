import { Injectable } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { DatePipe } from '@angular/common';
import { TranslateService } from '@ngx-translate/core';

import { Observable, BehaviorSubject } from 'rxjs';
import { Subject } from 'rxjs';

import { HttpClient, HttpParams, HttpHeaders, HttpErrorResponse } from '@angular/common/http';

import { AppState } from './app.state';
import { User } from './models/user';

@Injectable({
  providedIn: 'root'
})
export class AppService {

  // Observe language
  private langSubject: BehaviorSubject<string> = new BehaviorSubject('cs');
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
    
    this.translate.use(lang);
    this.langSubject.next(lang);
  }

  public get currentLangValue(): string {
    return this.langSubject.value;
  }

}
