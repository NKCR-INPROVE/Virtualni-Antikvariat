import { Component, OnInit, Input, OnDestroy, ViewContainerRef, TemplateRef } from '@angular/core';
import { Overlay, OverlayRef } from '@angular/cdk/overlay';
import { TemplatePortal } from '@angular/cdk/portal';
import { AppService } from 'src/app/app.service';
import { OfferRecord } from 'src/app/models/offer-record';
import { AppState } from 'src/app/app.state';
import { Exemplar, ExemplarZdroj } from 'src/app/models/exemplar';
import { Demand } from 'src/app/models/demand';
import { CsvComponent } from '../csv/csv.component';
import { MatDialog, MatSnackBar } from '@angular/material';
import { AppConfiguration } from 'src/app/app-configuration';
import { AddToOfferDialogComponent } from '../add-to-offer-dialog/add-to-offer-dialog.component';
import { Offer } from 'src/app/models/offer';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-result-item',
  templateUrl: './result-item.component.html',
  styleUrls: ['./result-item.component.scss']
})
export class ResultItemComponent implements OnInit, OnDestroy {
  private overlayRef: OverlayRef;
  @Input() doc;

  displayedColumns = ['zdroj', 'signatura', 'status', 'dilciKnih', 'rocnik_svazek', 'cislo', 'rok', 'buttons'];

  exemplars: Exemplar[];
  offers: string[];
  demands: string[];

  activeStatus: string = null;
  activeZdroj: string = null;
  isInCart: boolean;
  docInOffer: boolean;

  public tooltip: {
    field: string,
    text: string
  } = {
      field: '',
      text: ''
    };

  constructor(
    public dialog: MatDialog,
    private overlay: Overlay,
    private viewContainerRef: ViewContainerRef,
    private config: AppConfiguration,
    private service: AppService,
    public state: AppState
  ) { }

  ngOnInit() {
    this.setExemplars();
    this.isInCart = this.state.shoppingCart.findIndex(e => e.doc_code === this.doc.code) > -1;
    this.docInOffer = this.isInOffer();
  }

  setExemplars() {
    this.exemplars = [];
    if (this.doc.ex) {
      const exs: ExemplarZdroj[] = this.doc.ex;
      exs.forEach(exZdroj => {
        exZdroj.ex.forEach(ex => {
          ex.zdroj = exZdroj.zdroj;
          if (exZdroj.zdroj === 'UKF') {
            ex.knihovna = 'NKP';
          } else {
            ex.knihovna = exZdroj.zdroj;
          }
          if (ex.isNKF) {
            ex.zdroj = 'NKF';
          }
          ex.id = exZdroj.id;
          ex.isInOffer = this.isInOffer(ex);
          ex.isDemand = this.hasDemand(ex);
          ex.belongUser = this.belongUser(ex);

          this.exemplars.push(ex);
        });
      });
    }
  }

  ngOnDestroy() {
    this.closePop();
  }

  hasDifferences(field: string): boolean {
    const arr: Array<any> = this.doc[field];
    if (!arr) {
      return false;
    }
    if (!arr[0]) {
      return false;
    }
    return !arr.every(v => {
      if (v instanceof Array) {
        return JSON.stringify(v) === JSON.stringify(arr[0]);
      } else {
        return v === arr[0];
      }
    });
  }

  openPop(field: string, relative: any, template: TemplateRef<any>) {
    const arr: Array<string> = this.doc[field];
    this.tooltip = {
      field,
      text: arr.join('<br/>')
    };
    this.closeInfoOverlay();
    setTimeout(() => {
      this.openInfoOverlay(relative, template);
    }, 200);
  }

  closePop() {
    this.closeInfoOverlay();
  }

  openInfoOverlay(relative: any, template: TemplateRef<any>) {
    this.closeInfoOverlay();

    this.overlayRef = this.overlay.create({
      positionStrategy: this.overlay.position().flexibleConnectedTo(relative._elementRef).withPositions([{
        overlayX: 'end',
        overlayY: 'top',
        originX: 'center',
        originY: 'bottom'
      }]).withPush().withViewportMargin(30).withDefaultOffsetX(37).withDefaultOffsetY(20),
      scrollStrategy: this.overlay.scrollStrategies.close(),
      hasBackdrop: true,
      backdropClass: 'popover-backdrop'
    });
    this.overlayRef.backdropClick().subscribe(() => this.closeInfoOverlay());

    const portal = new TemplatePortal(template, this.viewContainerRef);
    this.overlayRef.attach(portal);
  }

  closeInfoOverlay() {
    if (this.overlayRef) {
      this.overlayRef.detach();
      this.overlayRef.dispose();
      this.overlayRef = null;
    }
  }

  openLink(id?: string) {
    if (id) {
      window.open('/api/original?id=' + id);
    } else {
      window.open('/api/original?id=' + this.doc.id[0]);
    }
  }

  addToOffer(ex?: Exemplar) {
    const record = new OfferRecord();
    record.knihovna = this.state.user.username;
    record.doc_code = this.doc.code;
    record.title = this.doc.title[0];
    if (ex) {
      record.exemplar = ex.md5;
      record.zaznam = ex.id;
      // record.fields = ex.
    }
    const dialogRef = this.dialog.open(AddToOfferDialogComponent, {
      width: '500px',
      data: record
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.service.addToOffer(result).subscribe(resp => {
          if (resp.error) {
            this.service.showSnackBar('snack_bar.add_error', '', true);
          } else {
            this.service.showSnackBar('snack_bar.add_success');
            if (ex) {
              ex.isInOffer = true;
            } else {
              this.docInOffer = true;
            }
          }
          console.log(ex);
        });
      }
    });
  }

  addToDemands(ex?: Exemplar) {
    const demand = new Demand();
    demand.knihovna = this.state.user.username;
    demand.doc_code = this.doc.code;
    demand.zaznam = this.doc.id[0];
    demand.title = this.doc.titlemd5[0];
    if (ex) {
      demand.zaznam = ex.id;
      demand.exemplar = ex.md5;
    }
    this.service.addToDemands(demand).subscribe(resp => {
      this.service.showSnackBar('snack_bar.add_to_demand_success');
    });
  }

  removeFromDemands(ex?: Exemplar) {
    const demand: Demand = this.doc.poptavka_ext.find(d => this.state.user.username === d.knihovna);
    console.log(demand);

    this.service.removeFromDemands(demand).subscribe(resp => {
      this.service.showSnackBar('snack_bar.remove_from_demand_success');
    });
  }

  csv() {
    const data = this.doc.export;
    const dialogRef = this.dialog.open(CsvComponent, {
      width: '500px',
      data
    });
  }

  hasIcon(zdroj: string) {
    return this.config.standardSources.includes(zdroj);
  }

  getOfferUser(id: string): string {
    let code = id;

    this.doc.nabidka_ext.forEach((offer: OfferRecord) => {
      if (offer.offer_id === id) {
        code = offer.knihovna;
      }
    });
    return code;
  }

  belongUser(ex: Exemplar): boolean {
    if (!this.state.user) {
      return false;
    }
    let kn = ex.knihovna;
    if (kn === 'UKF' || kn === 'NKF') {
      kn = 'NKP';
    }
    const exInLib = kn === this.state.user.username;
    const o = this.doc.ex.find(exe => ex.id === exe.id && exInLib);
    return o;
  }

  userHasDoc(): boolean {
    if (!this.state.user || !this.doc.zdroj) {
      return false;
    }
    if (this.state.user.username === 'NKP') {
      return this.doc.zdroj.includes('UKF') || this.doc.zdroj.includes('NKF');
    } else {
      return this.doc.zdroj.includes(this.state.user.username);
    }
  }

  isInOffer(ex?: Exemplar): boolean {
    if (!this.doc.nabidka) {
      return false;
    }

    const o = this.doc.nabidka_ext.find(exe => {
      let kn = exe.knihovna;
      if (kn === 'UKF' || kn === 'NKF') {
        kn = 'NKP';
      }
      const exInLib: boolean = kn === this.state.user.username;
      if (ex) {
        return ex.id === exe.zaznam && exInLib;
      } else {
        return exInLib;
      }

    });
    if (o) {
      return true;
    } else {
      return false;
    }

  }

  hasDemand(ex?: Exemplar): boolean {
    if (!this.state.user) {
      return false;
    }
    if (!this.doc.poptavka) {
      return false;
    }
    if (!this.doc.poptavka.includes(this.state.user.username)) {
      return false;
    }

    if (ex) {
      let kn = ex.knihovna;
      if (kn === 'UKF' || kn === 'NKF') {
        kn = 'NKP';
      }
      const exInLib = kn === this.state.user.username;
      const o = this.doc.poptavka_ext.find(exe => ex.id === exe.zaznam && exInLib);
      return o;
    } else {
      return true;
    }

  }

  toggleShopping(offer: OfferRecord) {
    if (this.isInCart) {
      const idx = this.state.shoppingCart.findIndex(e => e.doc_code === this.doc.code);
      this.service.removeFromShoppingCart(idx);
    } else {
      this.service.addToShoppingCart(offer);
    }
    this.isInCart = !this.isInCart;
  }

  addWanted(offer: OfferRecord, want: boolean) {
    if (this.state.isLibrary) {
      if (want) {
        if (!offer.chci) {
          offer.chci = [];
        }
        offer.chci.push(this.state.user.username);
      } else {
        if (!offer.nechci) {
          offer.nechci = [];
        }
        offer.nechci.push(this.state.user.username);
      }
      this.service.addToOffer(offer).subscribe(resp => {
        this.service.showSnackBar('snack_bar.response_to_offer', '');
      });
    } else {
      this.service.addToShoppingCart(offer);
    }
  }

  toggleStatus(status: string) {
    if (this.activeStatus !== null) {
      this.activeStatus = null;
    } else {
      this.activeStatus = status;
    }
  }

  toggleZdroj(zdroj: string) {
    if (this.activeZdroj !== null) {
      this.activeZdroj = null;
    } else {
      this.activeZdroj = zdroj;
    }
  }

  isRowHidden(row): boolean {
    return (this.activeStatus !== null && row.status !== this.activeStatus) ||
      (this.activeZdroj !== null && row.zdroj !== this.activeZdroj);
  }

  removeFromOffer(ex?: Exemplar) {
    // this.records = this.records.filter((val, index) => index !== idx);

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '350px',
      data: { title: 'offers.remove_from_offer' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {

        const o = this.doc.nabidka_ext.find(exe => {
          let kn = exe.knihovna;
          if (kn === 'UKF' || kn === 'NKF') {
            kn = 'NKP';
          }
          const exInLib = kn === this.state.user.username;
          if (ex) {
            return ex.id === exe.zaznam && exInLib;
          } else {
            return exInLib;
          }

        });

        const id = o.id;

        this.service.removeOfferRecord(id, this.doc.code).subscribe(resp => {
          if (resp.error) {
            this.service.showSnackBar('snack_bar.remove_from_offer_error', '', true);
          } else {
            this.service.showSnackBar('snack_bar.remove_from_offer_success');
            if (ex) {
              ex.isInOffer = false;
            } else {
              this.docInOffer = false;
            }
          }
        });
      }
    });

  }


}
