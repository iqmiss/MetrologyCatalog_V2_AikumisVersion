export interface User {
  id: number;
  email: string;
  role: 'client' | 'metrolog' | 'manager' | 'admin';
  fullName: string;
  phone?: string;
  companyId?: number;
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

export interface Order {
  id: number;
  orderNumber: string;
  clientId: number;
  serviceId: number;
  labId: number;
  status: 'new' | 'awaiting_payment' | 'awaiting_delivery' | 'received_in_lab' | 'in_work' | 'under_review' | 'completed';
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

export interface Contract {
  id: number;
  orderId: number;
  contractNumber: string;
  signedAt?: string;
  filePath?: string;
  isSigned: boolean;
  signedBy?: number;
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
  notificationType: 'order_status' | 'document_ready' | 'reminder';
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