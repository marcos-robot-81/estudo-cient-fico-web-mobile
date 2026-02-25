import { Component } from '@angular/core';
import { RouterLink} from '@angular/router';
import { NgOptimizedImage } from '@angular/common';



@Component({
  selector: 'app-menu',
  imports: [RouterLink, NgOptimizedImage],
  templateUrl: './menu.html',
  styleUrl: './menu.scss',
})
export class Menu {
  logoPath = '/assets/images/logos/Vayziru-logo-colorida.png';
}
