import { Component, OnInit } from '@angular/core';
import { AppConfiguration } from 'src/app/app-configuration';

@Component({
  selector: 'app-facets',
  templateUrl: './facets.component.html',
  styleUrls: ['./facets.component.scss']
})
export class FacetsComponent implements OnInit {

  constructor(private config: AppConfiguration) { }

  ngOnInit() {
    console.log(this.config.facets);
  }

}
