export class Exemplar {
  id: string;
  zdroj: string;
  knihovna: string;
  isNKF: boolean;
  signatura: string;
  status: string;
  dilciKnih: string;
  rocnik_svazek: string;
  cislo: string;
  rok: string;
  md5: string;
}

export class ExemplarZdroj {
  id: string;
  zdroj: string;
  file: string;
  ex: Exemplar[] = [];
}