import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ResultsComponent } from './pages/results/results.component';
import { HomeComponent } from './pages/home/home.component';
import { RegistrationComponent } from './pages/registration/registration.component';
import { AdminComponent } from './pages/admin/admin.component';
import { AuthGuard } from './auth-guard';
import { DemandsComponent } from './pages/demands/demands.component';
import { OffersComponent } from './pages/offers/offers.component';
import { VaRegistrationComponent } from './pages/va-registration/va-registration.component';


const routes: Routes = [
  { path: 'registrace', component: VaRegistrationComponent },
  { path: 'results', component: ResultsComponent },
  { path: 'home', component: HomeComponent },
  { path: 'demands', component: DemandsComponent },
  { path: 'offers', component: OffersComponent },
  // { path: 'Poptávky', component: DemandsComponent },
  // { path: 'Nabídky', component: OffersComponent },
  { path: 'admin', component: AdminComponent, canActivate: [AuthGuard]},
  { path: '', component: HomeComponent },
  { path: 'login', component: HomeComponent },

  // otherwise redirect to home
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
