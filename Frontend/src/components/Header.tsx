import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import './Header.css';

// Компонент шапки и боковой навигации
// Меню адаптируется под роль пользователя — каждая роль видит только свои разделы
export default function Header() {
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();

  // Состояние открытия/закрытия бокового меню (sidebar)
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleLogout = () => {
    // Очищаем токен и данные пользователя из хранилища и перенаправляем на логин
    logout();
    navigate('/login');
  };

  // Навигация с автоматическим закрытием бокового меню
  const navigateTo = (path: string) => {
    navigate(path);
    setSidebarOpen(false);
  };

  return (
    <>
      {/* Верхняя панель с логотипом и информацией о пользователе */}
      <header className="header">
        <div className="header-content">
          <div className="header-left">
            {/* Кнопка открытия бокового меню — видна только на мобильных */}
            <button
              className="menu-toggle"
              onClick={() => setSidebarOpen(!sidebarOpen)}
            >
              ☰
            </button>
            <h1 className="logo">Метрология</h1>
          </div>

          <div className="header-right">
            {/* Показываем имя и роль текущего пользователя */}
            <span className="user-info">
              {user?.fullName} ({user?.role})
            </span>
            <button className="logout-btn" onClick={handleLogout}>
              Выход
            </button>
          </div>
        </div>
      </header>

      {/* Боковое меню — навигация по разделам */}
      <aside className={`sidebar ${sidebarOpen ? 'open' : ''}`}>
        <nav className="sidebar-nav">
          <div className="sidebar-header">
            <h2>Меню</h2>
            <button
              className="close-btn"
              onClick={() => setSidebarOpen(false)}
            >
              ✕
            </button>
          </div>

          {/* Общие разделы — доступны всем авторизованным пользователям */}
          <div className="nav-section">
            <h3>Общее</h3>
            <ul>
              <li>
                <button onClick={() => navigateTo('/catalog')}>
                  📋 Каталог услуг
                </button>
              </li>
              <li>
                <button onClick={() => navigateTo('/profile')}>
                  👤 Профиль
                </button>
              </li>
            </ul>
          </div>

          {/* Разделы для клиента — подача и просмотр заявок */}
          {user?.role === 'client' && (
            <div className="nav-section">
              <h3>Клиент</h3>
              <ul>
                <li>
                  <button onClick={() => navigateTo('/create-order')}>
                    ➕ Новая заявка
                  </button>
                </li>
                <li>
                  <button onClick={() => navigateTo('/my-orders')}>
                    📦 Мои заявки
                  </button>
                </li>
              </ul>
            </div>
          )}

          {/* Разделы для метролога — очередь заявок */}
          {user?.role === 'metrolog' && (
            <div className="nav-section">
              <h3>Работа</h3>
              <ul>
                <li>
                  <button onClick={() => navigateTo('/queue')}>
                    ⏳ Очередь заявок
                  </button>
                </li>
              </ul>
            </div>
          )}

          {/* Разделы для менеджера — очередь, дашборд и отчёты */}
          {user?.role === 'manager' && (
            <div className="nav-section">
              <h3>Управление</h3>
              <ul>
                <li>
                  <button onClick={() => navigateTo('/queue')}>
                    ⏳ Очередь заявок
                  </button>
                </li>
                <li>
                  <button onClick={() => navigateTo('/dashboard')}>
                    📊 Дашборд
                  </button>
                </li>
                <li>
                  <button onClick={() => navigateTo('/reports')}>
                    📈 Отчёты
                  </button>
                </li>
              </ul>
            </div>
          )}

          {/* Разделы для администратора — управление пользователями и каталог */}
          {user?.role === 'admin' && (
            <div className="nav-section">
              <h3>Администрирование</h3>
              <ul>
                <li>
                  <button onClick={() => navigateTo('/admin/users')}>
                    👥 Пользователи
                  </button>
                </li>
                <li>
                  <button onClick={() => navigateTo('/catalog')}>
                    📋 Каталог услуг
                  </button>
                </li>
              </ul>
            </div>
          )}
        </nav>
      </aside>

      {/* Затемнение фона при открытом боковом меню — клик закрывает меню */}
      {sidebarOpen && (
        <div
          className="sidebar-overlay"
          onClick={() => setSidebarOpen(false)}
        />
      )}
    </>
  );
}