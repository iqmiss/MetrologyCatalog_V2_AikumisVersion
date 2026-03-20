import { useState, useEffect } from 'react';
import api from '../services/api';

// Интерфейс пользователя для страницы администрирования
interface User {
  id: number;
  email: string;
  fullName: string;
  role: string;
  phone: string;
  active: boolean;
}

// Страница управления пользователями для роли admin
// Позволяет менять роли и блокировать/активировать аккаунты
export default function AdminUsers() {
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  // Словарь для перевода ролей на русский язык
  const roleLabels: Record<string, string> = {
    client: 'Клиент',
    metrolog: 'Метролог',
    manager: 'Руководитель',
    admin: 'Администратор',
  };

  // Цвета для визуального выделения каждой роли
  const roleColors: Record<string, string> = {
    client: '#3b82f6',
    metrolog: '#8b5cf6',
    manager: '#f97316',
    admin: '#dc2626',
  };

  // Загружаем всех пользователей при монтировании компонента
  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setIsLoading(true);
      // GET /api/users — доступен только администратору
      const response = await api.get('/users');
      setUsers(response.data);
    } catch (err) {
      setError('Ошибка при загрузке пользователей');
    } finally {
      setIsLoading(false);
    }
  };

  // Меняет роль пользователя через выпадающий список
  // Обновляет данные локально без перезагрузки страницы
  const handleRoleChange = async (userId: number, newRole: string) => {
    try {
      await api.put(`/users/${userId}/role`, { role: newRole });
      setUsers((prev) =>
        prev.map((u) => (u.id === userId ? { ...u, role: newRole } : u))
      );
    } catch (err) {
      setError('Ошибка при смене роли');
    }
  };

  // Переключает статус активности пользователя (блокировка/разблокировка)
  // Заблокированный пользователь не может войти в систему
  const handleToggleActive = async (userId: number, isActive: boolean) => {
    try {
      // Передаём инвертированное значение — меняем на противоположное
      await api.put(`/users/${userId}/active`, { active: !isActive });
      setUsers((prev) =>
        prev.map((u) => (u.id === userId ? { ...u, active: !isActive } : u))
      );
    } catch (err) {
      setError('Ошибка при изменении статуса пользователя');
    }
  };

  if (isLoading) return <div className="loading">Загрузка...</div>;
  if (error) return <div className="error-message">{error}</div>;

  return (
    <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}>
      <h1 style={{ color: '#fff', marginBottom: '20px' }}>👥 Управление пользователями</h1>

      <div style={{ color: '#b0b0b0', marginBottom: '20px', fontSize: '14px' }}>
        Всего пользователей: <strong style={{ color: '#fff' }}>{users.length}</strong>
      </div>

      {/* Таблица пользователей */}
      <div style={{
        background: '#1a1a1a',
        border: '1px solid #333',
        borderRadius: '8px',
        overflow: 'hidden'
      }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ background: '#252525', borderBottom: '1px solid #333' }}>
              {['ID', 'ФИО', 'Email', 'Телефон', 'Роль', 'Статус', 'Действия'].map((h) => (
                <th key={h} style={{
                  padding: '12px 16px',
                  textAlign: 'left',
                  color: '#0ea5e9',
                  fontSize: '13px',
                  fontWeight: 600
                }}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {users.map((user, i) => (
              // Заблокированные пользователи отображаются полупрозрачными
              <tr key={user.id} style={{
                borderBottom: '1px solid #2a2a2a',
                background: i % 2 === 0 ? '#1a1a1a' : '#1e1e1e',
                opacity: user.active ? 1 : 0.5
              }}>
                <td style={{ padding: '12px 16px', color: '#666' }}>{user.id}</td>
                <td style={{ padding: '12px 16px', color: '#fff' }}>{user.fullName || '—'}</td>
                <td style={{ padding: '12px 16px', color: '#b0b0b0' }}>{user.email}</td>
                <td style={{ padding: '12px 16px', color: '#b0b0b0' }}>{user.phone || '—'}</td>
                <td style={{ padding: '12px 16px' }}>
                  {/* Выпадающий список для смены роли — цвет рамки соответствует роли */}
                  <select
                    value={user.role}
                    onChange={(e) => handleRoleChange(user.id, e.target.value)}
                    style={{
                      background: '#2a2a2a',
                      border: `1px solid ${roleColors[user.role]}`,
                      borderRadius: '6px',
                      color: roleColors[user.role],
                      padding: '4px 8px',
                      fontSize: '13px',
                      cursor: 'pointer'
                    }}
                  >
                    {Object.entries(roleLabels).map(([key, label]) => (
                      <option key={key} value={key}>{label}</option>
                    ))}
                  </select>
                </td>
                <td style={{ padding: '12px 16px' }}>
                  {/* Бейдж статуса активности */}
                  <span style={{
                    background: user.active ? '#10b981' : '#dc2626',
                    color: '#fff',
                    padding: '3px 10px',
                    borderRadius: '12px',
                    fontSize: '12px'
                  }}>
                    {user.active ? 'Активен' : 'Заблокирован'}
                  </span>
                </td>
                <td style={{ padding: '12px 16px' }}>
                  {/* Кнопка блокировки/активации — цвет меняется в зависимости от статуса */}
                  <button
                    onClick={() => handleToggleActive(user.id, user.active)}
                    style={{
                      padding: '6px 12px',
                      background: user.active ? '#7f1d1d' : '#14532d',
                      border: 'none',
                      borderRadius: '6px',
                      color: '#fff',
                      fontSize: '12px',
                      cursor: 'pointer',
                      marginBottom: 0
                    }}
                  >
                    {user.active ? 'Заблокировать' : 'Активировать'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}