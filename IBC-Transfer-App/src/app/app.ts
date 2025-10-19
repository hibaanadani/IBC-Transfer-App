import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <main>
      <router-outlet></router-outlet>
    </main>
  `,

  imports: [RouterOutlet],
})
export class App {}
