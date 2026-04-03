import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

// Компонент шапки и боковой навигации
// Меню адаптируется под роль пользователя — каждая роль видит только свои разделы
export default function Header() {
  const navigate = useNavigate();
  const location = useLocation();
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

  // Активный пункт меню — подсвечивается текущий маршрут
  const isActive = (path: string) => location.pathname === path;

  const navItemClass = (path: string) =>
    `w-full text-left px-4 py-2.5 rounded-lg text-sm font-medium transition-colors cursor-pointer border-none flex items-center gap-3 ${
      isActive(path)
        ? 'bg-[#0A2E5C] text-white'
        : 'text-gray-600'
    }`;

  const navItemStyle = (path: string): React.CSSProperties =>
    isActive(path)
      ? { marginBottom: 0, background: '#0A2E5C' }
      : { marginBottom: 0, background: 'white' };

  return (
    <>
      {/* Верхняя панель */}
      <header className="bg-white border-b border-gray-100 px-4 h-[60px] flex items-center justify-between sticky top-0 z-50 shadow-sm">
        <div className="flex items-center gap-3">
          {/* Кнопка открытия бокового меню — видна только на мобильных */}
          <button
            className="md:hidden p-2 rounded-lg text-gray-500 hover:bg-gray-100 border-none cursor-pointer"
            style={{ marginBottom: 0, background: 'none' }}
            onClick={() => setSidebarOpen(!sidebarOpen)}
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <path d="M4 5h16M4 12h16M4 19h16" />
            </svg>
          </button>

          {/* Логотип */}
          <div className="flex items-center gap-2 cursor-pointer" onClick={() => navigateTo('/')}>
            <div className="w-8 h-8 bg-gradient-to-br from-[#0A2E5C] to-[#00B2FF] rounded-lg flex items-center justify-center">
              <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                <path d="M9.671 4.136a2.34 2.34 0 0 1 4.659 0 2.34 2.34 0 0 0 3.319 1.915 2.34 2.34 0 0 1 2.33 4.033 2.34 2.34 0 0 0 0 3.831 2.34 2.34 0 0 1-2.33 4.033 2.34 2.34 0 0 0-3.319 1.915 2.34 2.34 0 0 1-4.659 0 2.34 2.34 0 0 0-3.32-1.915 2.34 2.34 0 0 1-2.33-4.033 2.34 2.34 0 0 0 0-3.831A2.34 2.34 0 0 1 6.35 6.051a2.34 2.34 0 0 0 3.319-1.915" />
                <circle cx="12" cy="12" r="3" />
              </svg>
            </div>
            <span className="font-bold text-[#0A2E5C] text-lg hidden sm:block">MetrologyCatalog</span>
          </div>
        </div>

        {/* Правая часть — имя пользователя и кнопка выхода */}
        <div className="flex items-center gap-3">
          <div className="hidden sm:flex items-center gap-2">
            <div className="w-8 h-8 bg-[#00B2FF] rounded-full flex items-center justify-center text-white text-sm font-bold">
              {user?.fullName?.charAt(0).toUpperCase() || 'U'}
            </div>
            <div className="text-right">
              <p className="text-sm font-medium text-[#0A2E5C]" style={{ margin: 0 }}>{user?.fullName}</p>
              <p className="text-xs text-gray-400" style={{ margin: 0 }}>{user?.role}</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="flex items-center gap-2 px-3 py-2 text-sm font-medium text-gray-500 hover:text-red-500 hover:bg-red-50 rounded-lg border-none cursor-pointer transition-colors"
            style={{ marginBottom: 0, background: 'none' }}
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline points="16 17 21 12 16 7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
            <span className="hidden sm:block">Выход</span>
          </button>
        </div>
      </header>

      {/* Боковое меню */}
      <aside className={`fixed left-0 top-[60px] h-[calc(100vh-60px)] w-[240px] bg-white border-r border-gray-100 z-40 transition-transform duration-300 overflow-y-auto
        ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'} md:translate-x-0`}>
        <nav className="p-4 flex flex-col gap-1">

          {/* Закрыть кнопка — только мобильная */}
          <div className="flex items-center justify-between mb-4 md:hidden">
            <span className="text-xs font-semibold text-gray-400 uppercase tracking-wider">Меню</span>
            <button
              onClick={() => setSidebarOpen(false)}
              className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 border-none cursor-pointer"
              style={{ marginBottom: 0, background: 'none' }}
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                <path d="M18 6 6 18M6 6l12 12" />
              </svg>
            </button>
          </div>

          {/* Общие разделы — доступны всем авторизованным пользователям */}
          <div className="mb-4">
            <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider px-3 mb-2" style={{ margin: '0 0 8px' }}>
              Общее
            </p>
            <button onClick={() => navigateTo('/catalog')} className={navItemClass('/catalog')} style={navItemStyle('/catalog')}>
              <svg className="w-4 h-4 shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1"/>
              </svg>
              Каталог услуг
            </button>
            <button onClick={() => navigateTo('/profile')} className={navItemClass('/profile')} style={navItemStyle('/profile')}>
              <svg className="w-4 h-4 shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                <circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/>
              </svg>
              Профиль
            </button>
          </div>

          {/* Разделы для клиента — подача и просмотр заявок */}
          {user?.role === 'client' && (
            <div className="mb-4">
              <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider px-3 mb-2" style={{ margin: '0 0 8px' }}>
                Клиент
              </p>
              <button onClick={() => navigateTo('/create-order')} className={navItemClass('/create-order')} style={navItemStyle('/create-order')}>
                <svg className="w-4 h-4 shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <circle cx="12" cy="12" r="10"/><path d="M12 8v8M8 12h8"/>
                </svg>
                Новая заявка
              </button>
              <button onClick={() => navigateTo('/my-orders')} className={navItemClass('/my-orders')} style={navItemStyle('/my-orders')}>
                <svg className="w-4 h-4 shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path d="M11 21.73a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73z"/>
                </svg>
                Мои заявки
              </button>
            </div>
          )}

          {/* Разделы для метролога — очередь заявок */}
          {user?.role === 'metrolog' && (
            <div className="mb-4">
              <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider px-3 mb-2" style={{ margin: '0 0 8px' }}>
                Работа
              </p>
              <button onClick={() => navigateTo('/queue')} className={navItemClass('/queue')} style={navItemStyle('/queue')}>
                <svg className="w-4 h-4 shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path d="M12 6v6l4 2"/><circle cx="12" cy="12" r="10"/>
                </svg>
                Очередь заявок
              </button>
            </div>
          )}

          {/* Разделы для менеджера — очередь, дашборд и отчёты */}
          {user?.role === 'manager' && (
            <div className="mb-4">
              <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider px-3 mb-2" style={{ margin: '0 0 8px' }}>
                Управление
              </p>
              <button onClick={() => navigateTo('/queue')} className={navItemClass('/queue')} style={navItemStyle('/queue')}>
                <svg className="w-4 h-4 shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path d="M12 6v6l4 2"/><circle cx="12" cy="12" r="10"/>
                </svg>
                Очередь заявок
              </button>
              <button onClick={() => navigateTo('/dashboard')} className={navItemClass('/dashboard')} style={navItemStyle('/dashboard')}>
                <svg className="w-4 h-4 shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/>
                </svg>
                Дашборд
              </button>
              <button onClick={() => navigateTo('/reports')} className={navItemClass('/reports')} style={navItemStyle('/reports')}>
                <svg className="w-4 h-4 shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path d="M16 7h6v6"/><path d="m22 7-8.5 8.5-5-5L2 17"/>
                </svg>
                Отчёты
              </button>
            </div>
          )}

          {/* Разделы для администратора — управление пользователями */}
          {user?.role === 'admin' && (
            <div className="mb-4">
              <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider px-3 mb-2" style={{ margin: '0 0 8px' }}>
                Администрирование
              </p>
              <button onClick={() => navigateTo('/admin/users')} className={navItemClass('/admin/users')} style={navItemStyle('/admin/users')}>
                <svg className="w-4 h-4 shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                </svg>
                Пользователи
              </button>
            </div>
          )}
        </nav>
      </aside>

      {/* Затемнение фона при открытом боковом меню на мобильных */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black/40 z-30 md:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}
    </>
  );
}