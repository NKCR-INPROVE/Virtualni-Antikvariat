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

  constructor(
    public dialog: MatDialog,
    private route: ActivatedRoute,
    private router: Router) { }

  ngOnInit() {
    // this.searchParams = Object.assign(SearchParams, this.route.snapshot.queryParams as SearchParams);
    // this.advParams = this.route.snapshot.queryParams as AdvancedParams;
    
    this.router.events.subscribe(val => {
      if (val instanceof NavigationEnd) {
        Utils.sanitize(this.route.snapshot.queryParams, this.searchParams);
        Utils.sanitize(this.route.snapshot.queryParams, this.advParams);
        console.log(this.searchParams);
      }
    });
  }

  search() {
    const params = {...this.searchParams, ...this.advParams};
    this.router.navigate(['/results'], {queryParams: params});
  }

  openAdvanced(): void {
    const data = Object.assign({}, this.advParams);
    const dialogRef = this.dialog.open(AdvancedSearchDialog, {
      width: '350px',
      data
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
      if (result) {
        Utils.sanitize(result, this.advParams);
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
    @Inject(MAT_DIALOG_DATA) public data: AdvancedParams) {}

  onNoClick(): void {
    this.dialogRef.close();
  }

}
