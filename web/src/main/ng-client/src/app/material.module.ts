import { NgModule } from '@angular/core';

import {
  MatFormFieldModule,
  MatInputModule,
  MatButtonModule,
  MatMenuModule,
  MatToolbarModule,
  MatIconModule,
  MatDialogModule,
  MatCardModule,
  MatSelectModule,
  MatTooltip,
  MatTooltipModule,
  MatCheckboxModule,
  MatListModule,
  MatPaginator,
  MatPaginatorModule,
  MatTableModule,
  MatSnackBarModule,
  MatRadioModule,
  MatProgressBarModule
} from '@angular/material';

@NgModule({
  imports: [
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatMenuModule,
    MatToolbarModule,
    MatIconModule,
    MatDialogModule,
    MatCardModule,
    MatSelectModule,
    MatTooltipModule,
    MatCheckboxModule,
    MatListModule,
    MatPaginatorModule,
    MatTableModule,
    MatSnackBarModule,
    MatRadioModule,
    MatProgressBarModule
  ],
  exports: [
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatMenuModule,
    MatToolbarModule,
    MatIconModule,
    MatDialogModule,
    MatCardModule,
    MatSelectModule,
    MatTooltipModule,
    MatCheckboxModule,
    MatListModule,
    MatPaginatorModule,
    MatTableModule,
    MatSnackBarModule,
    MatRadioModule,
    MatProgressBarModule
  ]
})
export class MaterialModule {}