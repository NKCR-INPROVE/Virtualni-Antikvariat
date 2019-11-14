import { Component, OnInit } from '@angular/core';

import { AppState } from '../../app.state';
import { AppService } from '../../app.service';
import { ActivatedRoute, RouterStateSnapshot, Router } from '@angular/router';
import { AuthenticationService } from 'src/app/shared';
import { User } from 'src/app/models/user';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  currLang: string;
  user: User;

  constructor(
    private authService: AuthenticationService,
    private route: ActivatedRoute,
    private router: Router,
    public state: AppState,
    private service: AppService) {
  }

  ngOnInit() {
    this.authService.currentUser.subscribe(x => this.user = x);
    this.service.currentLang.subscribe((lang) => {
      this.currLang = lang;
    });
  }

  changeLang() {
    const lang: string = (this.currLang === 'cs' ? 'en' : 'cs');
    this.service.changeLang(lang);
  }

  logout() {
    this.authService.logout();
  }

  gologin() {
    this.state.redirectUrl = this.router.url;

    console.log(this.route, this.state.redirectUrl);
    this.router.navigate(['login']);
  }

}
