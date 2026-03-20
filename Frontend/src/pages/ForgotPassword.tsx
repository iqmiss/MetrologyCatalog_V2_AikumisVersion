import { useState } from 'react';
import { Link } from 'react-router-dom';
import { authApi } from '../services/api';

// Страница восстановления пароля
// Пользователь вводит email и получает ссылку для сброса пароля
export default function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    try {
      setIsLoading(true);

      // Отправляем email на бэкенд
      // Бэкенд генерирует UUID токен и отправляет ссылку через Mailtrap SMTP
      await authApi.forgotPassword({ email });

      // Показываем одинаковое сообщение независимо от того существует email или нет
      // Это защита от перебора email адресов
      setSuccess('Ссылка для сброса пароля отправлена на email');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка при отправке');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h1>Восстановление пароля</h1>

        {error && <div className="error-message">{error}</div>}

        {/* Сообщение об успешной отправке */}
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
            <label>Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Введите ваш email"
              required
            />
          </div>

          <button type="submit" disabled={isLoading}>
            {isLoading ? 'Отправка...' : 'Отправить ссылку'}
          </button>
        </form>

        <div className="links">
          <p><Link to="/login">Вернуться к входу</Link></p>
        </div>
      </div>
    </div>
  );
}