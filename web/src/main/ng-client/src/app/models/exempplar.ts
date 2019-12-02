export class Exemplar {
  id: string;
  zdroj: string;
  signatura: string;
  status: string;
  dilciKnih: string;
  rocnik_svazek: string;
  cislo: string;
  rok: string;
}

export class ExemplarZdroj {
  id: string;
  zdroj: string;
  file: string;
  ex: Exemplar[] = [];
}