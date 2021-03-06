import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { first } from 'rxjs/operators';
import { AuthenticationService } from 'src/app/shared/authentication.service';
import { MatDialogRef } from '@angular/material';
import { Md5 } from 'ts-md5';



@Component({ templateUrl: 'login.component.html' })
export class LoginComponent implements OnInit {
    loginForm: FormGroup;
    loading = false;
    submitted = false;
    returnUrl: string;
    error = '';

    constructor(
        public dialogRef: MatDialogRef<LoginComponent>,
        private formBuilder: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private authenticationService: AuthenticationService
    ) {
        // redirect to home if already logged in
        if (this.authenticationService.currentUserValue) {
            this.router.navigate(['/']);
        }
    }

    ngOnInit() {
        this.loginForm = this.formBuilder.group({
            username: ['', Validators.required],
            password: ['', Validators.required]
        });

        this.returnUrl = this.route.snapshot.queryParams['returnUrl'];
    }

    // convenience getter for easy access to form fields
    get f() { return this.loginForm.controls; }

    onSubmit() {

        this.submitted = true;

        // stop here if form is invalid
        if (this.loginForm.invalid) {
            return;
        }

        this.loading = true;
        const pwd = '' + Md5.hashStr(this.f.password.value);
        this.authenticationService.login(this.f.username.value, pwd)
            .pipe(first())
            .subscribe(
                data => {
                    if (data.error) {
                        this.error = data.error;
                    } else {
                        this.dialogRef.close(data);
                        if (this.returnUrl) {
                            this.router.navigate([this.returnUrl]);
                        }
                    }
                    this.loading = false;
                },
                error => {
                    console.log(error);
                    this.error = error;
                    this.loading = false;
                });
    }
}
