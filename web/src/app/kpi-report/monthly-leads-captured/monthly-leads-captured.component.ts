import {Component, Input} from '@angular/core';
import {KpiReport} from "../../models/kpi-report";
import {SharedUtil} from "../../util/shared-util";
import {animate, query, stagger, style, transition, trigger} from "@angular/animations";

@Component({
  selector: 'app-monthly-leads-captured',
  templateUrl: './monthly-leads-captured.component.html',
  styleUrl: './monthly-leads-captured.component.css',
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
export class MonthlyLeadsCapturedComponent {
  @Input() reportData!: KpiReport;
  @Input() isVisible!: { [key: string]: string };

  getStatusSeverity(status: string) {
    switch (status.toLowerCase()) {
      case 'open':
        return 'info';
      case 'won':
        return 'success';
      case 'lost':
        return 'danger';
      case 'abandoned':
        return 'warning';
      default:
        return 'contrast';
    }
  }

  protected readonly SharedUtil = SharedUtil;
}
