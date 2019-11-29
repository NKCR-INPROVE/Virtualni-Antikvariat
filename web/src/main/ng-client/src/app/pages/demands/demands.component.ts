import { Component, OnInit } from '@angular/core';
import { Demand } from 'src/app/models/demand';
import { AppService } from 'src/app/app.service';

@Component({
  selector: 'app-demands',
  templateUrl: './demands.component.html',
  styleUrls: ['./demands.component.scss']
})
export class DemandsComponent implements OnInit {

  demands: Demand[] = [];

  constructor(private service: AppService) { }

  ngOnInit() {
    this.refresh();
  }

  add() {
    const d = new Demand();
    this.demands.push(d);
  }

  remove(idx: number) {
    this.demands.splice(idx, 1);
  }

  refresh() {
    this.service.getDemands().subscribe(resp => { this.demands = resp; });
  }

}
