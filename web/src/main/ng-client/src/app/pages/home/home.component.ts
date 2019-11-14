import { Component, OnInit } from '@angular/core';
import { first } from 'rxjs/operators';

import { AppState } from 'src/app/app.state';
import { User } from 'src/app/models/user';
import { AppService } from 'src/app/app.service';
import { AuthenticationService } from 'src/app/shared';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})

export class HomeComponent implements OnInit {

  loading = false;
  user: User;

  constructor(
    private service: AppService,
    private authService: AuthenticationService,
    public state: AppState) { }

  ngOnInit() {
    this.loading = true;
    this.authService.currentUser.subscribe(x => this.user = x);
  }

}
