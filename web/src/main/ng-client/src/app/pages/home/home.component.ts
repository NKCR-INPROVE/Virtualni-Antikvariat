import { Component, OnInit } from '@angular/core';
import { first } from 'rxjs/operators';

import { AppState } from 'src/app/app.state';
import { User } from 'src/app/models/user';
import { AppService } from 'src/app/app.service';
import { AuthenticationService } from 'src/app/shared';
import { ActivatedRoute } from '@angular/router';
import { MatDialog } from '@angular/material';
import { LoginComponent } from 'src/app/components/login/login.component';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})

export class HomeComponent implements OnInit {

  loading = false;
  user: User;

  constructor(
    public dialog: MatDialog,
    private service: AppService,
    private authService: AuthenticationService,
    private route: ActivatedRoute,
    public state: AppState) { }

  ngOnInit() {
    this.loading = true;
    this.authService.currentUser.subscribe(x => this.user = x);
    console.log(this.route.snapshot.queryParams);
    if (this.route.snapshot.queryParams['login'])  {
      this.login();
    }
  }

  login() {

    const dialogRef = this.dialog.open(LoginComponent, {
      width: '350px'
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log(result);
      if (result) {

      }
    });
  }


}
