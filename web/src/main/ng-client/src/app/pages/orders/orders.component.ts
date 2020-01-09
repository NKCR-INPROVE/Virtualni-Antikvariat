import { Component, OnInit } from '@angular/core';
import { Cart } from 'src/app/models/cart';
import { OfferRecord } from 'src/app/models/offer-record';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';
import { AppService } from 'src/app/app.service';
import { AppState } from 'src/app/app.state';

@Component({
  selector: 'app-orders',
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.scss']
})
export class OrdersComponent implements OnInit {

  displayedColumns = ['text', 'price'];
  orders: Cart[] = [];
  records: OfferRecord[];
  current: Cart;

  constructor(
    private router: Router,
    public dialog: MatDialog,
    private service: AppService,
    private state: AppState) { }

  ngOnInit() {
    if (this.state.isLibrary) {
      this.refresh();
    } else {
      this.router.navigate(['/home']);
    }
  }

  refresh() {
    this.service.getOrders().subscribe(resp => {
      this.orders = resp.orders;
      if (!this.current) {
        this.load(this.orders[0]);
      }
    });
  }

  load(order: Cart) {
    this.current = order;
    this.records = order.item;
  }

  process() {
    this.current.status = 'processed';
    this.service.processOrder(this.current).subscribe(resp => {

      if (resp.error) {
        this.service.showSnackBar('cart.process_error', '', true);
        this.current.status = 'new';
      } else {
        this.service.showSnackBar('cart.process_success');
      }
    });
  }

}
