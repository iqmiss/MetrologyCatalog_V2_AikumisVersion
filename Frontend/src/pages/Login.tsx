import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { authApi } from '../services/api';
import type { LoginRequest } from '../types';

// Страница входа в систему
// После успешного входа сохраняет токен и данные пользователя, перенаправляет в профиль
export default function Login() {
  const navigate = useNavigate();
  const { login } = useAuthStore();

  // Данные формы входа
  const [formData, setFormData] = useState<LoginRequest>({ email: '', password: '' });
  const [error, setLocalError] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);

  // Универсальный обработчик изменения полей формы
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLocalError('');
    setIsLoading(true);

    try {
      // Отправляем email и пароль на бэкенд
      // Бэкенд проверяет пароль через BCrypt и возвращает токен
      const response = await authApi.login(formData);
      const { token, user } = response.data;

      // Сохраняем токен и данные пользователя в глобальном хранилище (Zustand)
      login(user, token);
      navigate('/profile');
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Ошибка входа';
      setLocalError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h1>Вход в систему</h1>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="email">Email:</label>
            <input
              id="email"
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              required
              placeholder="your@email.com"
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Пароль:</label>
            <input
              id="password"
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              required
              placeholder="••••••••"
            />
          </div>

          <button type="submit" disabled={isLoading}>
            {isLoading ? 'Вход...' : 'Войти'}
          </button>
        </form>

        {/* Ссылки на регистрацию и восстановление пароля */}
        <div className="links">
          <p>
            Нет аккаунта? <a href="/register">Зарегистрируйся</a>
          </p>
          <p>
            <a href="/forgot-password">Забыли пароль?</a>
          </p>
        </div>
      </div>
    </div>
  );
}