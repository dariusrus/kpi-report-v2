import { animate, state, style, transition, trigger } from '@angular/animations';

export const dropInAnimation = trigger('dropIn', [
  state('void', style({
    opacity: 0,
    transform: 'translateY(-20px) translateX(-50%)'
  })),
  state('*', style({
    opacity: 1,
    transform: 'translateY(0) translateX(-50%)'
  })),
  transition(':enter', [
    style({
      opacity: 0,
      transform: 'translateY(-20px) translateX(-50%)'
    }),
    animate('300ms ease-in')
  ]),
  transition(':leave', [
    animate('200ms ease-out', style({
      opacity: 0,
      transform: 'translateY(0) translateX(-50%)'
    }))
  ])
]);
