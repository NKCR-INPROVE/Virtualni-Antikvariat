import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { ActivatedRoute, Router, NavigationEnd } from '@angular/router';
import { AppState } from './app.state';
import { HomeComponent } from './pages/home/home.component';
import { AppService } from './app.service';
import { ReportComponent } from './pages/report/report.component';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  
  constructor(
    translate: TranslateService,
    private route: ActivatedRoute,
    private router: Router,
    public state: AppState,
    private service: AppService) {
    // this language will be used as a fallback when a translation isn't found in the current language
    translate.setDefaultLang('cs');
    this.service.changeLang('cs');
  }

  ngOnInit() {
    this.router.events.subscribe(val => {
      if (val instanceof NavigationEnd) {
        this.state.isHome = this.route.snapshot.firstChild.routeConfig.component === HomeComponent;
        this.state.isReport = this.route.snapshot.firstChild.routeConfig.component === ReportComponent;
        this.service.getViews().subscribe();
      }
    });

  }

}
