import { Component, OnInit, HostBinding } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { ActivatedRoute, Router, NavigationEnd } from '@angular/router';
import { AppState } from './app.state';
import { HomeComponent } from './pages/home/home.component';
import { AppService } from './app.service';
import { ReportComponent } from './pages/report/report.component';
import { AuthenticationService } from './shared';
import { OverlayContainer } from '@angular/cdk/overlay';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  @HostBinding('class') componentCssClass;
  
  constructor(
    private authService: AuthenticationService,
    private overlayContainer: OverlayContainer,
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

    this.authService.currentUser.subscribe(x => {
      const isLogged = x !== null;
      const lib = isLogged && x.role === 'LIBRARY';
      const theme = lib ? 'vdk-theme' : 'va-theme';
      this.overlayContainer.getContainerElement().classList.add(theme);
      this.componentCssClass = theme;
      this.service.getOffers().subscribe();
    });
    this.router.events.subscribe(val => {
      if (val instanceof NavigationEnd) {
        this.state.isHome = this.route.snapshot.firstChild.routeConfig.component === HomeComponent;
        this.state.isReport = this.route.snapshot.firstChild.routeConfig.component === ReportComponent;
        this.service.getViews().subscribe();
      }
    });

  }

}
