import { Component, OnInit } from '@angular/core';

import { AppState } from '../../app.state';
import { AppService } from '../../app.service';
import { ActivatedRoute, RouterStateSnapshot, Router } from '@angular/router';
import { AuthenticationService } from 'src/app/shared';
import { User } from 'src/app/models/user';
import { MatDialog } from '@angular/material';
import { LoginComponent } from 'src/app/components/login/login.component';
import { View } from 'src/app/models/view';
import { ViewComponent } from '../view/view.component';
import { Utils } from 'src/app/shared/utils';
import { TranslateService } from '@ngx-translate/core';
import { DemandsComponent } from 'src/app/pages/demands/demands.component';
import { OffersComponent } from 'src/app/pages/offers/offers.component';
import { Md5 } from 'ts-md5';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  // Keep loaded langs, so we don't add duplicated routes
  loadedLangs: string[] = [];
  currLang: string;
  user: User;

  isLogged: boolean;
  views: View[] = [];
  selectedView: View = null;

  constructor(
    public dialog: MatDialog,
    private authService: AuthenticationService,
    private route: ActivatedRoute,
    private router: Router,
    private translate: TranslateService,
    public state: AppState,
    private service: AppService) {
  }

  ngOnInit() {

    this.authService.currentUser.subscribe(x => {
      this.user = x;
      this.state.user = x;
      this.isLogged = x !== null;
      this.state.isLibrary = this.isLogged && this.state.user.role === 'KNIHOVNA';
      this.service.getOffers().subscribe();
    });
    this.service.currentLang.subscribe((lang) => {
      this.currLang = lang;
      if (!this.loadedLangs.includes(lang)) {
        const l = this.router.config.length - 1;
        this.router.config.splice(l, 0, {path: this.translate.instant('route.demands'), component: DemandsComponent });
        this.router.config.splice(l, 0, {path: this.translate.instant('route.offers'), component: OffersComponent });
        this.loadedLangs.push(lang);
      }

    });
    this.state.views.subscribe((views) => {
      this.views = views;
    });
  }

  changeLang() {
    const lang: string = (this.currLang === 'cs' ? 'en' : 'cs');
    this.service.changeLang(lang);
  }

  changeView() {
    this.router.navigate(['/results'], { queryParams: this.selectedView.params });
  }

  saveCurrentView() {
    this.selectedView.params = this.route.snapshot.queryParams;
    this.service.saveView(this.selectedView).subscribe();
  }

  demands() {
    const r = this.translate.instant('route.demands');
    this.router.navigate([r]);
  }

  offers() {
    const r = this.translate.instant('route.offers');
    this.router.navigate([r]);
  }

  export() {

  }

  logout() {
    this.authService.logout();
  }

  login() {

    const dialogRef = this.dialog.open(LoginComponent, {
      width: '350px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {

      }
    });
  }

  openViewDialog(): void {
    const data = {
      params: this.route.snapshot.queryParams,
      user: this.user.code,
      name: '',
      overwrite: false,
      global: true,
      id: null
    };

    if (this.selectedView) {
      data.overwrite = true;
      data.name = this.selectedView.name;
      data.global = this.selectedView.global;
      data.id = this.selectedView.id;
    }

    const dialogRef = this.dialog.open(ViewComponent, {
      width: '350px',
      data
    });

    dialogRef.afterClosed().subscribe(ret => {
      if (ret) {
        const v: View = new View();
        Utils.sanitize(ret, v);
        if (!ret.overwrite) {
          v.id = null;
        }
        this.service.saveView(v).subscribe(res => {
          if (res) {
            this.views.push(v);
          }
        });
      }
    });
  }


}
