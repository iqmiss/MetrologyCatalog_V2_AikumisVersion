import { useState, useEffect } from 'react';
import { useAuthStore } from '../store/authStore';
import api, { orderApi } from '../services/api';
import type { Order } from '../types';

// Страница очереди заявок для ролей metrolog и manager
// Показывает все заявки в системе и позволяет управлять их статусами
export default function Queue() {
  const { user } = useAuthStore();
  const [orders, setOrders] = useState<Order[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string>('');
  const [downloadingId, setDownloadingId] = useState<number | null>(null);

  // Состояние модалки для завершения заявки
  const [showModal, setShowModal] = useState(false);
  const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);
  const [resultType, setResultType] = useState('certificate');
  const [submitting, setSubmitting] = useState(false);

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

  // Определяет следующий статус для каждого текущего
  // awaiting_payment → awaiting_payment т.к. клиент оплачивает сам
  const statusFlow: Record<string, string> = {
    new: 'awaiting_payment',
    awaiting_payment: 'awaiting_payment',
    awaiting_delivery: 'received_in_lab',
    received_in_lab: 'in_work',
    in_work: 'under_review',
    under_review: 'completed',
    completed: 'completed',
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
      setError('Ошибка при загрузке заявок');
    } finally {
      setIsLoading(false);
    }
  };

  // Обрабатывает нажатие кнопки "Далее →"
  // Если следующий статус — completed, показываем модалку для заполнения результата
  const handleStatusChange = async (orderId: number, currentStatus: string) => {
    const nextStatus = statusFlow[currentStatus];
    if (nextStatus === currentStatus) return;

    if (nextStatus === 'completed') {
      setSelectedOrderId(orderId);
      setShowModal(true);
      return;
    }

    try {
      await orderApi.updateStatus(orderId, nextStatus);
      // Обновляем статус локально без перезагрузки страницы
      setOrders((prev) =>
        prev.map((order) =>
          order.id === orderId ? { ...order, status: nextStatus as Order['status'] } : order
        )
      );
    } catch (err) {
      setError('Ошибка при изменении статуса');
    }
  };

  // Завершает заявку: создаёт результат поверки и меняет статус на completed
  // Вызывается из модалки при нажатии "✅ Завершить"
  const handleCompleteWithResult = async () => {
    if (!selectedOrderId || !user) return;

    try {
      setSubmitting(true);

      // Создаём запись о результате поверки в таблице results
      await api.post('/results', {
        orderId: selectedOrderId,
        resultType,         // Тип: certificate, protocol, report
        metrologistId: user.id, // ID текущего метролога
      });

      // Меняем статус заявки на completed (отправит email клиенту)
      await orderApi.updateStatus(selectedOrderId, 'completed');

      // Обновляем статус локально без перезагрузки
      setOrders((prev) =>
        prev.map((order) =>
          order.id === selectedOrderId
            ? { ...order, status: 'completed' as Order['status'] }
            : order
        )
      );

      // Сбрасываем состояние модалки
      setShowModal(false);
      setSelectedOrderId(null);
      setResultType('certificate');
    } catch (err) {
      setError('Ошибка при завершении заявки');
    } finally {
      setSubmitting(false);
    }
  };

  // Скачивает PDF сертификат/протокол/отчёт для завершённой заявки
  const handleDownloadPdf = async (orderId: number, orderNumber: string) => {
    try {
      setDownloadingId(orderId);
      const response = await fetch(`http://localhost:8080/api/pdf/certificate/${orderId}`, {
        method: 'GET',
      });

      if (!response.ok) throw new Error('Ошибка загрузки PDF');

      // Создаём временную ссылку для скачивания файла
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `certificate_${orderNumber}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError('Не удалось скачать PDF');
    } finally {
      setDownloadingId(null);
    }
  };

  // Скачивает PDF договора для заявки
  // Если договор ещё не создан — создаёт его автоматически
  const handleDownloadContract = async (orderId: number, orderNumber: string) => {
    try {
      // POST создаст договор если его нет, или вернёт существующий
      await api.post(`/contracts/${orderId}`);

      const response = await fetch(`http://localhost:8080/api/contracts/${orderId}/download`);
      if (!response.ok) throw new Error('Ошибка загрузки');

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `contract_${orderNumber}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError('Ошибка при загрузке договора');
    }
  };

  if (isLoading) {
    return (
      <div className="queue-container">
        <div className="loading">Загрузка очереди...</div>
      </div>
    );
  }

  return (
    <div className="queue-container">
      <h1>Очередь заявок</h1>

      {error && <div className="error-message">{error}</div>}

      {orders.length === 0 ? (
        <div className="no-orders">Нет заявок в системе</div>
      ) : (
        <div className="queue-table">
          {/* Заголовок таблицы */}
          <div className="table-header">
            <div className="col col-number">№ Заявки</div>
            <div className="col col-client">Клиент ID</div>
            <div className="col col-status">Статус</div>
            <div className="col col-price">Стоимость</div>
            <div className="col col-action">Действие</div>
          </div>

          {orders.map((order) => (
            <div key={order.id} className="table-row">
              <div className="col col-number">
                <strong>#{order.orderNumber}</strong>
              </div>
              <div className="col col-client">{order.clientId}</div>
              <div className="col col-status">
                <span
                  className="status-badge"
                  style={{ backgroundColor: statusColors[order.status] }}
                >
                  {statusLabels[order.status] || order.status}
                </span>
              </div>
              <div className="col col-price">{order.totalPrice.toLocaleString()} ₸</div>
              <div className="col col-action">
                {/* Кнопка "Далее →" — скрыта для completed и awaiting_payment */}
                {order.status !== 'completed' && order.status !== 'awaiting_payment' && (
                  <button
                    className="btn-next-status"
                    onClick={() => handleStatusChange(order.id, order.status)}
                  >
                    Далее →
                  </button>
                )}

                {/* Для awaiting_payment показываем текст вместо кнопки */}
                {order.status === 'awaiting_payment' && (
                  <span style={{ color: '#eab308', fontSize: '13px' }}>Ожидает оплаты</span>
                )}

                {/* Кнопка договора — доступна для всех статусов кроме new */}
                {order.status !== 'new' && (
                  <button
                    className="btn-pdf"
                    style={{ background: '#1d4ed8', marginLeft: '8px' }}
                    onClick={() => handleDownloadContract(order.id, order.orderNumber)}
                  >
                    📝 Договор
                  </button>
                )}

                {/* Кнопка сертификата — только для завершённых заявок */}
                {order.status === 'completed' && (
                  <button
                    className="btn-pdf"
                    onClick={() => handleDownloadPdf(order.id, order.orderNumber)}
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

      {/* Модалка для завершения заявки — метролог выбирает тип документа */}
      {showModal && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.7)', display: 'flex',
          alignItems: 'center', justifyContent: 'center', zIndex: 1000
        }}>
          <div style={{
            background: '#1a1a1a', border: '1px solid #333',
            borderRadius: '8px', padding: '30px', width: '400px'
          }}>
            <h2 style={{ color: '#fff', marginBottom: '20px' }}>Завершить заявку</h2>

            <div className="form-group">
              <label style={{ color: '#b0b0b0', display: 'block', marginBottom: '8px' }}>
                Тип документа
              </label>
              {/* Выбор типа результата: сертификат, протокол или отчёт */}
              <select
                value={resultType}
                onChange={(e) => setResultType(e.target.value)}
                className="filter-select"
                style={{ width: '100%' }}
              >
                <option value="certificate">Сертификат</option>
                <option value="protocol">Протокол</option>
                <option value="report">Отчёт</option>
              </select>
            </div>

            <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
              <button
                onClick={handleCompleteWithResult}
                disabled={submitting}
                style={{ flex: 1, marginBottom: 0, background: '#10b981' }}
              >
                {submitting ? 'Сохранение...' : '✅ Завершить'}
              </button>
              <button
                onClick={() => { setShowModal(false); setSelectedOrderId(null); }}
                style={{ flex: 1, marginBottom: 0, background: '#333' }}
              >
                Отмена
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}