import { Component, OnInit } from '@angular/core';
import { User } from 'src/app/models/user';
import { AppState } from 'src/app/app.state';
import { Md5 } from 'ts-md5';
import { AppService } from 'src/app/app.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {

  user: User;

  constructor(
    public state: AppState,
    private service: AppService
  ) { }

  ngOnInit() {
    this.user = this.state.user;
  }

  save() {

    this.service.saveUser(this.user).subscribe(resp => {
      if (resp.error) {
        this.service.showSnackBar('user.save_error', '', 'app-snack-error');
      } else {
        this.service.showSnackBar('user.save_success', '', 'app-snack-success');
      }
    });

  }

}
