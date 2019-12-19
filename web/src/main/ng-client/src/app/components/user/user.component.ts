import { Component, OnInit, Input } from '@angular/core';
import { User } from 'src/app/models/user';
import { AppConfiguration } from 'src/app/app-configuration';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss']
})
export class UserComponent implements OnInit {

  @Input() user: User;
  @Input() type: string = 'registrace';

  
  userTypes: string[] = ['USER', 'LIBRARY'];
  userType = 'USER';

  constructor(
    public config: AppConfiguration) { }

  ngOnInit() {
    console.log(this.user);
    this.userType = this.user.role;
  }

}
