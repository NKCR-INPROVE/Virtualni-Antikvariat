import { Component, OnInit, ElementRef } from '@angular/core';
import { User } from 'src/app/models/user';
import { AppConfiguration } from 'src/app/app-configuration';
import { AppService } from 'src/app/app.service';

@Component({
  selector: 'app-va-registration',
  templateUrl: './va-registration.component.html',
  styleUrls: ['./va-registration.component.scss']
})
export class VaRegistrationComponent implements OnInit {

  user: User = new User();

  constructor(
    private el: ElementRef,
    public config: AppConfiguration,
    private service: AppService
  ) { }

  ngOnInit() {
  }

  send() {
    console.log(this.user);
    // Check username uniqueness
    this.service.checkUserExists(this.user.username).subscribe(exists => {
      
      if (exists) {
        this.service.showSnackBar('user.alreadyExists');
        this.el.nativeElement.querySelector('#username').focus();
      } else {
        this.service.addUser(this.user).subscribe(resp => {
          if (resp.error) {
            this.service.showSnackBar('user.send_error', '', 'app-snack-error');
          } else {
            this.service.showSnackBar('user.send_success', '', 'app-snack-success');
          }
        });
      }
    });
  }

}
