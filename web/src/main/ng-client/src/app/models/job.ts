export class Job {
  jobKey: string;
  name: string;
  state: string;
  nextFireTime: Date;
  config: any;
  status: {
    last_run: Date,
    last_message: string
  };
}
