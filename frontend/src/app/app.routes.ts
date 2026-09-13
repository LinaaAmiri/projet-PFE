import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { ForgotPasswordComponent } from './pages/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './pages/reset-password/reset-password.component';
import { AdminComponent } from './pages/admin/admin.component';
import { EmployeComponent } from './pages/employe/employe.component';
import { AgentIaComponent } from './pages/agent-ai/agent-ai.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'admin', component: AdminComponent },
  { path: 'employe', component: EmployeComponent },
  { path: 'agent-ai', component: AgentIaComponent }
];