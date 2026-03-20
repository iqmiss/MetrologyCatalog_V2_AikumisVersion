import { useState } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { authApi } from '../services/api';

// Страница сброса пароля — открывается по ссылке из email
// URL содержит токен: /reset-password?token=UUID
export default function ResetPassword() {
  const navigate = useNavigate();

  // Извлекаем токен из URL параметра
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  // Состояние формы
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    // Валидация на фронтенде перед отправкой запроса
    if (password.length < 6) {
      setError('Пароль должен быть не менее 6 символов');
      return;
    }

    if (password !== confirm) {
      setError('Пароли не совпадают');
      return;
    }

    // Проверяем что токен есть в URL
    if (!token) {
      setError('Недействительная ссылка');
      return;
    }

    try {
      setIsLoading(true);

      // Отправляем токен и новый пароль на бэкенд
      // Бэкенд проверит токен и обновит пароль через BCrypt
      await authApi.resetPassword({ token, newPassword: password });

      setSuccess('Пароль успешно изменён!');

      // Перенаправляем на страницу входа через 2 секунды
      setTimeout(() => navigate('/login'), 2000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка при сбросе пароля');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h1>Новый пароль</h1>

        {error && <div className="error-message">{error}</div>}

        {/* Сообщение об успехе */}
        {success && (
          <div style={{
            padding: '12px 15px',
            background: '#14532d',
            border: '1px solid #16a34a',
            borderRadius: '6px',
            color: '#86efac',
            marginBottom: '20px',
            fontSize: '14px'
          }}>
            {success}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Новый пароль</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Минимум 6 символов"
              required
            />
          </div>

          <div className="form-group">
            <label>Подтвердите пароль</label>
            <input
              type="password"
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
              placeholder="Повторите пароль"
              required
            />
          </div>

          <button type="submit" disabled={isLoading}>
            {isLoading ? 'Сохранение...' : 'Сохранить пароль'}
          </button>
        </form>

        <div className="links">
          <p><Link to="/login">Вернуться к входу</Link></p>
        </div>
      </div>
    </div>
  );
}