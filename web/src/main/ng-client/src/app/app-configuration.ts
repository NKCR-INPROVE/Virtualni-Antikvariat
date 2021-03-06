import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Configuration } from './shared/config';

@Injectable({
    providedIn: 'root'
}) export class AppConfiguration {

    private config: Configuration;
    public invalidServer: boolean;

    public get context() {
        return this.config.context;
    }

    public get facets() {
        return this.config.facets;
    }

    public get standardSources() {
        return this.config.standardSources;
    }

    public get roles() {
        return this.config.roles;
    }

    public get doprava() {
        return this.config.doprava;
    }

    public get platba() {
        return this.config.platba;
    }

    /**
     * List the files holding section configuration in assets/configs folder
     * ['search'] will look for /assets/configs/search.json
     */
    private configs: string[] = [];

    constructor(
        private http: HttpClient) { }

    public configLoaded() {
        return this.config && true;
    }

    public load(): Promise<any> {
        console.log('loading app...');
        const promise = this.http.get('assets/config.json')
            .toPromise()
            .then(cfg => {
                this.config = cfg as Configuration;
            }).then(() => {
                return this.loadConfigs();
            });
        return promise;
    }

    async loadConfigs(): Promise<any> {

        // Load common configs
        this.configs.forEach(async config => {
            const url = 'assets/configs/' + config + '.json';
            const value = await this.mergeFile(url) as string;
            if (value) {
                this.config[config] = value;
            } else {
                console.log(url + ' not found');
            }
        });

        return new Promise((resolve, reject) => {
            resolve();
        });
    }

    mergeFile(url: string): Promise<any> {

        return new Promise((resolve, reject) => {
            this.http.get(url)
                .subscribe(
                    res => {
                        resolve(res);
                    },
                    error => {
                        resolve(false);
                        return of(url + ' not found');
                    }
                );
        });
    }

}
