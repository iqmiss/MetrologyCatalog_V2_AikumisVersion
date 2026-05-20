export interface User {
  id: number;
  email: string;
  role: 'client' | 'metrolog' | 'manager' | 'director' | 'gen_director' | 'financier' | 'approver' | 'admin' | 'yurist';
  fullName: string;
  phone?: string;
  iin?: string;
  companyId?: number;
  labId?: number;
  isActive: boolean;
}

export interface Company {
  id: number;
  bin: string;
  name: string;
  address?: string;
  phone?: string;
  email?: string;
}

export interface Laboratory {
  id: number;
  name: string;
  address?: string;
  phone?: string;
  city?: string;
  email?: string;
}

export interface Service {
  id: number;
  name: string;
  description?: string;
  measurementType: string;
  price?: number | null;
  durationDays: number;
  labId: number;
  isActive: boolean;
  standard?: string;
  labName?: string;
  code?: string;
}

export interface Subservice {
  id: number;
  serviceId: number;
  name: string;
  code: string;
  description?: string;
  fullCode?: string;
  isActive: boolean;
}

export interface SubserviceField {
  id: number;
  subserviceId: number;
  fieldKey: string;
  labelRu: string;
  fieldType: 'text' | 'number' | 'date' | 'select' | 'file';
  required: boolean;
  optionsJson?: string;
  sortOrder: number;
  isRepeating: boolean;
}

export interface ApplicationFieldValue {
  id: number;
  orderId: number;
  fieldKey: string;
  fieldValue: string;
  rowIndex: number;
  filledByRole: string;
}

export interface ChatMessage {
  id: number;
  orderId: number;
  senderId: number;
  senderRole: string;
  messageText?: string;
  attachmentBase64?: string;
  attachmentName?: string;
  sentAt: string;
  read: boolean;
}

export interface DocumentComment {
  id: number;
  orderId: number;
  commenterId: number;
  commenterRole: string;
  highlightedText?: string;
  commentText: string;
  resolved: boolean;
  createdAt: string;
  resolvedAt?: string;
}

export interface OrderDemand {
  id: number;
  orderId: number;
  createdBy: number;
  demandText: string;
  status: 'open' | 'fulfilled';
  createdAt: string;
  fulfilledAt?: string;
}

export type OrderStatus =
  | 'pending_contract'
  | 'revision'
  | 'awaiting_approval'
  | 'awaiting_director'
  | 'awaiting_payment'
  | 'pending_delivery'
  | 'awaiting_delivery'
  | 'received_in_lab'
  | 'in_work'
  | 'under_review'
  | 'completed'
  | 'cancelled'
  | 'annulled'
  | 'terminated';

export interface Order {
  id: number;
  orderNumber: string;
  clientId: number;
  serviceId: number;
  labId: number;
  assignedLabId?: number;
  assignedAt?: string;
  status: OrderStatus;
  price?: number | null;
  dueDate?: string;
  metrologistId?: number;
  clientComment?: string;
  managerComment?: string;
  invoiceSent?: boolean;
  paymentReceiptName?: string;
  receiptUploadedAt?: string;
  subserviceId?: number;
  secondaryStatus?: string;
  applicationCode?: string;
  serviceAddress?: string;
  responsibleDepartment?: string;
  signerUserId?: number;
  clientEditEnabled?: boolean;
  formLocked?: boolean;
}

export interface OrderItem {
  id: number;
  orderId: number;
  deviceType: string;
  model: string;
  serialNumber: string;
  quantity: number;
}

export type ContractStatus =
  | 'draft'
  | 'pending_approval'
  | 'approved'
  | 'signed'
  | 'rejected'
  | 'annulled'
  | 'terminated';

export interface Contract {
  id: number;
  orderId: number;
  contractNumber: string;
  registrationNumber?: string;
  contractFileName?: string;
  filePath?: string;
  status: ContractStatus;
  directorSigned: boolean;
  directorSignedAt?: string;
  approverSigned: boolean;
  approverSignedAt?: string;
  financierSigned: boolean;
  financierSignedAt?: string;
  clientSigned: boolean;
  clientSignedAt?: string;
  genDirectorSigned: boolean;
  genDirectorSignedAt?: string;
  rejectedByRole?: string;
  rejectedReason?: string;
  annulledAt?: string;
  annulledReason?: string;
  terminatedAt?: string;
  terminatedReason?: string;
  metrologConfirmed?: boolean;
  financierConfirmed?: boolean;
  yuristConfirmed?: boolean;
  confirmationsRequested?: boolean;
}

export interface Result {
  id: number;
  orderId: number;
  resultType: 'certificate' | 'protocol' | 'report';
  issuedAt?: string;
  filePath?: string;
  metrologistId: number;
  isSigned: boolean;
  signedAt?: string;
}

export interface Device {
  id: number;
  companyId: number;
  type: string;
  model?: string;
  serialNumber: string;
  lastVerifiedAt?: string;
  nextVerificationDate?: string;
}

export type NotificationType =
  | 'order_status'
  | 'document_ready'
  | 'reminder'
  | 'approval_required'
  | 'payment_received'
  | 'assigned_to_lab'
  | 'receipt_uploaded'
  | 'demand_created'
  | 'demand_fulfilled'
  | 'doc_comment_added'
  | 'confirmation_requested';

export interface Notification {
  id: number;
  userId: number;
  orderId?: number;
  message: string;
  notificationType: NotificationType;
  isRead: boolean;
  readAt?: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  phone?: string;
  bin?: string;
  companyName?: string;
  companyAddress?: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}