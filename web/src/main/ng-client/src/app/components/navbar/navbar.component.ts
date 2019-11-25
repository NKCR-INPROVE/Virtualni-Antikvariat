import { Component, OnInit } from '@angular/core';

import { AppState } from '../../app.state';
import { AppService } from '../../app.service';
import { ActivatedRoute, RouterStateSnapshot, Router } from '@angular/router';
import { AuthenticationService } from 'src/app/shared';
import { User } from 'src/app/models/user';
import { MatDialog } from '@angular/material';
import { LoginComponent } from 'src/app/components/login/login.component';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  currLang: string;
  user: User;

  isLogged: boolean;
  views: string[] = ['bohemika', 'ztrata'];

  constructor(
    public dialog: MatDialog,
    private authService: AuthenticationService,
    private route: ActivatedRoute,
    private router: Router,
    public state: AppState,
    private service: AppService) {
  }

  ngOnInit() {
    this.authService.currentUser.subscribe(x => this.isLogged = x !== null);
    this.authService.currentUser.subscribe(x => this.user = x);
    this.service.currentLang.subscribe((lang) => {
      this.currLang = lang;
    });
  }

  changeLang() {
    const lang: string = (this.currLang === 'cs' ? 'en' : 'cs');
    this.service.changeLang(lang);
  }

  demands() {

  }

  offers() {

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

}
