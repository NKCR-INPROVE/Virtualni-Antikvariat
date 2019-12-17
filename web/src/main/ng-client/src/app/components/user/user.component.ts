import { Component, OnInit, Input } from '@angular/core';
import { User } from 'src/app/models/user';

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

  constructor() { }

  ngOnInit() {
    this.userType = this.user.role
  }

}
