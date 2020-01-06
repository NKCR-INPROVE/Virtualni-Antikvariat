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

  favicoId = 'vdk-favicon';

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

    this.removeExternalLinkElements();
    this.authService.currentUser.subscribe(x => {
      const isLogged = x !== null;
      this.state.user = x;
      this.state.isLibrary = isLogged && this.state.user.role === 'LIBRARY';
      const lib = isLogged && x.role === 'LIBRARY';
      const theme = lib ? 'vdk-theme' : 'va-theme';
      this.overlayContainer.getContainerElement().classList.add(theme);
      this.componentCssClass = theme;
      const favico = lib ? 'vdk-ico.png' : 'va-ico.png';
      this.setFavIcon('assets/img/' + favico);
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

      // I remove the favicon node from the document header.
      private removeFavIcon(): void {

        const linkElement = document.head.querySelector( '#' + this.favicoId );
        if ( linkElement ) {
            document.head.removeChild( linkElement );
        }
    }

    private setFavIcon( href: string ): void {

        this.removeFavIcon();
        this.addFavIcon( href );

    }

  private addFavIcon(href: string): void {
    const linkElement = document.createElement('link');
    linkElement.setAttribute('id', this.favicoId);
    linkElement.setAttribute('rel', 'icon');
    linkElement.setAttribute('type', 'image/x-icon');
    linkElement.setAttribute('href', href);
    document.head.appendChild(linkElement);
  }

  private removeExternalLinkElements(): void {
    const linkElements = document.querySelectorAll('link[ rel ~= \'icon\' i]');
    for (const linkElement of Array.from(linkElements)) {
      linkElement.parentNode.removeChild(linkElement);
    }
  }


}
