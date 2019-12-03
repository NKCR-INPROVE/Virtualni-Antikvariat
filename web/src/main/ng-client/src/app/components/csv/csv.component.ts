import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'app-csv',
  templateUrl: './csv.component.html',
  styleUrls: ['./csv.component.scss']
})
export class CsvComponent implements OnInit {

  constructor(
    public dialogRef: MatDialogRef<CsvComponent>,
    @Inject(MAT_DIALOG_DATA) public data: string) {}


  ngOnInit(): void { }
}
