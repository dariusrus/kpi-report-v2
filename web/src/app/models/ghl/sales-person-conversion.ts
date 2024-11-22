export interface SalesPersonConversion {
  salesPersonId: string;
  salesPersonName: string;
  photoUrl: string;
  count: number;
  monetaryValue: number;
  convertedContacts: ConvertedContacts[];
}

export interface ConvertedContacts {
  contactId: string;
  contactName: string;
  contactEmail: string;
  contactPhone: number;
  totalSms: number;
  totalEmails: number;
  totalCalls: number;
  totalLiveChatMessages: number;
}

