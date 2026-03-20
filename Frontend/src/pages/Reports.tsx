import { useState, useEffect } from 'react';
import { orderApi } from '../services/api';
import type { Order } from '../types';

// Страница отчётов для роли manager
// Показывает все заявки в системе с фильтрацией по статусу и подсчётом выручки
export default function Reports() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [filterStatus, setFilterStatus] = useState('');

  // Словарь для перевода статусов на русский язык
  const statusLabels: Record<string, string> = {
    new: 'Новая',
    awaiting_payment: 'Ожидает оплаты',
    awaiting_delivery: 'Ожидает доставки',
    received_in_lab: 'Принято в лаб',
    in_work: 'В работе',
    under_review: 'На проверке',
    completed: 'Завершено',
  };

  // Цвета бейджей для каждого статуса
  const statusColors: Record<string, string> = {
    new: '#3b82f6',
    awaiting_payment: '#eab308',
    awaiting_delivery: '#f59e0b',
    received_in_lab: '#8b5cf6',
    in_work: '#ec4899',
    under_review: '#f97316',
    completed: '#10b981',
  };

  // Загружаем все заявки при монтировании компонента
  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setIsLoading(true);
      const response = await orderApi.getAll();
      setOrders(response.data);
    } catch (err) {
      setError('Ошибка при загрузке отчёта');
    } finally {
      setIsLoading(false);
    }
  };

  // Фильтруем заявки по выбранному статусу
  // Если фильтр не выбран — показываем все заявки
  const filteredOrders = filterStatus
    ? orders.filter((o) => o.status === filterStatus)
    : orders;

  // Считаем выручку только по завершённым заявкам из отфильтрованного списка
  const totalRevenue = filteredOrders
    .filter((o) => o.status === 'completed')
    .reduce((sum, o) => sum + o.totalPrice, 0);

  // Форматируем дату в читаемый вид для русской локали
  const formatDate = (dateString?: string) => {
    if (!dateString) return '—';
    return new Date(dateString).toLocaleDateString('ru-RU');
  };

  if (isLoading) return <div className="loading">Загрузка...</div>;
  if (error) return <div className="error-message">{error}</div>;

  return (
    <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}>
      <h1 style={{ color: '#fff', marginBottom: '20px' }}>📈 Отчёты</h1>

      {/* Панель фильтров и статистики */}
      <div style={{ display: 'flex', gap: '16px', marginBottom: '20px', alignItems: 'center' }}>
        {/* Фильтр по статусу */}
        <select
          value={filterStatus}
          onChange={(e) => setFilterStatus(e.target.value)}
          className="filter-select"
          style={{ maxWidth: '220px' }}
        >
          <option value="">Все статусы</option>
          {Object.entries(statusLabels).map(([key, label]) => (
            <option key={key} value={key}>{label}</option>
          ))}
        </select>

        {/* Счётчик найденных заявок */}
        <span style={{ color: '#b0b0b0', fontSize: '14px' }}>
          Найдено: <strong style={{ color: '#fff' }}>{filteredOrders.length}</strong> заявок
        </span>

        {/* Выручка показывается только при фильтре "Все" или "Завершено" */}
        {filterStatus === 'completed' || filterStatus === '' ? (
          <span style={{ color: '#b0b0b0', fontSize: '14px' }}>
            Выручка: <strong style={{ color: '#10b981' }}>{totalRevenue.toLocaleString()} ₸</strong>
          </span>
        ) : null}
      </div>

      {/* Таблица заявок */}
      <div style={{
        background: '#1a1a1a',
        border: '1px solid #333',
        borderRadius: '8px',
        overflow: 'hidden'
      }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ background: '#252525', borderBottom: '1px solid #333' }}>
              {['№ Заявки', 'Клиент ID', 'Статус', 'Стоимость', 'Срок'].map((h) => (
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
            {filteredOrders.length === 0 ? (
              <tr>
                <td colSpan={5} style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
                  Нет заявок
                </td>
              </tr>
            ) : (
              filteredOrders.map((order, i) => (
                // Чередуем цвет строк для лучшей читаемости
                <tr key={order.id} style={{
                  borderBottom: '1px solid #2a2a2a',
                  background: i % 2 === 0 ? '#1a1a1a' : '#1e1e1e'
                }}>
                  <td style={{ padding: '12px 16px', color: '#fff' }}>#{order.orderNumber}</td>
                  <td style={{ padding: '12px 16px', color: '#b0b0b0' }}>{order.clientId}</td>
                  <td style={{ padding: '12px 16px' }}>
                    {/* Цветной бейдж статуса */}
                    <span style={{
                      background: statusColors[order.status],
                      color: '#fff',
                      padding: '3px 10px',
                      borderRadius: '12px',
                      fontSize: '12px'
                    }}>
                      {statusLabels[order.status] || order.status}
                    </span>
                  </td>
                  <td style={{ padding: '12px 16px', color: '#0ea5e9' }}>
                    {order.totalPrice.toLocaleString()} ₸
                  </td>
                  <td style={{ padding: '12px 16px', color: '#b0b0b0' }}>
                    {formatDate(order.dueDate)}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}