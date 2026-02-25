import { Component } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';

@Component({
  selector: 'app-home',
  imports: [NgOptimizedImage],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {
logo = '/assets/images/logos/Vayziru-logo-colorida-1024px.png';
image1 = '/assets/images/home/Gemini_Generated_Imag1.png';
}
