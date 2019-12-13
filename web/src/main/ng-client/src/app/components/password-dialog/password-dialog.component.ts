import { Component, OnInit, Inject } from '@angular/core';
import { AppService } from 'src/app/app.service';
import { AppState } from 'src/app/app.state';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Md5 } from 'ts-md5';

@Component({
  selector: 'app-password-dialog',
  templateUrl: './password-dialog.component.html',
  styleUrls: ['./password-dialog.component.scss']
})
export class PasswordDialogComponent implements OnInit {

  hesloForm: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<PasswordDialogComponent>,
    private formBuilder: FormBuilder,
    public state: AppState,
    private service: AppService) { }

  ngOnInit() {
    this.hesloForm = this.formBuilder.group({
      oldheslo: ['', Validators.required],
      newheslo: ['', Validators.required]
    });
  }

  get f() { return this.hesloForm.controls; }

  onSubmit() {
    const data = {
      code: this.state.user.code,
      oldheslo: '' + Md5.hashStr(this.f.oldheslo.value),
      newheslo: '' + Md5.hashStr(this.f.newheslo.value)
    };

    this.service.resetHeslo(data).subscribe(resp => {
      if (resp.error) {
        this.service.showSnackBar('heslo.reset_heslo_error', resp.error, 'app-snack-error');
      } else {
        this.service.showSnackBar('heslo.reset_heslo_success', '', 'app-snack-success');
        this.dialogRef.close();
      }
    });
  }

}
