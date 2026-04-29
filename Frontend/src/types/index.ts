export interface User {
  id: number;
  email: string;
  role: 'client' | 'metrolog' | 'manager' | 'director' | 'financier' | 'approver' | 'admin';
  fullName: string;
  phone?: string;
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
  price: number;
  durationDays: number;
  labId: number;
  isActive: boolean;
  standard?: string;
  labName?: string;
}

export type OrderStatus =
  | 'pending_contract'
  | 'awaiting_approval'
  | 'awaiting_director'
  | 'awaiting_payment'
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
  status: OrderStatus;
  totalPrice: number;
  dueDate?: string;
  metrologistId?: number;
}

export interface OrderItem {
  id: number;
  orderId: number;
  deviceType: string;
  model: string;
  serialNumber: string;
  quantity: number;
  unitPrice: number;
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
  filePath?: string;
  status: ContractStatus;

  clientSigned: boolean;
  clientSignedAt?: string;
  clientSignedBy?: number;

  directorSigned: boolean;
  directorSignedAt?: string;
  directorSignedBy?: number;

  annulledAt?: string;
  annulledBy?: number;
  annulledReason?: string;

  terminatedAt?: string;
  terminatedBy?: number;
  terminatedReason?: string;
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

export interface Notification {
  id: number;
  userId: number;
  orderId?: number;
  message: string;
  notificationType: 'order_status' | 'document_ready' | 'reminder' | 'approval_required';
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