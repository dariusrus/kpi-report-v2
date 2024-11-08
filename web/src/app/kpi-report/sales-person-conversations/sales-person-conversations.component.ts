import { Component, Input, OnInit } from '@angular/core';
import { animate, query, stagger, style, transition, trigger } from "@angular/animations";
import { KpiReport } from "../../models/kpi-report";
import {ConversationEvent, SalesPersonConversation} from "../../models/ghl/sales-person-conversation";
import { SharedUtil } from "../../util/shared-util";
import { MonthlyAverage } from "../../models/monthly-average";

interface SalesPerson {
  id: string;
  name: string;
}



@Component({
  selector: 'app-sales-person-conversations',
  templateUrl: './sales-person-conversations.component.html',
  styleUrl: './sales-person-conversations.component.css',
  animations: [
    trigger('fadeIn', [
      transition('* => *', [
        query(':enter', [
          style({ opacity: 0 }),
          stagger(100, [
            animate('0.5s', style({ opacity: 1 }))
          ])
        ], { optional: true })
      ])
    ]),
  ]
})
export class SalesPersonConversationsComponent implements OnInit {
  @Input() reportData!: KpiReport;
  @Input() reportDataPreviousMap: [KpiReport, MonthlyAverage][] = [];
  @Input() isVisible!: { [key: string]: string };

  data: any[] = [];
  scheme = 'picnic';

  salesPersons: SalesPerson[] = [];
  selectedSalesPersons: SalesPerson[] = [];
  filteredConversations: SalesPersonConversation[] = [];

  // Total counts for number cards
  totalSmsCount: number = 0;
  totalEmailCount: number = 0;
  totalCallCount: number = 0;
  totalLiveChatCount: number = 0;

  events: ConversationEvent[] = [];

  ngOnInit() {
    this.initializeSalesPersons();
    if (this.salesPersons.length > 0) {
      this.selectedSalesPersons = [this.salesPersons[0]];
    }
    this.filterConversations();
    this.updateTotalCounts();
  }

  initializeSalesPersons() {
    const uniqueSalesPersons = new Map<string, SalesPerson>();
    this.reportData.salesPersonConversations.forEach(conversation => {
      if (!uniqueSalesPersons.has(conversation.salesPersonId)) {
        uniqueSalesPersons.set(conversation.salesPersonId, {
          id: conversation.salesPersonId,
          name: conversation.salesPersonName
        });
      }
    });
    this.salesPersons = Array.from(uniqueSalesPersons.values());
  }

  filterConversations() {
    if (this.selectedSalesPersons.length > 0) {
      const selectedIds = this.selectedSalesPersons.map(sp => sp.id);
      this.filteredConversations = this.reportData.salesPersonConversations.filter(
        conversation => selectedIds.includes(conversation.salesPersonId)
      );
    } else {
      this.filteredConversations = this.reportData.salesPersonConversations;
    }
    this.updateTotalCounts();
  }

  updateTotalCounts() {
    this.totalSmsCount = 0;
    this.totalEmailCount = 0;
    this.totalCallCount = 0;
    this.totalLiveChatCount = 0;

    this.filteredConversations.forEach(conversation => {
      const messageCounts = this.getMessageCounts(conversation);
      this.totalSmsCount += messageCounts.smsCount;
      this.totalEmailCount += messageCounts.emailCount;
      this.totalCallCount += messageCounts.callCount;
      this.totalLiveChatCount += messageCounts.liveChatCount;
    });
  }

  getMessageCounts(conversation: SalesPersonConversation) {
    let smsCount = 0;
    let emailCount = 0;
    let callCount = 0;
    let liveChatCount = 0;

    conversation.conversationMessages.forEach(message => {
      switch (message.messageType) {
        case 'TYPE_SMS':
          smsCount++;
          break;
        case 'TYPE_EMAIL':
          emailCount++;
          break;
        case 'TYPE_CALL':
          callCount++;
          break;
        case 'TYPE_LIVE_CHAT':
          liveChatCount++;
          break;
      }
    });

    return { smsCount, emailCount, callCount, liveChatCount };
  }

  populateEvents(conversation: SalesPersonConversation) {
    conversation.events = conversation.conversationMessages.map(message => ({
      status: this.toTitleCase(message.status || ''),
      dateAdded: message.dateAdded,
      direction: message.direction,
      messageType: this.getFriendlyMessageType(message.messageType),
      messageBody: message.messageBody || '',
      callDuration: message.callDuration,
    }));
  }

  getFriendlyMessageType(messageType: string): string {
    switch (messageType) {
      case 'TYPE_SMS':
        return 'SMS';
      case 'TYPE_EMAIL':
        return 'E-mail';
      case 'TYPE_CALL':
        return 'Call';
      case 'TYPE_LIVE_CHAT':
        return 'Live Chat';
      default:
        return 'Unknown';
    }
  }

  getMessageIcon(messageType: string): string {
    switch (messageType) {
      case 'SMS':
        return 'pi pi-mobile';
      case 'E-mail':
        return 'pi pi-envelope';
      case 'Call':
        return 'pi pi-phone';
      case 'Live Chat':
        return 'pi pi-comments';
      default:
        return 'pi pi-info-circle';
    }
  }

  getMessageColor(messageType: string): string {
    switch (messageType) {
      case 'SMS':
        return '#49D025';
      case 'E-mail':
        return '#259ED0';
      case 'Call':
        return '#AD25D0';
      case 'Live Chat':
        return '#CDCC32';
      default:
        return 'pi pi-info-circle';
    }
  }

  titleCase(input: string): string {
    if (!input) return '';

    return input
      .split(' ')
      .map(word => {
        // Ignore words that start with a digit
        return /^[0-9]/.test(word) ? word : word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
      })
      .join(' ');
  }

  getDirectionIcon(direction: string): string {
    return direction === 'inbound' ? 'pi-arrow-circle-down' : 'pi-arrow-circle-up';
  }

  getFormattedMessageType(messageType: string): string {
    return messageType
      .replace(/^type_/i, '') // Remove "type_" prefix if present
      .replace(/_/g, ' ') // Replace underscores with spaces
      .replace(/\b\w/g, char => char.toUpperCase()); // Capitalize the first letter of each word
  }

  get sortedConversations() {
    return this.filteredConversations.sort((a, b) => a.salesPersonName.localeCompare(b.salesPersonName));
  }

  toTitleCase(text: string): string {
    if (!text) return text;
    return text.replace(/\w\S*/g, (word) =>
      word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()
    );
  }

  formatDuration(seconds: number): string {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    const hoursDisplay = hours > 0 ? `${hours}h ` : '';
    const minutesDisplay = minutes > 0 ? `${minutes}m ` : '';
    const secondsDisplay = secs > 0 ? `${secs}s` : '';

    return `${hoursDisplay}${minutesDisplay}${secondsDisplay}`.trim();
  }

  protected readonly SharedUtil = SharedUtil;
}
