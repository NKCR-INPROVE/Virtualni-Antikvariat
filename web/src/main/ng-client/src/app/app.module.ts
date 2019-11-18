import { BrowserModule } from '@angular/platform-browser';
import { NgModule, APP_INITIALIZER } from '@angular/core';

import { HttpClientModule, HttpClient, HTTP_INTERCEPTORS } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { registerLocaleData } from '@angular/common';
import localeCs from '@angular/common/locales/cs';
import { FormsModule } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';

import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';

import { AppRoutingModule } from './app-routing.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { AppComponent } from './app.component';
import { AppState } from './app.state';
import { AppService } from './app.service';
import { AuthGuard } from './auth-guard';

import { MaterialModule } from './material.module';

import { ResultsComponent } from './pages/results/results.component';
import { HomeComponent } from './pages/home/home.component';
import { FooterComponent } from './components/footer/footer.component';
import { NavbarComponent } from './components/navbar/navbar.component';
import { AppConfiguration } from './app-configuration';
import { LoginComponent } from './components/login/login.component';
import { BasicAuthInterceptor, ErrorInterceptor } from './shared';
import { RegistrationComponent } from './pages/registration/registration.component';
import { SearchFormComponent, AdvancedSearchDialog } from './components/search-form/search-form.component';
import { FacetsComponent } from './components/facets/facets.component';
import { ResultItemComponent } from './components/result-item/result-item.component';
import { ResultsHeaderComponent } from './components/results-header/results-header.component';


registerLocaleData(localeCs, 'cs');

export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http);
}

export function createTranslateLoader(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}


@NgModule({
  declarations: [
    AppComponent,
    ResultsComponent,
    HomeComponent,
    FooterComponent,
    NavbarComponent,
    LoginComponent,
    RegistrationComponent,
    SearchFormComponent,
    AdvancedSearchDialog,
    FacetsComponent,
    ResultItemComponent,
    ResultsHeaderComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MaterialModule,
    BrowserAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: createTranslateLoader,
        deps: [HttpClient]
      }
    }),
  ],
  entryComponents: [AdvancedSearchDialog, LoginComponent],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: BasicAuthInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
    { provide: APP_INITIALIZER, useFactory: (config: AppConfiguration) => () => config.load(), deps: [AppConfiguration], multi: true },
    HttpClient, DatePipe, AppConfiguration, AppState, AppService, AuthGuard],
  bootstrap: [AppComponent]
})
export class AppModule { }
