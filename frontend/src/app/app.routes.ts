import { Routes } from '@angular/router';

import { TruckListComponent } from './truck-list/truck-list';
import { TruckFormComponent } from './truck-form/truck-form';

export const routes: Routes = [
  { path: '', redirectTo: 'trucks', pathMatch: 'full' },
  { path: 'trucks', component: TruckListComponent},
  { path: 'trucks/new', component: TruckFormComponent},
  { path: 'trucks/edit/:id', component: TruckFormComponent}
];