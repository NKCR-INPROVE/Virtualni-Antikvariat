import { Component, OnInit, Input, OnDestroy, ViewContainerRef, TemplateRef } from '@angular/core';
import { Overlay, OverlayRef } from '@angular/cdk/overlay';
import { TemplatePortal } from '@angular/cdk/portal';
import { AppService } from 'src/app/app.service';
import { OfferRecord } from 'src/app/models/offer-record';
import { AppState } from 'src/app/app.state';
import { Exemplar, ExemplarZdroj } from 'src/app/models/exempplar';
import { Demand } from 'src/app/models/demand';
import { CsvComponent } from '../csv/csv.component';
import { MatDialog, MatSnackBar } from '@angular/material';
import { AppConfiguration } from 'src/app/app-configuration';
import { AddToOfferDialogComponent } from '../add-to-offer-dialog/add-to-offer-dialog.component';

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

  public tooltip: {
    field: string,
    text: string
  } = {
      field: '',
      text: ''
    };

  constructor(
    public dialog: MatDialog,
    private snackBar: MatSnackBar,
    private overlay: Overlay,
    private viewContainerRef: ViewContainerRef,
    private config: AppConfiguration,
    private service: AppService,
    public state: AppState
  ) { }

  ngOnInit() {
    this.setExemplars();
  }

  setExemplars() {
    this.exemplars = [];
    if (this.doc.ex) {
      const exs: ExemplarZdroj[] = this.doc.ex;
      exs.forEach(exZdroj => {
        exZdroj.ex.forEach(ex => {
          ex.zdroj = exZdroj.zdroj;
          if (ex.isNKF) {
            ex.zdroj = 'NKF';
          }
          ex.id = exZdroj.id;
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
      window.open('/api/original?id=' + this.doc.id);
    }
  }

  addToOffer(ex?: Exemplar) {
    const record = new OfferRecord();
    record.knihovna = this.state.user.code;
    record.doc_code = this.doc.code;
    record.title = this.doc.title[0];
    const dialogRef = this.dialog.open(AddToOfferDialogComponent, {
      width: '500px',
      data: record
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.service.addToOffer(record).subscribe();
      }
    });


  }

  addToDemands(ex?: Exemplar) {
    const demand = new Demand();
    demand.knihovna = this.state.user.code;
    demand.zaznam = this.doc.code;
    demand.title = this.doc.titlemd5[0];
    if (ex) {
      demand.zaznam = ex.id;
      demand.exemplar = ex.md5;
    }
    this.service.addToDemands(demand).subscribe(resp => {
      this.snackBar.open('Doc added to demands', '', {
        duration: 2000,
        verticalPosition : 'top'
      });
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
}
