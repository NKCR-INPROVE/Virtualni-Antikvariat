import { Component, OnInit, Inject, Input, Output, EventEmitter } from '@angular/core';
import { trigger, state, style, animate, transition } from '@angular/animations';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { Subscription, of } from 'rxjs';

import {
  HttpClient, HttpResponse, HttpRequest,
  HttpEventType, HttpErrorResponse
} from '@angular/common/http';

import { catchError, last, map, tap } from 'rxjs/operators';


@Component({
  selector: 'app-upload-to-offer-dialog',
  templateUrl: './upload-to-offer-dialog.component.html',
  styleUrls: ['./upload-to-offer-dialog.component.scss'],
  animations: [
        trigger('fadeInOut', [
              state('in', style({ opacity: 100 })),
              transition('* => void', [
                    animate(300, style({ opacity: 0 }))
              ])
        ])
  ]
})
export class UploadToOfferDialogComponent implements OnInit {

  @Input() text = 'Upload';
  /** Name used in form which will be sent in HTTP request. */
  @Input() param = 'file';
  /** Target URL for file uploading. */
  @Input() target = '/api/offers/addfile';
  /** File extension that accepted, same as 'accept' of <input type="file" />. */
  @Input() accept = 'plain/*';

  /** Allow you to add handler after its completion. Bubble up response text from remote. */
  @Output() complete = new EventEmitter<string>();

  format: string;

  private files: Array<FileUploadModel> = [];


  constructor(
    public dialogRef: MatDialogRef<UploadToOfferDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private http: HttpClient) { }

  ngOnInit() {
  }

  onClick() {
    const fileUpload = document.getElementById('fileUpload') as HTMLInputElement;
    fileUpload.onchange = () => {
      for (let index = 0; index < fileUpload.files.length; index++) {
        const file = fileUpload.files[index];
        this.files.push({
          data: file, state: 'in',
          inProgress: false, progress: 0, canRetry: false, canCancel: true
        });
      }
      // this.uploadFiles();
    };
    fileUpload.click();
  }
  cancelFile(file: FileUploadModel) {
    file.sub.unsubscribe();
    this.removeFileFromArray(file);
  }

  retryFile(file: FileUploadModel) {
    this.uploadFile(file);
    file.canRetry = false;
  }

  private uploadFile(file: FileUploadModel) {
    const fd = new FormData();
    fd.append(this.param, file.data);

    const req = new HttpRequest('POST', this.target, fd, {
      reportProgress: true
    });

    file.inProgress = true;
    file.sub = this.http.request(req).pipe(
      map(event => {
        switch (event.type) {
          case HttpEventType.UploadProgress:
            file.progress = Math.round(event.loaded * 100 / event.total);
            break;
          case HttpEventType.Response:
            return event;
        }
      }),
      tap(message => { }),
      last(),
      catchError((error: HttpErrorResponse) => {
        file.inProgress = false;
        file.canRetry = true;
        return of(`${file.data.name} upload failed.`);
      })
    ).subscribe(
      (event: any) => {
        if (typeof (event) === 'object') {
          this.removeFileFromArray(file);
          this.complete.emit(event.body);
        }
      }
    );
  }

  private uploadFiles() {
    const fileUpload = document.getElementById('fileUpload') as HTMLInputElement;
    fileUpload.value = '';

    this.files.forEach(file => {
      this.uploadFile(file);
    });
  }

  private removeFileFromArray(file: FileUploadModel) {
    const index = this.files.indexOf(file);
    if (index > -1) {
      this.files.splice(index, 1);
    }
  }

}


export class FileUploadModel {
  data: File;
  state: string;
  inProgress: boolean;
  progress: number;
  canRetry: boolean;
  canCancel: boolean;
  sub?: Subscription;
}