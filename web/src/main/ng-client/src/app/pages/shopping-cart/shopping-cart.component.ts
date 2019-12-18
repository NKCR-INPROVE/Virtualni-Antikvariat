import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { AppService } from 'src/app/app.service';
import { AppState } from 'src/app/app.state';
import { OfferRecord } from 'src/app/models/offer-record';
import { User } from 'src/app/models/user';

@Component({
  selector: 'app-shopping-cart',
  templateUrl: './shopping-cart.component.html',
  styleUrls: ['./shopping-cart.component.scss']
})
export class ShoppingCartComponent implements OnInit {

  displayedColumns = ['text', 'cena', 'button'];
  totalCost: number;

  data: OfferRecord[];

  constructor(
    private changeDetectorRefs: ChangeDetectorRef,
    private service: AppService,
    public state: AppState) { }

  ngOnInit() {
    this.refresh();
  }

  order() {
    let user: User;

    if(this.state.user) {
      user = this.state.user;
    } else {
      user = new User();
    }

    this.service.orderCart(user).subscribe(resp => {});
  }

  remove(idx: number) {
    this.service.removeFromShoppingCart(idx);
    this.refresh();
  }

  refresh() {
    this.data = Object.assign([], this.state.shoppingCart);
    this.totalCost = this.data.map(t => t.cena).reduce((acc, value) => acc + value, 0);
  }

}
