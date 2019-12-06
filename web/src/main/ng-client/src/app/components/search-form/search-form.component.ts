import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialog } from '@angular/material';
import { SearchParams, AdvancedParams } from 'src/app/models/search-params';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { Utils } from 'src/app/shared/utils';

@Component({
  selector: 'app-search-form',
  templateUrl: './search-form.component.html',
  styleUrls: ['./search-form.component.scss']
})
export class SearchFormComponent implements OnInit {

  searchParams: SearchParams = new SearchParams();
  advParams: AdvancedParams = new AdvancedParams();
  hasAdvanced: boolean;

  constructor(
    public dialog: MatDialog,
    private route: ActivatedRoute,
    private router: Router) { }

  ngOnInit() {
    this.router.events.subscribe(val => {
      if (val instanceof NavigationEnd) {
        this.setParams();
      }
    });
    this.setParams();
  }

  setParams() {
    Utils.sanitize(this.route.snapshot.queryParams, this.searchParams);
    Utils.sanitize(this.route.snapshot.queryParams, this.advParams);
    console.log(Object.values(this.advParams));
    this.hasAdvanced = ! (Object.values(this.advParams).every(k => k === null));
    console.log(this.hasAdvanced);
  }

  search() {
    this.searchParams.offset = 0;
    const params = { ...this.searchParams, ...this.advParams };
    this.router.navigate(['/results'], { queryParams: params });
  }

  clearQuery() {
    this.searchParams.q = null;
    this.advParams = new AdvancedParams();
    this.search();
  }

  export() {

  }

  offers() { }

  demands() { }

  openAdvanced(): void {
    const data = Object.assign({}, this.advParams);
    const dialogRef = this.dialog.open(AdvancedSearchDialog, {
      width: '350px',
      data
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.advParams = new AdvancedParams();
        Utils.sanitize(result, this.advParams);
        this.search();
      }
    });
  }


}



@Component({
  selector: 'app-advanced-search-dialog',
  templateUrl: 'advanced-search-dialog.html',
})
export class AdvancedSearchDialog {

  constructor(
    public dialogRef: MatDialogRef<AdvancedSearchDialog>,
    @Inject(MAT_DIALOG_DATA) public data: AdvancedParams) { }

    onNoClick(): void {
      this.dialogRef.close();
    }

    reset(): void {
      this.data = new AdvancedParams();
    }


}
