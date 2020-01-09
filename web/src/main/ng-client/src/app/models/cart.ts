import { OfferRecord } from './offer-record';
import { User } from './user';

export class Cart {
  id: string;
  user: User;
  library: string;
  status: string;
  doprava:  { [key: string]: string };
  item: OfferRecord[];
  created: Date;
}
