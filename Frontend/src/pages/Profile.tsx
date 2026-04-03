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
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Данные формы редактирования — инициализируются текущими данными пользователя
  const [formData, setFormData] = useState<Partial<User>>({
    fullName: user?.fullName || '',
    email: user?.email || '',
    phone: user?.phone || '',
  });

  // Универсальный обработчик изменения полей формы
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
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

  // Цвет бейджа роли
  const roleBadgeColor: Record<string, string> = {
    client: 'bg-blue-100 text-blue-700',
    metrolog: 'bg-green-100 text-green-700',
    manager: 'bg-purple-100 text-purple-700',
    admin: 'bg-red-100 text-red-700',
  };

  // Переиспользуемый класс для input полей
  const inputClass = "w-full px-4 py-3 border border-gray-200 rounded-xl text-gray-900 text-sm outline-none focus:border-[#00B2FF] focus:ring-2 focus:ring-[#00B2FF]/10 transition-all bg-white";

  return (
    <div className="min-h-screen bg-gray-50 py-10 px-4">
      <div className="max-w-2xl mx-auto">

        {/* Заголовок страницы */}
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-[#0A2E5C]" style={{ margin: 0, fontSize: '1.75rem' }}>
            Мой профиль
          </h1>
          <p className="text-gray-500 text-sm mt-1" style={{ margin: '4px 0 0' }}>
            Управляйте личными данными и настройками аккаунта
          </p>
        </div>

        {/* Уведомления */}
        {error && (
          <div className="flex items-center gap-3 p-4 bg-red-50 border border-red-200 rounded-xl mb-6 text-red-600 text-sm">
            <svg className="w-5 h-5 shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10"/><path d="M12 8v4m0 4h.01"/>
            </svg>
            {error}
          </div>
        )}
        {success && (
          <div className="flex items-center gap-3 p-4 bg-green-50 border border-green-200 rounded-xl mb-6 text-green-600 text-sm">
            <svg className="w-5 h-5 shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <path d="M21.801 10A10 10 0 1 1 17 3.335"/><path d="m9 11 3 3L22 4"/>
            </svg>
            {success}
          </div>
        )}

        {/* Карточка профиля */}
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">

          {/* Шапка — аватар, имя, роль */}
          <div className="px-8 py-6 border-b border-gray-100" style={{ background: 'linear-gradient(135deg, #0A2E5C 0%, #1E4A7C 100%)' }}>
            <div className="flex items-center gap-5">
              {/* Аватар с первой буквой имени */}
              <div className="w-16 h-16 bg-[#00B2FF] rounded-2xl flex items-center justify-center text-white text-2xl font-bold shrink-0">
                {user?.fullName?.charAt(0).toUpperCase() || 'U'}
              </div>
              <div>
                <h2 className="text-xl font-bold text-white" style={{ margin: '0 0 6px', fontSize: '1.25rem' }}>
                  {user?.fullName}
                </h2>
                <span className={`text-xs font-semibold px-3 py-1 rounded-full bg-white/20 text-white`}>
                  {roleLabels[user?.role || 'client']}
                </span>
              </div>
              <div className="ml-auto text-right">
                <p className="text-white/40 text-xs" style={{ margin: 0 }}>ID профиля</p>
                <p className="text-white/70 text-sm font-mono" style={{ margin: '2px 0 0' }}>#{user?.id}</p>
              </div>
            </div>
          </div>

          {/* Тело карточки */}
          <div className="px-8 py-6">
            {!isEditing ? (
              <>
                {/* Секция: контактная информация */}
                <div className="mb-6">
                  <p className="text-xs font-semibold text-[#00B2FF] uppercase tracking-wider mb-4" style={{ margin: '0 0 16px' }}>
                    Контактная информация
                  </p>
                  <div className="flex flex-col gap-3">
                    {[
                      { label: 'Email', value: user?.email },
                      { label: 'Телефон', value: user?.phone || 'Не указан' },
                      { label: 'ФИО', value: user?.fullName },
                    ].map(item => (
                      <div key={item.label} className="flex justify-between items-center py-3 border-b border-gray-50">
                        <span className="text-sm text-gray-400">{item.label}</span>
                        <span className="text-sm font-medium text-gray-800">{item.value}</span>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Секция: информация об аккаунте */}
                <div className="mb-8">
                  <p className="text-xs font-semibold text-[#00B2FF] uppercase tracking-wider mb-4" style={{ margin: '0 0 16px' }}>
                    Информация об аккаунте
                  </p>
                  <div className="flex flex-col gap-3">
                    <div className="flex justify-between items-center py-3 border-b border-gray-50">
                      <span className="text-sm text-gray-400">Роль</span>
                      <span className={`text-xs font-semibold px-3 py-1 rounded-full ${roleBadgeColor[user?.role || 'client']}`}>
                        {roleLabels[user?.role || 'client']}
                      </span>
                    </div>
                    <div className="flex justify-between items-center py-3 border-b border-gray-50">
                      <span className="text-sm text-gray-400">ID компании</span>
                      <span className="text-sm font-medium text-gray-800">{user?.companyId || 'Не указана'}</span>
                    </div>
                    <div className="flex justify-between items-center py-3 border-b border-gray-50">
                      <span className="text-sm text-gray-400">Статус</span>
                      <span className={`text-xs font-semibold px-3 py-1 rounded-full ${user?.isActive ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                        {user?.isActive ? 'Активен' : 'Неактивен'}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Кнопки действий в режиме просмотра */}
                <div className="flex gap-3">
                  <button
                    onClick={() => setIsEditing(true)}
                    className="flex-1 py-3 bg-[#00B2FF] hover:bg-[#0095D9] text-white font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors flex items-center justify-center gap-2"
                    style={{ marginBottom: 0 }}
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                    Редактировать
                  </button>
                  <button
                    onClick={() => { logout(); window.location.href = '/login'; }}
                    className="flex-1 py-3 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors flex items-center justify-center gap-2"
                    style={{ marginBottom: 0 }}
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                      <polyline points="16 17 21 12 16 7"/>
                      <line x1="21" y1="12" x2="9" y2="12"/>
                    </svg>
                    Выйти
                  </button>
                </div>
              </>
            ) : (
              /* Режим редактирования — форма с полями для изменения данных */
              <form onSubmit={handleSave} className="flex flex-col gap-5">
                <p className="text-xs font-semibold text-[#00B2FF] uppercase tracking-wider" style={{ margin: '0 0 4px' }}>
                  Редактирование данных
                </p>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">ФИО *</label>
                  <input type="text" name="fullName" value={formData.fullName} onChange={handleChange}
                    required className={inputClass} style={{ fontFamily: 'inherit', marginBottom: 0 }} />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Email *</label>
                  <input type="email" name="email" value={formData.email} onChange={handleChange}
                    required className={inputClass} style={{ fontFamily: 'inherit', marginBottom: 0 }} />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Телефон</label>
                  <input type="tel" name="phone" value={formData.phone} onChange={handleChange}
                    placeholder="+7 (777) 123-45-67" className={inputClass}
                    style={{ fontFamily: 'inherit', marginBottom: 0 }} />
                </div>

                {/* Кнопки действий в режиме редактирования */}
                <div className="flex gap-3 mt-2">
                  <button
                    type="submit"
                    disabled={isSaving}
                    className="flex-1 py-3 bg-[#00B2FF] hover:bg-[#0095D9] text-white font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors"
                    style={{ marginBottom: 0 }}
                  >
                    {isSaving ? 'Сохранение...' : 'Сохранить'}
                  </button>
                  <button
                    type="button"
                    disabled={isSaving}
                    onClick={() => {
                      // Отменяем редактирование и сбрасываем форму к исходным данным
                      setIsEditing(false);
                      setFormData({
                        fullName: user?.fullName || '',
                        email: user?.email || '',
                        phone: user?.phone || '',
                      });
                    }}
                    className="flex-1 py-3 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors"
                    style={{ marginBottom: 0 }}
                  >
                    Отменить
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}