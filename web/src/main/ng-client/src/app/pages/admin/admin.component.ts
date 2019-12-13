import { Component, OnInit } from '@angular/core';
import { AppService } from 'src/app/app.service';
import { User } from 'src/app/models/user';
import { AppConfiguration } from 'src/app/app-configuration';
import { MatDialog } from '@angular/material';
import { PromptDialogComponent } from 'src/app/components/prompt-dialog/prompt-dialog.component';
import { Utils } from 'src/app/shared/utils';
import { Job } from 'src/app/models/job';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss']
})
export class AdminComponent implements OnInit {

  users: User[];
  user: User;
  jobs: Job[];

  displayedColumns = ['jobName', 'nextFireTime', 'lastRun', 'buttons'];

  constructor(
    public dialog: MatDialog,
    public config: AppConfiguration,
    private service: AppService
  ) { }

  ngOnInit() {
    this.getUsers();
    this.getJobs();
  }

  getUsers() {
    this.service.getUsers().subscribe(resp => {
      this.users = resp;
      this.user = this.users[0];
    });
  }

  selectUser(user: User) {
    this.user = user;
  }

  saveUser() {
    this.service.saveUser(this.user).subscribe(resp => {
      if (resp.error) {
        this.service.showSnackBar('admin.add_user_error', '', 'app-snack-error');
      } else {
        this.service.showSnackBar('admin.add_user_success', '', 'app-snack-success');
      }
    });
  }

  addUser() {
    const dialogRef = this.dialog.open(PromptDialogComponent, {
      width: '350px',
      data: { title: 'admin.add_user_name' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.user = new User();
        this.user.username = result;
        // this.user.zkratka = Utils.generateAvatar(result);
        this.user.role = 'USER';
      }
    });
    
  }

  getJobs() {
    this.service.getJobs().subscribe(resp => {
      this.jobs = [];
      Object.keys(resp).forEach(k => {
        this.jobs.push(resp[k]);
      });
    });
  }

  startJob(job: Job) {
    this.service.startJob(job).subscribe(resp => {
      if (resp.error) {
        this.service.showSnackBar('admin.start_job_error', '', 'app-snack-error');
      } else {
        this.service.showSnackBar('admin.start_job_success', '', 'app-snack-success');
      }
    });
  }

  stopJob(job: Job) {
    this.service.stopJob(job).subscribe(resp => {
      if (resp.error) {
        this.service.showSnackBar('admin.stop_job_error', '', 'app-snack-error');
      } else {
        this.service.showSnackBar('admin.stop_job_success', '', 'app-snack-success');
      }
    });

  }

}
