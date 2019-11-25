import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';

@Injectable()
export class AppState {
  public isHome: boolean;

  private resultsSubject: BehaviorSubject<any> = new BehaviorSubject<any>([]);
  public results: Observable<any[]> = this.resultsSubject.asObservable();

  private facetsSubject: BehaviorSubject<any> = new BehaviorSubject<any>({});
  public facets: Observable<any> = this.facetsSubject.asObservable();

  setResults(resp) {
    this.resultsSubject.next(resp.response.docs);
    console.log(resp.facet_counts.facet_fields);
    this.facetsSubject.next(resp.facet_counts.facet_fields);
  }

}

