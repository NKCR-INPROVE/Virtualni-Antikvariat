import { Component, OnInit, ChangeDetectorRef, Inject } from '@angular/core';
import { AppService } from 'src/app/app.service';
import { AppState } from 'src/app/app.state';
import { OfferRecord } from 'src/app/models/offer-record';
import { User } from 'src/app/models/user';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialog } from '@angular/material';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { STEPPER_GLOBAL_OPTIONS } from '@angular/cdk/stepper';
import { AppConfiguration } from 'src/app/app-configuration';

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
      const data = { user: new User(), cart: this.data };
      const dialogRef = this.dialog.open(OrderCartDialogComponent, {
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
  providers: [{
    provide: STEPPER_GLOBAL_OPTIONS, useValue: { showError: true }
  }]
})
export class OrderCartDialogComponent implements OnInit {

  userForm: FormGroup;
  dopravaForm: FormGroup;

  knihovny: string[] = [];
  cenik: { code: string, username: string, doprava: string[] }[];

  constructor(
    private service: AppService,
    public config: AppConfiguration,
    private formBuilder: FormBuilder,
    public dialogRef: MatDialogRef<OrderCartDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { user: User, cart: OfferRecord[], doprava: { [key: string]: string } }) { }


  get f() { return this.userForm.controls; }

  ngOnInit() {

    this.userForm = this.formBuilder.group({
      nazev: ['', Validators.required],
      adresa: ['', Validators.required],
      telefon: ['', Validators.required],
      email: ['', Validators.required]
    });

    this.service.getCenik().subscribe(resp => {
      this.cenik = resp.docs;

      const dp = {};
      this.data.cart.forEach(record => {
        if (!this.knihovny.includes(record.knihovna)) {
          this.knihovny.push(record.knihovna);
          dp[record.knihovna] = ['', Validators.required];
        }
      });

      this.dopravaForm = this.formBuilder.group(dp);
    });
  }

  getDoprava(kn: string) {
    if (this.cenik.length > 0) {
      const info = this.cenik.find(c => c.username === kn);
      return info.doprava;
    }
  }

  getCena(kn: string) {
    this.cenik.forEach(c => {

    });
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  reset(): void {
    // this.user = new User();
  }

  ok() {
    if (this.userForm.invalid) {
      return;
    }

    this.data.user.nazev = this.f.nazev.value;
    this.data.user.adresa = this.f.adresa.value;
    this.data.user.telefon = this.f.telefon.value;
    this.data.user.email = this.f.email.value;

    this.data.doprava = this.dopravaForm.value;

    console.log(this.data);
    this.service.orderCart(this.data).subscribe(resp => {
      console.log(resp);
      // this.dialogRef.close(this.data);
    });

  }


}
