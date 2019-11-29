import { Component, OnInit, Input, OnDestroy, ViewContainerRef, TemplateRef } from '@angular/core';
import { Overlay, OverlayRef } from '@angular/cdk/overlay';
import { TemplatePortal } from '@angular/cdk/portal';
import { AppService } from 'src/app/app.service';
import { OfferRecord } from 'src/app/models/offer-record';
import { AppState } from 'src/app/app.state';

@Component({
  selector: 'app-result-item',
  templateUrl: './result-item.component.html',
  styleUrls: ['./result-item.component.scss']
})
export class ResultItemComponent implements OnInit, OnDestroy {
  private overlayRef: OverlayRef;
  @Input() doc;

  // demo - petr
  displayedColumns = ['zdroj', 'signatura', 'status', 'dilciKnih', 'rocnik_svazek', 'cislo', 'rok', 'buttons'];
  dataSource = ELEMENT_DATA;
  // end demo - petr

  public tooltip: {
    field: string,
    text: string
  } = {
      field: '',
      text: ''
    };

  constructor(
    private overlay: Overlay,
    private viewContainerRef: ViewContainerRef,
    private service: AppService,
    public state: AppState
  ) { }

  ngOnInit() {
  }

  ngOnDestroy() {
    this.closePop();
  }

  hasDifferences(field: string): boolean {
    const arr: Array<string> = this.doc[field];
    return !arr.every(v => v === arr[0]);
  }

  openPop(field: string, relative: any, template: TemplateRef<any>) {
    const arr: Array<string> = this.doc[field];
    this.tooltip = {
      field: field,
      text: arr.join('<br/>')
    };
    console.log(this.tooltip);
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

  openLink() {
    window.open('/api/original/' + this.doc.id);
  }

  addToOffer() {
    const of = new OfferRecord();
    of.knihovna = this.state.user.code;
    of.offer_id = this.state.activeOffer.id;
    of.doc_code = this.doc.code;
    of.title = this.doc.title[0];
    this.service.addToOffer(of).subscribe();
  }

  addToDemands() {
    this.service.addToDemands(this.doc).subscribe();
  }

  csv() {
    window.open('/api/csv/' + this.doc.id);
  }


}

// demo petr
export interface PeriodicElement {
  zdroj: string;
  signatura: string;
  status: string;
  dilciKnih: string;
  rocnik_svazek: number;
  cislo: string;
  rok: string;
  buttons: string;
}

const ELEMENT_DATA: PeriodicElement[] = [
  {zdroj: "https://registr.digitalniknihovna.cz/system/libraries/logos/000/000/001/thumb/logo_mzk.png?1477230761", signatura: "4 D 000370/Abt.1", status: "prezenčně", dilciKnih: "", rocnik_svazek: 1, cislo: "", rok: "", buttons: ""},
  {zdroj: "https://registr.digitalniknihovna.cz/system/libraries/logos/000/000/018/thumb/Bez_na%CC%81zvu.png?1554386313", signatura: "4 E 000163/Abt.1", status: "prezenčně", dilciKnih: "", rocnik_svazek: 1, cislo: "", rok: "", buttons: ""},
  {zdroj: "https://registr.digitalniknihovna.cz/system/libraries/logos/000/000/008/thumb/logo_mlp.png?1477230940", signatura: "4 E 000163/Abt.2	", status: "absenčně", dilciKnih: "", rocnik_svazek: 1, cislo: "", rok: "", buttons: ""},
  {zdroj: "https://registr.digitalniknihovna.cz/system/libraries/logos/000/000/001/thumb/logo_mzk.png?1477230761", signatura: "4 D 000370/Abt.1", status: "prezenčně", dilciKnih: "", rocnik_svazek: 1, cislo: "", rok: "", buttons: ""},
  {zdroj: "https://registr.digitalniknihovna.cz/system/libraries/logos/000/000/018/thumb/Bez_na%CC%81zvu.png?1554386313", signatura: "4 E 000163/Abt.1", status: "prezenčně", dilciKnih: "", rocnik_svazek: 1, cislo: "", rok: "", buttons: ""},
  {zdroj: "https://registr.digitalniknihovna.cz/system/libraries/logos/000/000/008/thumb/logo_mlp.png?1477230940", signatura: "4 E 000163/Abt.2	", status: "absenčně", dilciKnih: "", rocnik_svazek: 1, cislo: "", rok: "", buttons: ""},
  {zdroj: "https://registr.digitalniknihovna.cz/system/libraries/logos/000/000/001/thumb/logo_mzk.png?1477230761", signatura: "4 D 000370/Abt.1", status: "prezenčně", dilciKnih: "", rocnik_svazek: 1, cislo: "", rok: "", buttons: ""},
  {zdroj: "https://registr.digitalniknihovna.cz/system/libraries/logos/000/000/018/thumb/Bez_na%CC%81zvu.png?1554386313", signatura: "4 E 000163/Abt.1", status: "prezenčně", dilciKnih: "", rocnik_svazek: 1, cislo: "", rok: "", buttons: ""},
  {zdroj: "https://registr.digitalniknihovna.cz/system/libraries/logos/000/000/008/thumb/logo_mlp.png?1477230940", signatura: "4 E 000163/Abt.2	", status: "absenčně", dilciKnih: "", rocnik_svazek: 1, cislo: "", rok: "", buttons: ""},
  {zdroj: "https://registr.digitalniknihovna.cz/system/libraries/logos/000/000/001/thumb/logo_mzk.png?1477230761", signatura: "4 D 000370/Abt.1", status: "prezenčně", dilciKnih: "", rocnik_svazek: 1, cislo: "", rok: "", buttons: ""},
  {zdroj: "https://registr.digitalniknihovna.cz/system/libraries/logos/000/000/018/thumb/Bez_na%CC%81zvu.png?1554386313", signatura: "4 E 000163/Abt.1", status: "prezenčně", dilciKnih: "", rocnik_svazek: 1, cislo: "", rok: "", buttons: ""},
  {zdroj: "https://registr.digitalniknihovna.cz/system/libraries/logos/000/000/008/thumb/logo_mlp.png?1477230940", signatura: "4 E 000163/Abt.2	", status: "absenčně", dilciKnih: "", rocnik_svazek: 1, cislo: "", rok: "", buttons: ""}
];
// end demo petr

