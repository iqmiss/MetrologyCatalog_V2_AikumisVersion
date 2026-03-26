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

// Добавляем JWT токен в каждый запрос через заголовок Authorization
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Если токен истёк (401) — очищаем хранилище и перенаправляем на логин
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

// Auth endpoints
export const authApi = {
  login: (data: LoginRequest) => api.post<AuthResponse>('/auth/login', data),
  register: (data: RegisterRequest) => api.post<AuthResponse>('/auth/register', data),
  forgotPassword: (data: ForgotPasswordRequest) => api.post('/auth/forgot-password', data),
  resetPassword: (data: ResetPasswordRequest) => api.post('/auth/reset-password', data),
};

// Service endpoints
export const serviceApi = {
  getAll: () => api.get('/services'),
  getById: (id: number) => api.get(`/services/${id}`),
  getByMeasurementType: (type: string) => api.get(`/services/type/${type}`),
  getByLabId: (labId: number) => api.get(`/services/lab/${labId}`),
};

// Order endpoints
export const orderApi = {
  getAll: () => api.get('/orders'),
  getById: (id: number) => api.get(`/orders/${id}`),
  getMyOrders: (clientId: number) => api.get('/orders/my-orders', { params: { clientId } }),
  getByLabId: (labId: number) => api.get(`/orders/lab/${labId}`),
  getByStatus: (status: string) => api.get(`/orders/status/${status}`),
  create: (data: any) => api.post('/orders', data),
  updateStatus: (id: number, status: string) => api.put(`/orders/${id}/status`, { status }),
};

// Contract endpoints — исправлены маршруты
export const contractApi = {
  getByOrderId: (orderId: number) => api.get(`/contracts/${orderId}`),
  sign: (orderId: number, userId: number) => api.put(`/contracts/${orderId}/sign`, { userId }),
  download: (orderId: number) => api.get(`/contracts/${orderId}/download`, { responseType: 'blob' }),
};

// Result endpoints
export const resultApi = {
  getByOrderId: (orderId: number) => api.get(`/results/order/${orderId}`),
  create: (data: any) => api.post('/results', data),
};

// Notification endpoints
export const notificationApi = {
  getAll: () => api.get('/notifications'),
  getUnread: () => api.get('/notifications/unread'),
  markAsRead: (id: number) => api.put(`/notifications/${id}/read`),
};

// User endpoints
export const userApi = {
  getProfile: () => api.get('/profile'),
  updateProfile: (data: any) => api.put('/profile', data),
};

export default api;