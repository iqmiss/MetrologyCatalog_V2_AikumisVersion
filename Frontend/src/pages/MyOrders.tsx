import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import api, { orderApi } from '../services/api';
import type { Order } from '../types';
import { downloadCertificate } from '../utils/download';

// Страница личного кабинета клиента
// Показывает все заявки клиента с возможностью оплаты, скачивания договора и сертификата
export default function MyOrders() {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [orders, setOrders] = useState<Order[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string>('');
  const [filterStatus, setFilterStatus] = useState<string>('');
  const [downloadingId, setDownloadingId] = useState<number | null>(null);
  const [payingId, setPayingId] = useState<number | null>(null);

  // Хранит статус договора для каждой заявки
  // none — договор не скачивался, exists — скачан, signed — подписан ЭЦП
  const [contractStatus, setContractStatus] = useState<Record<number, 'none' | 'exists' | 'signed'>>({});

  // Словарь для перевода статусов на русский язык
  const statusLabels: Record<string, string> = {
    new: 'Новая',
    awaiting_payment: 'Ожидает оплаты',
    awaiting_delivery: 'Ожидает доставки',
    received_in_lab: 'Принято в лаб',
    in_work: 'В работе',
    under_review: 'На проверке',
    completed: 'Завершено',
    cancelled: 'Отменено',
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

  // Загружаем заявки текущего клиента при монтировании компонента
  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setIsLoading(true);
      // Получаем только заявки текущего пользователя по его ID
      const response = await orderApi.getMyOrders(user?.id || 0);
      setOrders(response.data);
    } catch (err: any) {
      setError('Ошибка при загрузке заявок');
    } finally {
      setIsLoading(false);
    }
  };

  // Имитация оплаты — меняет статус с awaiting_payment на awaiting_delivery
  const handlePay = async (orderId: number) => {
    if (!window.confirm('Подтвердить оплату?')) return;
    try {
      setPayingId(orderId);
      await orderApi.updateStatus(orderId, 'awaiting_delivery');
      // Обновляем статус локально без перезагрузки страницы
      setOrders((prev) =>
        prev.map((order) =>
          order.id === orderId ? { ...order, status: 'awaiting_delivery' as Order['status'] } : order
        )
      );
    } catch (err) {
      setError('Ошибка при оплате');
    } finally {
      setPayingId(null);
    }
  };

  // Скачивает PDF договора
  // Если договор ещё не создан — создаёт его автоматически через POST
  const handleContract = async (orderId: number) => {
    try {
      await api.post(`/contracts/${orderId}`);
      
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8081/api/contracts/${orderId}/download`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (!response.ok) throw new Error('Ошибка загрузки');

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `contract_${orderId}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);

      setContractStatus(prev => ({ ...prev, [orderId]: 'exists' }));
    } catch (err) {
      setError('Ошибка при создании договора');
    }
  };

  // Имитация подписания договора через ЭЦП
  // Отправляет запрос на бэкенд с ID пользователя и временем подписания
  const handleSignContract = async (orderId: number) => {
    if (!window.confirm('Подписать договор ЭЦП?')) return;
    try {
      await api.put(`/contracts/${orderId}/sign`, { userId: user?.id });
      // Обновляем статус договора локально
      setContractStatus(prev => ({ ...prev, [orderId]: 'signed' }));
      alert('Договор подписан ЭЦП ✓');
    } catch (err) {
      setError('Ошибка при подписании');
    }
  };

  // Фильтруем заявки по выбранному статусу
  const filteredOrders = filterStatus
    ? orders.filter((order) => order.status === filterStatus)
    : orders;

  const getStatusColor = (status: string): string => statusColors[status] || '#888';

  // Форматируем дату в читаемый вид для русской локали
  const formatDate = (dateString?: string): string => {
    if (!dateString) return 'Не указана';
    return new Date(dateString).toLocaleDateString('ru-RU');
  };

  if (isLoading) {
    return (
      <div className="my-orders-container">
        <div className="loading">Загрузка заявок...</div>
      </div>
    );
  }

  return (
    <div className="my-orders-container">
      <h1>Мои заявки</h1>

      {error && <div className="error-message">{error}</div>}

      {/* Шапка со статистикой и кнопкой создания заявки */}
      <div className="orders-header">
        <div className="orders-stats">
          <div className="stat">
            <span className="stat-label">Всего заявок:</span>
            <span className="stat-value">{orders.length}</span>
          </div>
          <div className="stat">
            <span className="stat-label">В процессе:</span>
            <span className="stat-value">
              {orders.filter((o) => o.status !== 'completed').length}
            </span>
          </div>
          <div className="stat">
            <span className="stat-label">Завершено:</span>
            <span className="stat-value">
              {orders.filter((o) => o.status === 'completed').length}
            </span>
          </div>
        </div>

        <button className="btn-new-order" onClick={() => navigate('/create-order')}>
          ➕ Новая заявка
        </button>
      </div>

      {/* Фильтр по статусу */}
      <div className="filters">
        <select
          value={filterStatus}
          onChange={(e) => setFilterStatus(e.target.value)}
          className="filter-select"
        >
          <option value="">Все статусы</option>
          {Object.entries(statusLabels).map(([key, label]) => (
            <option key={key} value={key}>{label}</option>
          ))}
        </select>
      </div>

      {filteredOrders.length === 0 ? (
        <div className="no-orders">
          <p>Нет заявок</p>
          <button className="btn-create" onClick={() => navigate('/create-order')}>
            Создать первую заявку
          </button>
        </div>
      ) : (
        <div className="orders-list">
          {filteredOrders.map((order) => (
            <div key={order.id} className="order-card">
              {/* Заголовок карточки: номер заявки и статус */}
              <div className="order-header">
                <div className="order-number">
                  <h3>Заявка #{order.orderNumber}</h3>
                  <span
                    className="order-status"
                    style={{ borderColor: getStatusColor(order.status) }}
                  >
                    {statusLabels[order.status] || order.status}
                  </span>
                </div>
                <button className="btn-view" onClick={() => navigate(`/my-orders/${order.id}`)}>
                  Подробнее
                </button>
              </div>

              {/* Детали заявки */}
              <div className="order-details">
                <div className="detail-row">
                  <span className="label">Статус:</span>
                  <span
                    className="status-badge"
                    style={{ backgroundColor: getStatusColor(order.status) }}
                  >
                    {statusLabels[order.status] || order.status}
                  </span>
                </div>
                <div className="detail-row">
                  <span className="label">Стоимость:</span>
                  <span className="value">{order.totalPrice.toLocaleString()} ₸</span>
                </div>
                <div className="detail-row">
                  <span className="label">Плановая дата:</span>
                  <span className="value">{formatDate(order.dueDate)}</span>
                </div>
                <div className="detail-row">
                  <span className="label">ID лаборатории:</span>
                  <span className="value">{order.labId}</span>
                </div>
              </div>

              {/* Кнопки действий — зависят от текущего статуса заявки */}
              <div className="order-actions">
                <button className="btn-action" onClick={() => navigate(`/my-orders/${order.id}`)}>
                  Просмотр
                </button>

                {/* Отмена — только для новых заявок */}
                {(order.status === 'new' || order.status === 'awaiting_payment') && (
                <button
                  className="btn-action btn-secondary"
                  onClick={async () => {
                    if (!window.confirm('Отменить заявку?')) return;
                    try {
                      await orderApi.updateStatus(order.id, 'cancelled');
                      setOrders(prev =>
                        prev.map(o => o.id === order.id ? { ...o, status: 'cancelled' as Order['status'] } : o)
                      );
                    } catch (err) {
                      setError('Ошибка при отмене заявки');
                    }
                  }}
                >
                  Отменить
                </button>
              )}

                {/* Оплата — только для заявок в статусе awaiting_payment */}
                {order.status === 'awaiting_payment' && (
                  <button
                    className="btn-action btn-pay"
                    onClick={() => handlePay(order.id)}
                    disabled={payingId === order.id}
                  >
                    💳 Оплатить
                  </button>
                )}

                {/* Договор и ЭЦП — для всех статусов кроме new */}
                {order.status !== 'new' && (
                  <>
                    <button
                      className="btn-action"
                      style={{ background: '#1d4ed8' }}
                      onClick={() => handleContract(order.id)}
                    >
                      📝 Договор
                    </button>

                    {/* Кнопка подписания — появляется после скачивания договора */}
                    {contractStatus[order.id] === 'exists' && (
                      <button
                        className="btn-action"
                        style={{ background: '#065f46' }}
                        onClick={() => handleSignContract(order.id)}
                      >
                        ✍️ Подписать ЭЦП
                      </button>
                    )}

                    {/* Отметка о подписании */}
                    {contractStatus[order.id] === 'signed' && (
                      <span style={{ color: '#10b981', fontSize: '13px' }}>✓ Подписано</span>
                    )}
                  </>
                )}

                {/* Сертификат — только для завершённых заявок */}
                {order.status === 'completed' && (
                  <button
                    className="btn-action btn-pdf"
                    onClick={() => downloadCertificate(order.id, order.orderNumber, setError, setDownloadingId)}
                    disabled={downloadingId === order.id}
                  >
                    {downloadingId === order.id ? 'Загрузка...' : '📄 Сертификат'}
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}