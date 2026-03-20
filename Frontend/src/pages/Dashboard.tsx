import { useState, useEffect } from 'react';
import api from '../services/api';

// Интерфейс для данных статистики получаемых с бэкенда
interface Stats {
  totalOrders: number;      // Всего заявок в системе
  completedOrders: number;  // Завершённых заявок
  inWorkOrders: number;     // Заявок в работе
  newOrders: number;        // Новых заявок
  awaitingPayment: number;  // Ожидают оплаты
  totalRevenue: number;     // Суммарная выручка по завершённым заявкам
  totalClients: number;     // Количество клиентов в системе
}

// Страница дашборда для роли manager
// Показывает агрегированную статистику по всем заявкам и пользователям
export default function Dashboard() {
  const [stats, setStats] = useState<Stats | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  // Загружаем статистику при монтировании компонента
  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      setIsLoading(true);
      // GET /api/stats — данные считаются на бэкенде через Stream API
      const response = await api.get('/stats');
      setStats(response.data);
    } catch (err) {
      setError('Ошибка при загрузке статистики');
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) return <div className="loading">Загрузка...</div>;
  if (error) return <div className="error-message">{error}</div>;
  if (!stats) return null;

  return (
    <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}>
      <h1 style={{ color: '#fff', marginBottom: '30px' }}>📊 Дашборд</h1>

      {/* Сетка карточек со статистикой */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
        gap: '20px',
        marginBottom: '40px'
      }}>
        <StatCard label="Всего заявок" value={stats.totalOrders} color="#3b82f6" icon="📋" />
        <StatCard label="Завершено" value={stats.completedOrders} color="#10b981" icon="✅" />
        <StatCard label="В работе" value={stats.inWorkOrders} color="#ec4899" icon="⚙️" />
        <StatCard label="Новые" value={stats.newOrders} color="#8b5cf6" icon="🆕" />
        <StatCard label="Ожидают оплаты" value={stats.awaitingPayment} color="#eab308" icon="💳" />
        <StatCard label="Клиентов" value={stats.totalClients} color="#f97316" icon="👥" />
        {/* Карточка выручки занимает 2 колонки благодаря wide=true */}
        <StatCard
          label="Выручка"
          value={stats.totalRevenue.toLocaleString() + ' ₸'}
          color="#0ea5e9"
          icon="💰"
          wide
        />
      </div>
    </div>
  );
}

// Пропсы для карточки статистики
interface StatCardProps {
  label: string;        // Название показателя
  value: number | string; // Значение показателя
  color: string;        // Цвет рамки и текста значения
  icon: string;         // Эмодзи иконка
  wide?: boolean;       // Растянуть на 2 колонки (для выручки)
}

// Компонент карточки статистики — переиспользуется для каждого показателя
function StatCard({ label, value, color, icon, wide }: StatCardProps) {
  return (
    <div style={{
      background: '#1a1a1a',
      border: `1px solid ${color}`,
      borderRadius: '8px',
      padding: '20px',
      gridColumn: wide ? 'span 2' : 'span 1',
    }}>
      <div style={{ fontSize: '28px', marginBottom: '8px' }}>{icon}</div>
      <div style={{ color: '#b0b0b0', fontSize: '13px', marginBottom: '6px' }}>{label}</div>
      {/* Значение отображается цветом соответствующим карточке */}
      <div style={{ color, fontSize: '28px', fontWeight: 700 }}>{value}</div>
    </div>
  );
}