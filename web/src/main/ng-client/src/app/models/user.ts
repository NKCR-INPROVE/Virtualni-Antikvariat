export class User {
    code: string;
    heslo: string;
    username: string;
    nazev: string;
    role: string;
    priorita: number;
    telefon: string;
    email: string;
    sigla: string;
    adresa: string;
    active: boolean;
    doprava: string[];
    cenik_osobni: string;
    cenik_nadobirku: string;
    cenik_predem: string;
    platba: string[];
    celostatni: boolean;
    regionalni: boolean;
    periodicky: boolean;
    prijemce: string;
    poznamka: string;
    osoba: string;
    authdata?: string;
}
