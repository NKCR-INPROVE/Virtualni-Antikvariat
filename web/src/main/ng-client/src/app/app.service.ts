import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

import { Observable, ReplaySubject } from 'rxjs';

import { map } from 'rxjs/operators';

import { HttpClient, HttpParams } from '@angular/common/http';

import { AppState } from './app.state';
import { User } from './models/user';
import { View } from './models/view';
import { Demand } from './models/demand';
import { OfferRecord } from './models/offer-record';
import { Offer } from './models/offer';
import { MatSnackBar } from '@angular/material';
import { Job } from './models/job';
import { Cart } from './models/cart';

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
    private snackBar: MatSnackBar) {

  }

  showSnackBar(s: string, r: string = '', error: boolean = false) {
    const right = r !== '' ? this.getTranslation(r) : '';
    const clazz = error ? 'app-snack-error' : 'app-snack-success';
    this.snackBar.open(this.getTranslation(s), right, {
      duration: 2000,
      verticalPosition: 'top',
      panelClass: clazz
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
    return this.http.get<any>(`/api/offers/removerecord`, { params })
      .pipe(map(resp => {
        return resp;
      }));
  }


  saveView(v: View) {
    return this.http.post<View>(`/api/users/save_view`, v);
  }

  getUser(code: string) {
    const params: HttpParams = new HttpParams().set('code', code);
    return this.http.get<any>(`/api/users/info`, { params })
      .pipe(map(resp => {
        return resp;
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

  resetHeslo(json: { code: string, oldheslo: string, newheslo: string }) {
    return this.http.post<any>(`/api/users/resetpwd`, json)
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
    return this.http.get<any>(`/api/sched/startjob`, { params })
      .pipe(map(resp => {
        return resp;
      }));
  }


  stopJob(job: Job) {
    const params: HttpParams = new HttpParams().set('key', job.jobKey);
    return this.http.get<any>(`/api/sched/stopjob`, { params })
      .pipe(map(resp => {
        return resp;
      }));
  }

  checkUserExists(username: string): Observable<boolean> {
    const params: HttpParams = new HttpParams().set('username', username);
    return this.http.get<any>(`/api/users/check`, { params })
      .pipe(map(resp => {
        if (resp.error) {
          return false;
        } else {
          return resp.exists;
        }
      }));
  }

  addToShoppingCart(record: OfferRecord) {
    this.state.shoppingCart.push(record);
    if (this.state.user) {
      this.storeCart(record).subscribe();
    }
    localStorage.setItem('shoppingCart', JSON.stringify(this.state.shoppingCart));
  }

  removeFromShoppingCart(idx: number) {
    this.state.shoppingCart.splice(idx, 1);
    localStorage.setItem('shoppingCart', JSON.stringify(this.state.shoppingCart));
  }

  getShoppingCart() {
    if (this.state.user) {
      this.retrieveShoppingCart().subscribe( resp => this.state.shoppingCart = resp.cart);
    } else {
      if (localStorage.getItem('shoppingCart')) {
        this.state.shoppingCart = JSON.parse(localStorage.getItem('shoppingCart'));
      }
    }

  }

  retrieveShoppingCart() {
    return this.http.get<any>(`/api/users/cart`);
  }

  storeCart(record: OfferRecord) {
    const data = { user: this.state.user.code, item: record };
    return this.http.post<any>(`/api/users/storecart`, data);
  }

  orderCart(orderData: Cart) {
    // const data = { orderData, cart: this.state.shoppingCart };
    return this.http.post<any>(`/api/users/ordercart`, orderData);
  }

  getOrders() {
    return this.http.get<any>(`/api/users/orders`);
  }

  getCenik() {
    return this.http.get<any>(`/api/users/cenik`);
  }

}
