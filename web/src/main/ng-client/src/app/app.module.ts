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
import { FlexLayoutModule } from '@angular/flex-layout';

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
import { AdminComponent } from './pages/admin/admin.component';
import { fakeBackendProvider, FakeBackendInterceptor } from './shared/fake-backend';
import { environment } from 'src/environments/environment';
import { ViewComponent } from './components/view/view.component';
import { OffersComponent } from './pages/offers/offers.component';
import { DemandsComponent } from './pages/demands/demands.component';
import { CsvComponent } from './components/csv/csv.component';
import { AddToOfferDialogComponent } from './components/add-to-offer-dialog/add-to-offer-dialog.component';
import { NewOfferDialogComponent } from './components/new-offer-dialog/new-offer-dialog.component';
import { PromptDialogComponent } from './components/prompt-dialog/prompt-dialog.component';
import { TemplateToOfferDialogComponent } from './components/template-to-offer-dialog/template-to-offer-dialog.component';
import { UploadToOfferDialogComponent } from './components/upload-to-offer-dialog/upload-to-offer-dialog.component';
import { SearchToOfferDialogComponent } from './components/search-to-offer-dialog/search-to-offer-dialog.component';
import { VaRegistrationComponent } from './pages/va-registration/va-registration.component';
import { ConfirmDialogComponent } from './components/confirm-dialog/confirm-dialog.component';
import { ReportComponent } from './pages/report/report.component';
import { PasswordDialogComponent } from './components/password-dialog/password-dialog.component';
import { ProfileComponent } from './pages/profile/profile.component';
import { UserComponent } from './components/user/user.component';
import { ShoppingCartComponent } from './pages/shopping-cart/shopping-cart.component';


registerLocaleData(localeCs, 'cs');

export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http);
}

export function createTranslateLoader(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}

const providers: any[] = [
  { provide: HTTP_INTERCEPTORS, useClass: BasicAuthInterceptor, multi: true },
  { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
  { provide: APP_INITIALIZER, useFactory: (config: AppConfiguration) => () => config.load(), deps: [AppConfiguration], multi: true },
  HttpClient, DatePipe, AppConfiguration, AppState, AppService, AuthGuard];

if (environment.mocked) {
  console.log('Enabling mocked services.');
  providers.push(fakeBackendProvider);
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
    ResultsHeaderComponent,
    AdminComponent,
    ViewComponent,
    OffersComponent,
    DemandsComponent,
    CsvComponent,
    AddToOfferDialogComponent,
    NewOfferDialogComponent,
    PromptDialogComponent,
    TemplateToOfferDialogComponent,
    UploadToOfferDialogComponent,
    SearchToOfferDialogComponent,
    VaRegistrationComponent,
    ConfirmDialogComponent,
    ReportComponent,
    PasswordDialogComponent,
    ProfileComponent,
    UserComponent,
    ShoppingCartComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MaterialModule,
    FlexLayoutModule,
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
  entryComponents: [AdvancedSearchDialog, LoginComponent, ViewComponent, CsvComponent,
    AddToOfferDialogComponent, PromptDialogComponent, ConfirmDialogComponent,
    TemplateToOfferDialogComponent,
    UploadToOfferDialogComponent,
    SearchToOfferDialogComponent,
    PasswordDialogComponent],
  providers,
  bootstrap: [AppComponent]
})
export class AppModule { }
