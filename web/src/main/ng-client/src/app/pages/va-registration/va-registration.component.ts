import { Component, OnInit } from '@angular/core';
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
    public config: AppConfiguration,
    private service: AppService
  ) { }

  ngOnInit() {
  }

  send() {
    console.log(this.user);
  }

}
