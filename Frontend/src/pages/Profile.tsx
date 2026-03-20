import { useState } from 'react';
import { useAuthStore } from '../store/authStore';
import { userApi } from '../services/api';
import type { User } from '../types';

// Страница профиля пользователя
// Показывает личные данные и позволяет их редактировать
// Доступна всем ролям
export default function Profile() {
  const { user, logout } = useAuthStore();

  // Режим редактирования — переключается кнопкой "Редактировать профиль"
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string>('');
  const [success, setSuccess] = useState<string>('');

  // Данные формы редактирования — инициализируются текущими данными пользователя
  const [formData, setFormData] = useState<Partial<User>>({
    fullName: user?.fullName || '',
    email: user?.email || '',
    phone: user?.phone || '',
  });

  // Универсальный обработчик изменения полей формы
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Валидация обязательных полей
    if (!formData.fullName || !formData.email) {
      setError('Заполните обязательные поля');
      return;
    }

    try {
      setIsSaving(true);
      // Отправляем обновлённые данные на бэкенд
      await userApi.updateProfile(formData);
      setSuccess('Профиль обновлён успешно!');
      setIsEditing(false);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка при обновлении профиля');
    } finally {
      setIsSaving(false);
    }
  };

  // Словарь для перевода ролей на русский язык
  const roleLabels: Record<string, string> = {
    client: 'Клиент',
    metrolog: 'Метролог',
    manager: 'Менеджер',
    admin: 'Администратор',
  };

  return (
    <div className="profile-container">
      <div className="profile-box">
        <h1>Мой профиль</h1>

        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        {/* Шапка профиля — аватар с первой буквой имени и базовая информация */}
        <div className="profile-header">
          <div className="profile-avatar">
            <span>{user?.fullName?.charAt(0).toUpperCase() || 'U'}</span>
          </div>
          <div className="profile-info">
            <h2>{user?.fullName}</h2>
            <p className="role-badge">{roleLabels[user?.role || 'client']}</p>
          </div>
        </div>

        {/* Режим просмотра — показывает данные профиля */}
        {!isEditing ? (
          <>
            <div className="profile-details">
              {/* Секция: контактная информация */}
              <div className="detail-section">
                <h3>Контактная информация</h3>

                <div className="detail-item">
                  <span className="label">Email:</span>
                  <span className="value">{user?.email}</span>
                </div>

                <div className="detail-item">
                  <span className="label">Телефон:</span>
                  <span className="value">{user?.phone || 'Не указан'}</span>
                </div>

                <div className="detail-item">
                  <span className="label">ФИО:</span>
                  <span className="value">{user?.fullName}</span>
                </div>
              </div>

              {/* Секция: информация об аккаунте */}
              <div className="detail-section">
                <h3>Информация об аккаунте</h3>

                <div className="detail-item">
                  <span className="label">Роль:</span>
                  <span className="value">
                    {roleLabels[user?.role || 'client']}
                  </span>
                </div>

                <div className="detail-item">
                  <span className="label">ID компании:</span>
                  <span className="value">{user?.companyId || 'Не указана'}</span>
                </div>

                <div className="detail-item">
                  <span className="label">Статус:</span>
                  <span className="value status-active">
                    {user?.isActive ? 'Активен' : 'Неактивен'}
                  </span>
                </div>
              </div>
            </div>

            {/* Кнопки действий в режиме просмотра */}
            <div className="profile-actions">
              <button
                className="btn-primary"
                onClick={() => setIsEditing(true)}
              >
                ✏️ Редактировать профиль
              </button>
              <button
                className="btn-secondary"
                onClick={() => {
                  // Выходим из аккаунта и очищаем хранилище
                  logout();
                  window.location.href = '/login';
                }}
              >
                🚪 Выйти
              </button>
            </div>
          </>
        ) : (
          /* Режим редактирования — форма с полями для изменения данных */
          <form onSubmit={handleSave} className="edit-form">
            <div className="form-group">
              <label htmlFor="fullName">ФИО *</label>
              <input
                id="fullName"
                type="text"
                name="fullName"
                value={formData.fullName}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="email">Email *</label>
              <input
                id="email"
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                required
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

            {/* Кнопки действий в режиме редактирования */}
            <div className="form-actions">
              <button
                type="submit"
                className="btn-primary"
                disabled={isSaving}
              >
                {isSaving ? 'Сохранение...' : '💾 Сохранить'}
              </button>
              <button
                type="button"
                className="btn-secondary"
                onClick={() => {
                  // Отменяем редактирование и сбрасываем форму к исходным данным
                  setIsEditing(false);
                  setFormData({
                    fullName: user?.fullName || '',
                    email: user?.email || '',
                    phone: user?.phone || '',
                  });
                }}
                disabled={isSaving}
              >
                ✕ Отменить
              </button>
            </div>
          </form>
        )}

        {/* Футер с ID профиля */}
        <div className="profile-footer">
          <p className="text-muted">
            ID профиля: {user?.id}
          </p>
        </div>
      </div>
    </div>
  );
}