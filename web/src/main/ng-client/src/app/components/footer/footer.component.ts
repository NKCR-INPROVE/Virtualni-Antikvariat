import { Component, OnInit } from '@angular/core';
import { AppConfiguration } from 'src/app/app-configuration';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss']
})
export class FooterComponent implements OnInit {

  constructor(
    public config: AppConfiguration) { }

  ngOnInit() {
  }

}
