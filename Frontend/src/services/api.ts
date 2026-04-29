import axios from 'axios';
import type { AxiosInstance } from 'axios';
import type { AuthResponse, LoginRequest, RegisterRequest, ForgotPasswordRequest, ResetPasswordRequest } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081/api';

const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && window.location.pathname !== '/login') {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authApi = {
  login: (data: LoginRequest) => api.post<AuthResponse>('/auth/login', data),
  register: (data: RegisterRequest) => api.post<AuthResponse>('/auth/register', data),
  forgotPassword: (data: ForgotPasswordRequest) => api.post('/auth/forgot-password', data),
  resetPassword: (data: ResetPasswordRequest) => api.post('/auth/reset-password', data),
};

export const serviceApi = {
  getAll: () => api.get('/services'),
  getById: (id: number) => api.get(`/services/${id}`),
  getByMeasurementType: (type: string) => api.get(`/services/type/${type}`),
  getByLabId: (labId: number) => api.get(`/services/lab/${labId}`),
};

export const orderApi = {
  getAll: (labId?: number) => api.get('/orders', { params: labId ? { labId } : {} }),
  getById: (id: number) => api.get(`/orders/${id}`),
  getMyOrders: (clientId: number) => api.get('/orders/my-orders', { params: { clientId } }),
  getByLabId: (labId: number) => api.get(`/orders/lab/${labId}`),
  getByStatus: (status: string) => api.get(`/orders/status/${status}`),
  create: (data: any) => api.post('/orders', data),
  updateStatus: (id: number, status: string) => api.put(`/orders/${id}/status`, { status }),
  confirmPayment: (id: number, paid: boolean, comment?: string, invoiceAmount?: number | null) =>
    api.put(`/orders/${id}/payment`, { paid, comment, invoiceAmount }),
  update: (id: number, data: any) => api.put(`/orders/${id}`, data),
};

export const contractApi = {
  getByOrderId: (orderId: number) => api.get(`/contracts/${orderId}`),
  create: (orderId: number) => api.post(`/contracts/${orderId}`),

  // Менеджер отправляет на согласование
  submit: (orderId: number) => api.put(`/contracts/${orderId}/submit`),

  // Согласующий одобряет/отклоняет
  approve: (orderId: number, userId: number) =>
    api.put(`/contracts/${orderId}/approve`, { userId }),
  reject: (orderId: number, userId: number, reason: string) =>
    api.put(`/contracts/${orderId}/reject`, { userId, reason }),

  // Директор подписывает
  signByDirector: (orderId: number, userId: number) =>
    api.put(`/contracts/${orderId}/sign/director`, { userId }),

  // Клиент подписывает
  signByClient: (orderId: number, userId: number) =>
    api.put(`/contracts/${orderId}/sign/client`, { userId }),

  // Аннулирование и расторжение
  annul: (orderId: number, userId: number, reason: string) =>
    api.put(`/contracts/${orderId}/annul`, { userId, reason }),
  terminate: (orderId: number, userId: number, reason: string) =>
    api.put(`/contracts/${orderId}/terminate`, { userId, reason }),

  download: (orderId: number) =>
    api.get(`/contracts/${orderId}/download`, { responseType: 'blob' }),
};

export const resultApi = {
  getByOrderId: (orderId: number) => api.get(`/results/order/${orderId}`),
  create: (data: any) => api.post('/results', data),
};

export const notificationApi = {
  getAll: () => api.get('/notifications'),
  getUnread: () => api.get('/notifications/unread'),
  markAsRead: (id: number) => api.put(`/notifications/${id}/read`),
};

export const userApi = {
  getProfile: () => api.get('/profile'),
  updateProfile: (data: any) => api.put('/profile', data),
  getClients: () => api.get('/users/clients'),
};

export default api;