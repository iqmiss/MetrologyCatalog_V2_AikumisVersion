import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { orderApi, contractApi, serviceApi } from '../services/api';
import api from '../services/api';
import type { Order, Service } from '../types';
import { downloadCertificate } from '../utils/download';

export default function Orders() {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [orders, setOrders] = useState<Order[]>([]);
  const [services, setServices] = useState<Service[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [filterStatus, setFilterStatus] = useState('');
  const [downloadingId, setDownloadingId] = useState<number | null>(null);

  // Состояние данных договоров для каждой заявки
  const [contractData, setContractData] = useState<Record<number, {
    clientSigned: boolean;
    directorSigned: boolean;
    status: string;
    downloaded: boolean;
  }>>({});

  // Состояние модалки редактирования заявки (только для менеджера)
  const [editOrder, setEditOrder] = useState<Order | null>(null);
  const [editForm, setEditForm] = useState({
    serviceId: '',
    labId: '',
    totalPrice: '',
    dueDate: '',
    clientComment: '',
  });
  const [editLoading, setEditLoading] = useState(false);

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

  // Загружаем статус договоров для всех заявок после загрузки заявок
  useEffect(() => {
    if (orders.length === 0) return;
    orders.forEach(order => {
      contractApi.getByOrderId(order.id)
        .then(res => {
          setContractData(prev => ({
            ...prev,
            [order.id]: {
              clientSigned: res.data.clientSigned,
              directorSigned: res.data.directorSigned,
              status: res.data.status,
              downloaded: prev[order.id]?.downloaded || false,
            }
          }));
        })
        .catch(() => {});
    });
  }, [orders]);

  // Загружаем заявки и услуги при монтировании
  const fetchOrders = async () => {
    try {
      setIsLoading(true);
      // Менеджер видит все заявки, клиент только свои
      const response = user?.role === 'manager'
        ? await orderApi.getAll()
        : await orderApi.getMyOrders(user?.id || 0);
      setOrders(response.data);
    } catch {
      setError('Ошибка при загрузке заявок');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
    // Загружаем услуги для менеджера — нужны в модалке редактирования
    if (user?.role === 'manager') {
      serviceApi.getAll().then(res => setServices(res.data)).catch(() => {});
    }
  }, []);

  // Скачивает PDF договора
  const handleContract = async (orderId: number) => {
    try {
      const token = localStorage.getItem('token');
      const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8081/api';
      const response = await fetch(`${baseUrl}/contracts/${orderId}/download`, {
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
      // Отмечаем что договор скачан — нужно для показа кнопки подписания клиенту
      setContractData(prev => ({
        ...prev,
        [orderId]: { ...prev[orderId], downloaded: true }
      }));
    } catch {
      setError('Ошибка при скачивании договора');
    }
  };

  // Менеджер создаёт договор для заявки → заявка переходит в awaiting_approval
  const handleCreateContract = async (orderId: number) => {
    try {
      await contractApi.create(orderId);
      const order = orders.find(o => o.id === orderId);
      if (order?.status === 'pending_contract') {
        await orderApi.updateStatus(orderId, 'awaiting_approval');
      }
      await fetchOrders();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка при создании договора');
    }
  };

  // Менеджер отправляет договор на согласование согласующим
  const handleSubmit = async (orderId: number) => {
    try {
      await contractApi.submit(orderId);
      setContractData(prev => ({
        ...prev,
        [orderId]: { ...prev[orderId], status: 'pending_approval' }
      }));
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка при отправке на согласование');
    }
  };

  // Клиент подписывает договор ЭЦП
  const handleClientSign = async (orderId: number) => {
    if (!window.confirm('Подписать договор ЭЦП?')) return;
    try {
      await contractApi.signByClient(orderId, user?.id || 0);
      setContractData(prev => ({
        ...prev,
        [orderId]: { ...prev[orderId], clientSigned: true }
      }));
    } catch {
      setError('Ошибка при подписании');
    }
  };

  // Открывает модалку редактирования — заполняет форму текущими данными заявки
  const handleOpenEdit = (order: Order) => {
    setEditOrder(order);
    setEditForm({
      serviceId: order.serviceId.toString(),
      labId: order.labId.toString(),
      totalPrice: order.totalPrice.toString(),
      dueDate: order.dueDate || '',
      clientComment: (order as any).clientComment || '',
    });
  };

  // Сохраняет изменения заявки через PUT /api/orders/{id}
  const handleSaveEdit = async () => {
    if (!editOrder) return;
    try {
      setEditLoading(true);
      await orderApi.update(editOrder.id, {
        serviceId: parseInt(editForm.serviceId),
        labId: parseInt(editForm.labId),
        totalPrice: parseFloat(editForm.totalPrice),
        dueDate: editForm.dueDate,
        clientComment: editForm.clientComment,
      });
      setEditOrder(null);
      await fetchOrders();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка при редактировании');
    } finally {
      setEditLoading(false);
    }
  };

  const filteredOrders = filterStatus
    ? orders.filter(o => o.status === filterStatus)
    : orders;

  const formatDate = (dateString?: string) =>
    dateString ? new Date(dateString).toLocaleDateString('ru-RU') : 'Не указана';

  const getStatusClass = (status: string) =>
    statusColors[status] || { bg: 'bg-gray-100', text: 'text-gray-500' };

  const inputClass = "w-full px-4 py-3 border border-gray-200 rounded-xl text-gray-900 text-sm outline-none focus:border-[#00B2FF] focus:ring-2 focus:ring-[#00B2FF]/10 transition-all";
  const selectClass = "w-full px-4 py-3 border border-gray-200 rounded-xl text-gray-900 text-sm outline-none focus:border-[#00B2FF] focus:ring-2 focus:ring-[#00B2FF]/10 transition-all cursor-pointer";

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

        <div className="mb-8">
          <h1 className="text-2xl font-bold text-[#0A2E5C]" style={{ margin: 0, fontSize: '1.75rem' }}>
            {user?.role === 'manager' ? 'Все заявки' : 'Мои заявки'}
          </h1>
          <p className="text-gray-500 text-sm mt-1" style={{ margin: '4px 0 0' }}>
            Управляйте заявками на метрологические услуги
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

        {/* Статистика и кнопка создания */}
        <div className="flex flex-wrap gap-4 mb-6 items-start">
          <div className="flex gap-4 flex-1 flex-wrap">
            {[
              { label: 'Всего', value: orders.length },
              { label: 'В процессе', value: orders.filter(o => !['completed','cancelled','annulled','terminated'].includes(o.status)).length },
              { label: 'Завершено', value: orders.filter(o => o.status === 'completed').length },
            ].map(stat => (
              <div key={stat.label} className="bg-white border border-gray-100 rounded-xl px-6 py-4 shadow-sm text-center min-w-[120px]">
                <p className="text-xs text-gray-400 mb-1" style={{ margin: '0 0 4px' }}>{stat.label}</p>
                <p className="text-2xl font-bold text-[#0A2E5C]" style={{ margin: 0 }}>{stat.value}</p>
              </div>
            ))}
          </div>
          {/* Кнопка новой заявки — для клиента и менеджера */}
          {(user?.role === 'client' || user?.role === 'manager') && (
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
          )}
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
            {user?.role === 'client' && (
              <button
                onClick={() => navigate('/create-order')}
                className="px-6 py-3 bg-[#00B2FF] hover:bg-[#0095D9] text-white font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors"
                style={{ marginBottom: 0 }}
              >
                Создать первую заявку
              </button>
            )}
          </div>
        ) : (
          <div className="flex flex-col gap-4">
            {filteredOrders.map(order => {
              const sc = getStatusClass(order.status);
              const cd = contractData[order.id];
              return (
                <div key={order.id}
                  className="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm hover:shadow-md hover:border-[#00B2FF]/30 transition-all">

                  {/* Заголовок карточки */}
                  <div className="flex items-center justify-between mb-4 flex-wrap gap-3">
                    <div className="flex items-center gap-3">
                      <h3 className="font-bold text-[#0A2E5C]" style={{ margin: 0, fontSize: '1rem' }}>
                        Заявка #{order.orderNumber}
                      </h3>
                      <span className={`text-xs font-semibold px-3 py-1 rounded-full ${sc.bg} ${sc.text}`}>
                        {statusLabels[order.status] || order.status}
                      </span>
                    </div>
                    {/* Кнопка редактирования — только для менеджера */}
                    {user?.role === 'manager' && (
                      <button
                        onClick={() => handleOpenEdit(order)}
                        className="flex items-center gap-1 px-3 py-1.5 bg-gray-100 hover:bg-gray-200 text-gray-600 font-medium rounded-lg border-none cursor-pointer text-xs transition-colors"
                        style={{ marginBottom: 0 }}
                      >
                        <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                        </svg>
                        Редактировать
                      </button>
                    )}
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
                      <p className="text-xs text-gray-400 mb-0.5" style={{ margin: '0 0 2px' }}>Договор</p>
                      <span className="text-xs font-semibold text-gray-600">
                        {cd ? (
                          cd.directorSigned && cd.clientSigned ? 'Подписан' :
                          cd.status === 'pending_approval' ? 'На согласовании' :
                          cd.status === 'approved' ? 'У директора' : 'Черновик'
                        ) : '—'}
                      </span>
                    </div>
                  </div>

                  {/* Кнопки действий */}
                  <div className="flex flex-wrap gap-2">

                    {/* Отмена — клиент может отменить на ранних статусах */}
                    {user?.role === 'client' && ['pending_contract', 'awaiting_payment'].includes(order.status) && (
                      <button
                        onClick={async () => {
                          if (!window.confirm('Отменить заявку?')) return;
                          try {
                            await orderApi.updateStatus(order.id, 'cancelled');
                            setOrders(prev => prev.map(o => o.id === order.id ? { ...o, status: 'cancelled' as Order['status'] } : o));
                          } catch {
                            setError('Ошибка при отмене');
                          }
                        }}
                        className="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-600 font-medium rounded-lg border-none cursor-pointer text-sm transition-colors"
                        style={{ marginBottom: 0 }}
                      >
                        Отменить
                      </button>
                    )}

                    {/* Менеджер создаёт договор — только для pending_contract */}
                    {user?.role === 'manager' && order.status === 'pending_contract' && (
                      <button
                        onClick={() => handleCreateContract(order.id)}
                        className="px-4 py-2 bg-[#0A2E5C] hover:bg-[#0d3a73] text-white font-medium rounded-lg border-none cursor-pointer text-sm transition-colors flex items-center gap-2"
                        style={{ marginBottom: 0 }}
                      >
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                          <circle cx="12" cy="12" r="10"/><path d="M12 8v8M8 12h8"/>
                        </svg>
                        Создать договор
                      </button>
                    )}

                    {/* Менеджер отправляет на согласование — если договор ещё черновик */}
                    {user?.role === 'manager' && order.status === 'awaiting_approval' && cd?.status === 'draft' && (
                      <button
                        onClick={() => handleSubmit(order.id)}
                        className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg border-none cursor-pointer text-sm transition-colors"
                        style={{ marginBottom: 0 }}
                      >
                        Отправить на согласование
                      </button>
                    )}

                    {/* Скачать договор — для всех статусов кроме pending_contract */}
                    {order.status !== 'pending_contract' && (
                      <button
                        onClick={() => handleContract(order.id)}
                        className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg border-none cursor-pointer text-sm transition-colors flex items-center gap-2"
                        style={{ marginBottom: 0 }}
                      >
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                          <path d="M6 22a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h8a2.4 2.4 0 0 1 1.704.706l3.588 3.588A2.4 2.4 0 0 1 20 8v12a2 2 0 0 1-2 2z"/>
                          <path d="M14 2v5a1 1 0 0 0 1 1h5"/>
                        </svg>
                        Договор
                      </button>
                    )}

                    {/* Клиент подписывает ЭЦП — после скачивания договора */}
                    {user?.role === 'client' && order.status === 'awaiting_payment' && !cd?.clientSigned && cd?.downloaded && (
                      <button
                        onClick={() => handleClientSign(order.id)}
                        className="px-4 py-2 bg-emerald-700 hover:bg-emerald-800 text-white font-medium rounded-lg border-none cursor-pointer text-sm transition-colors flex items-center gap-2"
                        style={{ marginBottom: 0 }}
                      >
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                        </svg>
                        Подписать ЭЦП
                      </button>
                    )}

                    {/* Индикатор подписи клиента */}
                    {user?.role === 'client' && cd?.clientSigned && (
                      <span className="flex items-center gap-1 text-sm text-emerald-600 font-medium px-2">
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2.5} viewBox="0 0 24 24">
                          <path d="m9 11 3 3L22 4"/>
                        </svg>
                        Вы подписали
                      </span>
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

      {/* Модалка редактирования заявки — только для менеджера */}
      {editOrder && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 px-4">
          <div className="bg-white rounded-2xl shadow-2xl p-8 w-full max-w-lg">
            <h2 className="font-bold text-[#0A2E5C] mb-6" style={{ margin: '0 0 24px', fontSize: '1.25rem' }}>
              Редактировать заявку #{editOrder.orderNumber}
            </h2>

            <div className="flex flex-col gap-4">

              {/* Выбор услуги */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Услуга</label>
                <select
                  value={editForm.serviceId}
                  onChange={e => setEditForm(prev => ({ ...prev, serviceId: e.target.value }))}
                  className={selectClass}
                  style={{ fontFamily: 'inherit', marginBottom: 0 }}
                >
                  <option value="">— Выберите услугу —</option>
                  {services.map(s => (
                    <option key={s.id} value={s.id}>
                      {s.name} — {s.price.toLocaleString()} ₸
                    </option>
                  ))}
                </select>
              </div>

              {/* Стоимость */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Стоимость (₸)</label>
                <input
                  type="number"
                  value={editForm.totalPrice}
                  onChange={e => setEditForm(prev => ({ ...prev, totalPrice: e.target.value }))}
                  className={inputClass}
                  style={{ fontFamily: 'inherit', marginBottom: 0 }}
                />
              </div>

              {/* Дата */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Плановая дата сдачи</label>
                <input
                  type="date"
                  value={editForm.dueDate}
                  onChange={e => setEditForm(prev => ({ ...prev, dueDate: e.target.value }))}
                  className={inputClass}
                  style={{ fontFamily: 'inherit', marginBottom: 0 }}
                />
              </div>

              {/* Комментарий */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Комментарий</label>
                <textarea
                  value={editForm.clientComment}
                  onChange={e => setEditForm(prev => ({ ...prev, clientComment: e.target.value }))}
                  rows={3}
                  className="w-full px-4 py-3 border border-gray-200 rounded-xl text-gray-900 text-sm outline-none focus:border-[#00B2FF] focus:ring-2 focus:ring-[#00B2FF]/10 transition-all resize-none"
                  style={{ fontFamily: 'inherit', marginBottom: 0 }}
                />
              </div>
            </div>

            <div className="flex gap-3 mt-6">
              <button
                onClick={handleSaveEdit}
                disabled={editLoading}
                className="flex-1 py-3 bg-[#00B2FF] hover:bg-[#0095D9] text-white font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors"
                style={{ marginBottom: 0 }}
              >
                {editLoading ? 'Сохранение...' : 'Сохранить'}
              </button>
              <button
                onClick={() => setEditOrder(null)}
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