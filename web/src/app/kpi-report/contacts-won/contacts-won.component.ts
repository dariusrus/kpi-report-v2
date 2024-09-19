import {Component, Input} from '@angular/core';
import {SharedUtil} from "../../util/shared-util";
import {KpiReport} from "../../models/kpi-report";

@Component({
  selector: 'app-contacts-won',
  templateUrl: './contacts-won.component.html',
  styleUrl: './contacts-won.component.css'
})
export class ContactsWonComponent {
  @Input() reportData!: KpiReport;
  @Input() isVisible!: { [key: string]: string };

  data: any[] = [];

  protected readonly SharedUtil = SharedUtil;
}
