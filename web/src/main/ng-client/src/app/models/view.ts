import { Params } from '@angular/router';

export class View {
  id: string = null;
  name: string = '';
  user: string = '';
  global: boolean = false;
  params: Params = {};
}
