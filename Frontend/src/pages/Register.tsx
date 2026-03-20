import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { authApi } from '../services/api';
import type { RegisterRequest } from '../types';

// Страница регистрации нового клиента (юридического лица)
// После успешной регистрации автоматически выполняет вход и перенаправляет в профиль
export default function Register() {
  const navigate = useNavigate();
  const { login } = useAuthStore();

  // Состояние формы — все поля регистрации
  const [formData, setFormData] = useState<RegisterRequest>({
    email: '',
    password: '',
    fullName: '',
    phone: '',
    bin: '',           // БИН компании (необязательно)
    companyName: '',   // Название компании (необязательно)
    companyAddress: '', // Адрес компании (необязательно)
  });
  const [error, setError] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);

  // Универсальный обработчик изменения полей формы
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    // Валидация обязательных полей на фронтенде
    if (!formData.email || !formData.password || !formData.fullName) {
      setError('Заполните обязательные поля');
      return;
    }

    if (formData.password.length < 6) {
      setError('Пароль должен быть не менее 6 символов');
      return;
    }

    setIsLoading(true);

    try {
      // Отправляем данные на бэкенд — пароль хэшируется через BCrypt на сервере
      const response = await authApi.register(formData);
      const { token, user } = response.data;

      // Сохраняем токен и данные пользователя в глобальном хранилище
      login(user, token);
      navigate('/profile');
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Ошибка регистрации';
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="register-container">
      <div className="register-box">
        <h1>Регистрация</h1>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit}>
          {/* Секция: личные данные пользователя */}
          <div className="form-section">
            <h3>Личные данные</h3>

            <div className="form-group">
              <label htmlFor="email">Email *</label>
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
              <label htmlFor="password">Пароль *</label>
              <input
                id="password"
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                required
                placeholder="Минимум 6 символов"
              />
            </div>

            <div className="form-group">
              <label htmlFor="fullName">ФИО *</label>
              <input
                id="fullName"
                type="text"
                name="fullName"
                value={formData.fullName}
                onChange={handleChange}
                required
                placeholder="Иван Иванов"
              />
            </div>

            <div className="form-group">
              <label htmlFor="phone">Телефон</label>
              <input
                id="phone"
                type="tel"
                name="phone"
                value={formData.phone}
                onChange={handleChange}
                placeholder="+7 (777) 123-45-67"
              />
            </div>
          </div>

          {/* Секция: данные компании (юридического лица) — необязательно */}
          <div className="form-section">
            <h3>Данные компании</h3>

            <div className="form-group">
              <label htmlFor="bin">БИН компании</label>
              <input
                id="bin"
                type="text"
                name="bin"
                value={formData.bin}
                onChange={handleChange}
                placeholder="123456789012"
              />
            </div>

            <div className="form-group">
              <label htmlFor="companyName">Название компании</label>
              <input
                id="companyName"
                type="text"
                name="companyName"
                value={formData.companyName}
                onChange={handleChange}
                placeholder="ООО Моя компания"
              />
            </div>

            <div className="form-group">
              <label htmlFor="companyAddress">Адрес компании</label>
              <input
                id="companyAddress"
                type="text"
                name="companyAddress"
                value={formData.companyAddress}
                onChange={handleChange}
                placeholder="ул. Тестовая, д. 1"
              />
            </div>
          </div>

          <button type="submit" disabled={isLoading}>
            {isLoading ? 'Регистрация...' : 'Зарегистрироваться'}
          </button>
        </form>

        <div className="links">
          <p>
            Уже есть аккаунт? <a href="/login">Войди</a>
          </p>
        </div>
      </div>
    </div>
  );
}