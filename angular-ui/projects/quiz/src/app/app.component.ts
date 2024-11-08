import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  standalone: true,
  selector: 'app-root',
  imports: [CommonModule, RouterOutlet],
  template: `<router-outlet></router-outlet>`,
  styles: [],
})
export class AppComponent {
  constructor() {}
}
