import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Electricity } from './pages/electricity/electricity';
import { Gas } from './pages/gas/gas';
import { HeatingElectricity } from './pages/heating-electricity/heating-electricity';
import { NightHeaters } from './pages/night-heaters/night-heaters';
import { CarElectricity } from './pages/car-electricity/car-electricity';
import { ElectricityComparision } from './navigation-pages/electricity-comparision/electricity-comparision';
import { GasComparision } from './navigation-pages/gas-comparision/gas-comparision';
import { CommercialElectricity } from './navigation-pages/commercial-electricity/commercial-electricity';

import { DeliveryAddress } from './nav-electricity-comparision/delivery-address/delivery-address';
import { SelectProvider } from './nav-electricity-comparision/select-provider/select-provider';
import { PaymentMethod } from './nav-electricity-comparision/payment-method/payment-method';
import { ConnectionData } from './nav-electricity-comparision/connection-data/connection-data';
import { CheckoutPage } from './nav-electricity-comparision/checkout-page/checkout-page';
import { Account } from './nav-electricity-comparision/account/account';
import { LoginPage } from './nav-electricity-comparision/login-page/login-page';
import { Sidebar } from './layout/sidebar/sidebar';
import { Register } from './nav-electricity-comparision/register/register';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },

  { path: 'home', component: Home, children: [
      { path: '', redirectTo: 'electricity', pathMatch: 'full' },
      { path: 'electricity', component: Electricity },
      { path: 'gas', component: Gas },
      { path: 'heating-electricity', component: HeatingElectricity },
      { path: 'night-heaters', component: NightHeaters },
      { path: 'car-electricity', component: CarElectricity },
    ]
  },
  { path: 'electricity-comparision', component: ElectricityComparision,
    children: [
      {path: '', component: SelectProvider},
      {path: 'login', component: LoginPage},
      {path: 'delivery-address', component: DeliveryAddress},
      {path: 'connection-data', component: ConnectionData},
      {path: 'payment-method', component: PaymentMethod},
      {path: 'account', component: Account},
      {path: 'checkout', component: CheckoutPage},
      {path: 'register', component: Register}
    ]
   },
  { path: 'gas-comparision', component: GasComparision },
  { path: 'commercial-electricity', component: CommercialElectricity },


];
