export interface SalesPersonConversation {
  salesPersonId: string;
  salesPersonName: string;
  ownerPhotoUrl: string;
  contactId: string;
  contactName: string;
  contactEmail: string;
  contactPhone: string;
  conversationMessages: ConversationMessage[];
  events: ConversationEvent[];
  lastManualMessageDate: string;
  lastMessageType: string;
}

export interface ConversationMessage {
  messageType: string;
  direction: string;
  status: string;
  dateAdded: string;
  messageBody: string;
  callDuration: number;
}

export interface ConversationEvent {
  status?: string;
  dateAdded?: string;
  direction?: string;
  messageType?: string;
  messageBody?: string;
  callDuration?: number;
}
