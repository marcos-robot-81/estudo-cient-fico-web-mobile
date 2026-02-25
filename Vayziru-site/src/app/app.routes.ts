import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Servicos } from './pages/servicos/servicos';
import { pmarca } from './pages/projetos-portifolio/busca/marca/marca';

export const routes: Routes = [
    {
        path: '',
        component: Home
    },
    {
        path: 'servicos',
        component: Servicos
    },
    {
        path: "INPI/busca/marca",
        component: pmarca
    }

    
];
