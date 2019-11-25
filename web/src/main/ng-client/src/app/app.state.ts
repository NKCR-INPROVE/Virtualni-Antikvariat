import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { View } from './models/view';

@Injectable()
export class AppState {
  public isHome: boolean;

  private viewsSubject: BehaviorSubject<View[]> = new BehaviorSubject<View[]>([]);
  public views: Observable<View[]> = this.viewsSubject.asObservable();

  private resultsSubject: BehaviorSubject<any> = new BehaviorSubject<any>([]);
  public results: Observable<any[]> = this.resultsSubject.asObservable();

  private facetsSubject: BehaviorSubject<any> = new BehaviorSubject<any>({});
  public facets: Observable<any> = this.facetsSubject.asObservable();

  setResults(resp) {
    this.resultsSubject.next(resp.response.docs);
    this.facetsSubject.next(resp.facet_counts.facet_fields);
  }

  setViews(resp) {
    this.viewsSubject.next(resp.response.docs);
  }

}

