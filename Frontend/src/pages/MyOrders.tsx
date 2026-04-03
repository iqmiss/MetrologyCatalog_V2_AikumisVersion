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
  const [error, setError] = useState('');
  const [filterStatus, setFilterStatus] = useState('');
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
  const statusColors: Record<string, { bg: string; text: string }> = {
    new: { bg: 'bg-blue-100', text: 'text-blue-700' },
    awaiting_payment: { bg: 'bg-yellow-100', text: 'text-yellow-700' },
    awaiting_delivery: { bg: 'bg-amber-100', text: 'text-amber-700' },
    received_in_lab: { bg: 'bg-purple-100', text: 'text-purple-700' },
    in_work: { bg: 'bg-pink-100', text: 'text-pink-700' },
    under_review: { bg: 'bg-orange-100', text: 'text-orange-700' },
    completed: { bg: 'bg-green-100', text: 'text-green-700' },
    cancelled: { bg: 'bg-gray-100', text: 'text-gray-500' },
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
      setOrders(prev =>
        prev.map(o => o.id === orderId ? { ...o, status: 'awaiting_delivery' as Order['status'] } : o)
      );
    } catch {
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
    } catch {
      setError('Ошибка при создании договора');
    }
  };

  // Имитация подписания договора через ЭЦП
  const handleSignContract = async (orderId: number) => {
    if (!window.confirm('Подписать договор ЭЦП?')) return;
    try {
      await api.put(`/contracts/${orderId}/sign`, { userId: user?.id });
      // Обновляем статус договора локально
      setContractStatus(prev => ({ ...prev, [orderId]: 'signed' }));
      alert('Договор подписан ЭЦП ✓');
    } catch {
      setError('Ошибка при подписании');
    }
  };

  // Фильтруем заявки по выбранному статусу
  const filteredOrders = filterStatus
    ? orders.filter(o => o.status === filterStatus)
    : orders;

  // Форматируем дату в читаемый вид для русской локали
  const formatDate = (dateString?: string) =>
    dateString ? new Date(dateString).toLocaleDateString('ru-RU') : 'Не указана';

  const getStatusClass = (status: string) =>
    statusColors[status] || { bg: 'bg-gray-100', text: 'text-gray-500' };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex items-center gap-3 text-gray-400">
          <svg className="w-5 h-5 animate-spin" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
            <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
          </svg>
          Загрузка заявок...
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4">
      <div className="max-w-5xl mx-auto">

        {/* Заголовок */}
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-[#0A2E5C]" style={{ margin: 0, fontSize: '1.75rem' }}>
            Мои заявки
          </h1>
          <p className="text-gray-500 text-sm mt-1" style={{ margin: '4px 0 0' }}>
            Управляйте своими заявками на метрологические услуги
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

        {/* Шапка со статистикой и кнопкой создания заявки */}
        <div className="flex flex-wrap gap-4 mb-6 items-start">
          <div className="flex gap-4 flex-1 flex-wrap">
            {[
              { label: 'Всего заявок', value: orders.length },
              { label: 'В процессе', value: orders.filter(o => o.status !== 'completed' && o.status !== 'cancelled').length },
              { label: 'Завершено', value: orders.filter(o => o.status === 'completed').length },
            ].map(stat => (
              <div key={stat.label} className="bg-white border border-gray-100 rounded-xl px-6 py-4 shadow-sm text-center min-w-[120px]">
                <p className="text-xs text-gray-400 mb-1" style={{ margin: '0 0 4px' }}>{stat.label}</p>
                <p className="text-2xl font-bold text-[#0A2E5C]" style={{ margin: 0 }}>{stat.value}</p>
              </div>
            ))}
          </div>
          <button
            onClick={() => navigate('/create-order')}
            className="flex items-center gap-2 px-5 py-3 bg-[#00B2FF] hover:bg-[#0095D9] text-white font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors"
            style={{ marginBottom: 0 }}
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10"/><path d="M12 8v8M8 12h8"/>
            </svg>
            Новая заявка
          </button>
        </div>

        {/* Фильтр по статусу */}
        <div className="mb-6">
          <select
            value={filterStatus}
            onChange={e => setFilterStatus(e.target.value)}
            className="px-4 py-3 bg-white border border-gray-200 rounded-xl text-sm text-gray-900 outline-none focus:border-[#00B2FF] focus:ring-2 focus:ring-[#00B2FF]/10 transition-all cursor-pointer"
            style={{ fontFamily: 'inherit', marginBottom: 0 }}
          >
            <option value="">Все статусы</option>
            {Object.entries(statusLabels).map(([key, label]) => (
              <option key={key} value={key}>{label}</option>
            ))}
          </select>
        </div>

        {filteredOrders.length === 0 ? (
          <div className="bg-white border border-gray-100 rounded-2xl p-16 text-center shadow-sm">
            <svg className="w-12 h-12 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" strokeWidth={1.5} viewBox="0 0 24 24">
              <path d="M11 21.73a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73z"/>
            </svg>
            <p className="text-gray-400 mb-4" style={{ margin: '0 0 16px' }}>Нет заявок</p>
            <button
              onClick={() => navigate('/create-order')}
              className="px-6 py-3 bg-[#00B2FF] hover:bg-[#0095D9] text-white font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors"
              style={{ marginBottom: 0 }}
            >
              Создать первую заявку
            </button>
          </div>
        ) : (
          <div className="flex flex-col gap-4">
            {filteredOrders.map(order => {
              const sc = getStatusClass(order.status);
              return (
                <div key={order.id}
                  className="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm hover:shadow-md hover:border-[#00B2FF]/30 transition-all">

                  {/* Заголовок карточки: номер заявки и статус */}
                  <div className="flex items-center justify-between mb-4 flex-wrap gap-3">
                    <div className="flex items-center gap-3">
                      <h3 className="font-bold text-[#0A2E5C]" style={{ margin: 0, fontSize: '1rem' }}>
                        Заявка #{order.orderNumber}
                      </h3>
                      <span className={`text-xs font-semibold px-3 py-1 rounded-full ${sc.bg} ${sc.text}`}>
                        {statusLabels[order.status] || order.status}
                      </span>
                    </div>
                  </div>

                  {/* Детали заявки */}
                  <div className="bg-gray-50 rounded-xl p-4 mb-4 grid grid-cols-2 md:grid-cols-4 gap-3">
                    <div>
                      <p className="text-xs text-gray-400 mb-0.5" style={{ margin: '0 0 2px' }}>Стоимость</p>
                      <p className="text-sm font-bold text-[#0A2E5C]" style={{ margin: 0 }}>{order.totalPrice.toLocaleString()} ₸</p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-400 mb-0.5" style={{ margin: '0 0 2px' }}>Плановая дата</p>
                      <p className="text-sm font-semibold text-gray-700" style={{ margin: 0 }}>{formatDate(order.dueDate)}</p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-400 mb-0.5" style={{ margin: '0 0 2px' }}>Лаборатория</p>
                      <p className="text-sm font-semibold text-gray-700" style={{ margin: 0 }}>#{order.labId}</p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-400 mb-0.5" style={{ margin: '0 0 2px' }}>Статус</p>
                      <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${sc.bg} ${sc.text}`}>
                        {statusLabels[order.status]}
                      </span>
                    </div>
                  </div>

                  {/* Кнопки действий — зависят от текущего статуса заявки */}
                  <div className="flex flex-wrap gap-2">
                    {/* Отмена — только для новых заявок */}
                    {(order.status === 'new' || order.status === 'awaiting_payment') && (
                      <button
                        onClick={async () => {
                          if (!window.confirm('Отменить заявку?')) return;
                          try {
                            await orderApi.updateStatus(order.id, 'cancelled');
                            setOrders(prev =>
                              prev.map(o => o.id === order.id ? { ...o, status: 'cancelled' as Order['status'] } : o)
                            );
                          } catch {
                            setError('Ошибка при отмене заявки');
                          }
                        }}
                        className="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-600 font-medium rounded-lg border-none cursor-pointer text-sm transition-colors"
                        style={{ marginBottom: 0 }}
                      >
                        Отменить
                      </button>
                    )}

                    {/* Оплата — только для заявок в статусе awaiting_payment */}
                    {order.status === 'awaiting_payment' && (
                      <button
                        onClick={() => handlePay(order.id)}
                        disabled={payingId === order.id}
                        className="px-4 py-2 bg-yellow-500 hover:bg-yellow-600 text-white font-medium rounded-lg border-none cursor-pointer text-sm transition-colors flex items-center gap-2"
                        style={{ marginBottom: 0 }}
                      >
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                          <rect width="20" height="14" x="2" y="5" rx="2"/><line x1="2" x2="22" y1="10" y2="10"/>
                        </svg>
                        {payingId === order.id ? 'Оплата...' : 'Оплатить'}
                      </button>
                    )}

                    {/* Договор и ЭЦП — для всех статусов кроме new */}
                    {order.status !== 'new' && (
                      <>
                        <button
                          onClick={() => handleContract(order.id)}
                          className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg border-none cursor-pointer text-sm transition-colors flex items-center gap-2"
                          style={{ marginBottom: 0 }}
                        >
                          <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                            <path d="M6 22a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h8a2.4 2.4 0 0 1 1.704.706l3.588 3.588A2.4 2.4 0 0 1 20 8v12a2 2 0 0 1-2 2z"/><path d="M14 2v5a1 1 0 0 0 1 1h5"/>
                          </svg>
                          Договор
                        </button>

                        {/* Кнопка подписания — появляется после скачивания договора */}
                        {contractStatus[order.id] === 'exists' && (
                          <button
                            onClick={() => handleSignContract(order.id)}
                            className="px-4 py-2 bg-emerald-700 hover:bg-emerald-800 text-white font-medium rounded-lg border-none cursor-pointer text-sm transition-colors flex items-center gap-2"
                            style={{ marginBottom: 0 }}
                          >
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                            </svg>
                            Подписать ЭЦП
                          </button>
                        )}

                        {/* Отметка о подписании */}
                        {contractStatus[order.id] === 'signed' && (
                          <span className="flex items-center gap-1 text-sm text-emerald-600 font-medium px-2">
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2.5} viewBox="0 0 24 24">
                              <path d="m9 11 3 3L22 4"/>
                            </svg>
                            Подписано
                          </span>
                        )}
                      </>
                    )}

                    {/* Сертификат — только для завершённых заявок */}
                    {order.status === 'completed' && (
                      <button
                        onClick={() => downloadCertificate(order.id, order.orderNumber, setError, setDownloadingId)}
                        disabled={downloadingId === order.id}
                        className="px-4 py-2 bg-[#00B2FF] hover:bg-[#0095D9] text-white font-medium rounded-lg border-none cursor-pointer text-sm transition-colors flex items-center gap-2"
                        style={{ marginBottom: 0 }}
                      >
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
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
        )}
      </div>
    </div>
  );
}