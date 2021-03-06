import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { View } from './models/view';
import { ResultsHeader } from './models/results-header';
import { Offer } from './models/offer';
import { User } from './models/user';
import { OfferRecord } from './models/offer-record';

@Injectable()
export class AppState {
  public isHome: boolean;
  public isReport: boolean;

  public user: User;
  public isLibrary: boolean;
  public isAdmin: boolean;

  private viewsSubject: BehaviorSubject<View[]> = new BehaviorSubject<View[]>([]);
  public views: Observable<View[]> = this.viewsSubject.asObservable();

  private resultsSubject: BehaviorSubject<any> = new BehaviorSubject<any>([]);
  public results: Observable<any[]> = this.resultsSubject.asObservable();

  private resultsHeaderSubject: BehaviorSubject<ResultsHeader> = new BehaviorSubject<ResultsHeader>(new ResultsHeader());
  public resultsHeader: Observable<ResultsHeader> = this.resultsHeaderSubject.asObservable();

  private facetsSubject: BehaviorSubject<any> = new BehaviorSubject<any>({});
  public facets: Observable<any> = this.facetsSubject.asObservable();

  public offers: Offer[] = [];
  public activeOffer: Offer;

  public shoppingCart: OfferRecord[] = [];

  setResults(resp) {
    this.resultsSubject.next(resp.response.docs);
    this.resultsHeaderSubject.next({
      numFound: resp.response.numFound,
      start: resp.response.start,
      rows: resp.responseHeader.params.rows
    });
    this.facetsSubject.next(resp.facet_counts.facet_fields);
  }

  setViews(resp) {
    this.viewsSubject.next(resp.response.docs);
  }

  setOffers(resp) {
    this.offers = resp;
    this.offers.forEach(offer => {
      if (!offer.closed) {
        this.activeOffer = offer;
      }
    });
  }

  setActiveOffer(offer: Offer) {
    this.activeOffer = offer;
  }

}

