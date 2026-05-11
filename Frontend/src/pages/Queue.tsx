import { useState, useEffect } from 'react';
import { useAuthStore } from '../store/authStore';
import api, { orderApi } from '../services/api';
import type { Order } from '../types';
import { downloadCertificate, downloadContract } from '../utils/download';

// Страница очереди заявок для ролей metrolog и manager
// Показывает все заявки в системе и позволяет управлять их статусами
export default function Queue() {

  const { user } = useAuthStore();
  const [orders, setOrders] = useState<Order[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [downloadingId, setDownloadingId] = useState<number | null>(null);

  // Состояние модалки для завершения заявки
  const [showModal, setShowModal] = useState(false);
  const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);
  const [resultType, setResultType] = useState('certificate');
  const [submitting, setSubmitting] = useState(false);

  // Словарь для перевода статусов на русский язык
  const statusLabels: Record<string, string> = {
    pending_contract:  'Ожидает договора',
    awaiting_approval: 'На согласовании',
    awaiting_director: 'У директора',
    awaiting_payment:  'Ожидает оплаты',
    awaiting_delivery: 'Ожидает доставки',
    received_in_lab:   'Принято в лаб',
    in_work:           'В работе',
    under_review:      'На проверке',
    completed:         'Завершено',
    cancelled:         'Отменено',
    annulled:          'Аннулировано',
    terminated:        'Расторгнуто',
  };

  // Определяет следующий статус для каждого текущего
  const statusFlow: Record<string, string> = {
    awaiting_delivery: 'received_in_lab',
    received_in_lab:   'in_work',
    in_work:           'under_review',
    under_review:      'completed',
    completed:         'completed',
    cancelled:         'cancelled',
  };

  // Цвета бейджей для каждого статуса
  const statusColors: Record<string, { bg: string; text: string }> = {
    pending_contract:  { bg: 'bg-slate-100',  text: 'text-slate-600' },
    awaiting_approval: { bg: 'bg-blue-100',   text: 'text-blue-700' },
    awaiting_director: { bg: 'bg-indigo-100', text: 'text-indigo-700' },
    awaiting_payment:  { bg: 'bg-yellow-100', text: 'text-yellow-700' },
    awaiting_delivery: { bg: 'bg-amber-100',  text: 'text-amber-700' },
    received_in_lab:   { bg: 'bg-purple-100', text: 'text-purple-700' },
    in_work:           { bg: 'bg-pink-100',   text: 'text-pink-700' },
    under_review:      { bg: 'bg-orange-100', text: 'text-orange-700' },
    completed:         { bg: 'bg-green-100',  text: 'text-green-700' },
    cancelled:         { bg: 'bg-gray-100',   text: 'text-gray-500' },
    annulled:          { bg: 'bg-red-100',    text: 'text-red-600' },
    terminated:        { bg: 'bg-red-100',    text: 'text-red-600' },
  };

  // Загружаем все заявки при монтировании компонента
  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
  try {
    setIsLoading(true);
    // Метролог видит только заявки своей лаборатории
    // Менеджер видит все заявки
    const labId = user?.role === 'metrolog' ? user?.labId : undefined;
    const response = await orderApi.getAll(labId);
    setOrders(response.data);
  } catch {
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
      setOrders(prev =>
        prev.map(o => o.id === orderId ? { ...o, status: nextStatus as Order['status'] } : o)
      );
    } catch (err: any) {
      // Показываем сообщение с бэкенда если есть
      const msg = err.response?.data?.message || 'Ошибка при изменении статуса';
      setError(msg);
    }
  };

  // Завершает заявку: создаёт результат поверки и меняет статус на completed
  const handleCompleteWithResult = async () => {
    if (!selectedOrderId || !user) return;
    try {
      setSubmitting(true);

      // Создаём запись о результате поверки в таблице results
      await api.post('/results', {
        orderId: selectedOrderId,
        resultType,
        metrologistId: user.id,
      });

      // Меняем статус заявки на completed (отправит email клиенту)
      await orderApi.updateStatus(selectedOrderId, 'completed');

      // Обновляем статус локально без перезагрузки
      setOrders(prev =>
        prev.map(o => o.id === selectedOrderId ? { ...o, status: 'completed' as Order['status'] } : o)
      );

      // Сбрасываем состояние модалки
      setShowModal(false);
      setSelectedOrderId(null);
      setResultType('certificate');
    } catch {
      setError('Ошибка при завершении заявки');
    } finally {
      setSubmitting(false);
    }
  };

  const getStatusClass = (status: string) =>
    statusColors[status] || { bg: 'bg-gray-100', text: 'text-gray-500' };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex items-center gap-3 text-gray-400">
          <svg className="w-5 h-5 animate-spin" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
            <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
          </svg>
          Загрузка очереди...
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4">
      <div className="max-w-6xl mx-auto">

        {/* Заголовок */}
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-[#0A2E5C]" style={{ margin: 0, fontSize: '1.75rem' }}>
            Очередь заявок
          </h1>
          <p className="text-gray-500 text-sm mt-1" style={{ margin: '4px 0 0' }}>
            Управление заявками в системе
          </p>
        </div>

        {error && (
          <div className="flex items-center gap-3 p-4 bg-red-50 border border-red-200 rounded-xl mb-6 text-red-600 text-sm">
            <svg className="w-5 h-5 shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10"/><path d="M12 8v4m0 4h.01"/>
            </svg>
            {error}
          </div>
        )}

        {orders.length === 0 ? (
          <div className="bg-white border border-gray-100 rounded-2xl p-16 text-center shadow-sm">
            <svg className="w-12 h-12 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" strokeWidth={1.5} viewBox="0 0 24 24">
              <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1"/>
            </svg>
            <p className="text-gray-400">Нет заявок в системе</p>
          </div>
        ) : (
          <div className="bg-white border border-gray-100 rounded-2xl shadow-sm overflow-hidden">
            {/* Заголовок таблицы */}
            <div className="hidden md:grid grid-cols-[1fr_100px_160px_220px] gap-4 px-6 py-3 bg-gray-50 border-b border-gray-100">
              {['№ Заявки', 'Клиент', 'Статус', 'Действия'].map(col => (
                <div key={col} className="text-xs font-semibold text-[#00B2FF] uppercase tracking-wider">{col}</div>
              ))}
            </div>

            {/* Строки таблицы */}
            <div className="divide-y divide-gray-50">
              {orders.map(order => {
                const sc = getStatusClass(order.status);
                return (
                  <div key={order.id}
                    className="grid grid-cols-1 md:grid-cols-[1fr_100px_160px_220px] gap-4 px-6 py-4 hover:bg-gray-50/50 transition-colors items-center">

                    {/* № заявки */}
                    <div className="font-bold text-[#0A2E5C]">#{order.orderNumber}</div>

                    {/* Клиент */}
                    <div className="text-sm text-gray-500">ID: {order.clientId}</div>

                    {/* Статус */}
                    <div>
                      <span className={`text-xs font-semibold px-3 py-1 rounded-full ${sc.bg} ${sc.text}`}>
                        {statusLabels[order.status] || order.status}
                      </span>
                    </div>

                    {/* Действия */}
                    <div className="flex flex-wrap gap-2">
                      {/* Кнопка "Далее →" — скрыта для completed и awaiting_payment */}
                      {!['completed', 'awaiting_payment', 'cancelled', 'pending_contract',
                        'awaiting_approval', 'awaiting_director', 'annulled', 'terminated'].includes(order.status) && (
                        <button
                          onClick={() => handleStatusChange(order.id, order.status)}
                          className="px-3 py-1.5 bg-[#00B2FF] hover:bg-[#0095D9] text-white font-medium rounded-lg border-none cursor-pointer text-xs transition-colors flex items-center gap-1"
                          style={{ marginBottom: 0 }}
                        >
                          Далее
                          <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" strokeWidth={2.5} viewBox="0 0 24 24">
                            <path d="m9 18 6-6-6-6"/>
                          </svg>
                        </button>
                      )}

                      {/* Для awaiting_payment показываем текст вместо кнопки */}
                      {order.status === 'awaiting_payment' && (
                        <span className="text-xs text-yellow-600 font-medium px-2 py-1.5">Ожидает оплаты</span>
                      )}

                      {/* Кнопка договора — доступна для всех статусов */}
                      {(
                        <button
                          onClick={() => downloadContract(order.id, order.orderNumber, api, setError)}
                          className="px-3 py-1.5 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg border-none cursor-pointer text-xs transition-colors flex items-center gap-1"
                          style={{ marginBottom: 0 }}
                        >
                          <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                            <path d="M6 22a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h8a2.4 2.4 0 0 1 1.704.706l3.588 3.588A2.4 2.4 0 0 1 20 8v12a2 2 0 0 1-2 2z"/><path d="M14 2v5a1 1 0 0 0 1 1h5"/>
                          </svg>
                          Договор
                        </button>
                      )}

                      {/* Кнопка сертификата — только для завершённых заявок */}
                      {order.status === 'completed' && (
                        <button
                          onClick={() => downloadCertificate(order.id, order.orderNumber, setError, setDownloadingId)}
                          disabled={downloadingId === order.id}
                          className="px-3 py-1.5 bg-green-600 hover:bg-green-700 text-white font-medium rounded-lg border-none cursor-pointer text-xs transition-colors flex items-center gap-1"
                          style={{ marginBottom: 0 }}
                        >
                          <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
                          </svg>
                          {downloadingId === order.id ? 'Загрузка...' : 'Сертификат'}
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>

      {/* Модалка для завершения заявки — метролог выбирает тип документа */}
      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 px-4">
          <div className="bg-white rounded-2xl shadow-2xl p-8 w-full max-w-md">
            <h2 className="font-bold text-[#0A2E5C] mb-6" style={{ margin: '0 0 24px', fontSize: '1.25rem' }}>
              Завершить заявку
            </h2>

            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-1.5">
                Тип документа
              </label>
              {/* Выбор типа результата: сертификат, протокол или отчёт */}
              <select
                value={resultType}
                onChange={e => setResultType(e.target.value)}
                className="w-full px-4 py-3 border border-gray-200 rounded-xl text-gray-900 text-sm outline-none focus:border-[#00B2FF] focus:ring-2 focus:ring-[#00B2FF]/10 transition-all cursor-pointer"
                style={{ fontFamily: 'inherit', marginBottom: 0 }}
              >
                <option value="certificate">Сертификат</option>
                <option value="protocol">Протокол</option>
                <option value="report">Отчёт</option>
              </select>
            </div>

            <div className="flex gap-3">
              <button
                onClick={handleCompleteWithResult}
                disabled={submitting}
                className="flex-1 py-3 bg-green-600 hover:bg-green-700 text-white font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors flex items-center justify-center gap-2"
                style={{ marginBottom: 0 }}
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2.5} viewBox="0 0 24 24">
                  <path d="m9 11 3 3L22 4"/>
                </svg>
                {submitting ? 'Сохранение...' : 'Завершить'}
              </button>
              <button
                onClick={() => { setShowModal(false); setSelectedOrderId(null); }}
                className="flex-1 py-3 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors"
                style={{ marginBottom: 0 }}
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