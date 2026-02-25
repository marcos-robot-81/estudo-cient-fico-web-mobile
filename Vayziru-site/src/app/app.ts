import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Menu } from './componentes/menu/menu';
import { cfooter } from './componentes/footer/footer';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Menu, cfooter],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('Vayziru');
}
