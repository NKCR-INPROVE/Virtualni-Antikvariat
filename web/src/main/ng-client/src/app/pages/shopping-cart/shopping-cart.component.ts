import { Component, OnInit, ChangeDetectorRef, Inject } from '@angular/core';
import { AppService } from 'src/app/app.service';
import { AppState } from 'src/app/app.state';
import { OfferRecord } from 'src/app/models/offer-record';
import { User } from 'src/app/models/user';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialog } from '@angular/material';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

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
    public dialog: MatDialog,
    private changeDetectorRefs: ChangeDetectorRef,
    private service: AppService,
    public state: AppState) { }

  ngOnInit() {
    this.refresh();
  }

  getCart() {
    this.service.retrieveShoppingCart().subscribe(resp => { console.log(resp); });
  }

  remove(idx: number) {
    this.service.removeFromShoppingCart(idx);
    this.refresh();
  }

  refresh() {
    this.data = Object.assign([], this.state.shoppingCart);
    this.totalCost = this.data.map(t => t.cena).reduce((acc, value) => acc + value, 0);
  }

  order() {
    let user: User;

    if (this.state.user) {
      user = this.state.user;
    } else {
      const data = new User();
      const dialogRef = this.dialog.open(OrderCartDialogComponent, {
        width: '350px',
        data
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          console.log(result);
          // this.service.orderCart(result).subscribe(resp => { });
        }
      });
    }
  }

}

@Component({
  selector: 'app-order-cart-dialog',
  templateUrl: 'order-cart-dialog.html',
})
export class OrderCartDialogComponent implements OnInit {

  userForm: FormGroup;

  constructor(
    private formBuilder: FormBuilder,
    public dialogRef: MatDialogRef<OrderCartDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public user: User) { }


  get f() { return this.userForm.controls; }

  ngOnInit() {
    this.userForm = this.formBuilder.group({
      oldheslo: ['', Validators.required],
      newheslo: ['', Validators.required]
    });
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  reset(): void {
    this.user = new User();
  }

  ok() {
    if (this.userForm.invalid) {
      return;
    }
    this.dialogRef.close(this.user);
  }


}
