import {Component, Input} from '@angular/core';
import {SharedUtil} from "../../util/shared-util";
import {KpiReport} from "../../models/kpi-report";
import {dropInAnimation} from "../../util/animations";
import {animate, query, stagger, style, transition, trigger} from "@angular/animations";

@Component({
  selector: 'app-contacts-won',
  templateUrl: './contacts-won.component.html',
  styleUrl: './contacts-won.component.css',
  animations: [
    trigger('fadeIn', [
      transition('* => *', [
        query(':enter', [
          style({opacity: 0}),
          stagger(100, [
            animate('0.5s', style({opacity: 1}))
          ])
        ], {optional: true})
      ])
    ]),
  ]
})
export class ContactsWonComponent {
  @Input() reportData!: KpiReport;
  @Input() isVisible!: { [key: string]: string };

  data: any[] = [];

  protected readonly SharedUtil = SharedUtil;
}
