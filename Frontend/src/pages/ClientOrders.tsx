import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { orderApi, contractApi, serviceApi, subserviceApi } from '../services/api';
import type { Order, Service, Subservice, Contract } from '../types';

const statusLabels: Record<string, { label: string; description: string; color: string }> = {
  pending_contract: {
    label: 'Заявление на проверке',
    description: 'Ваше заявление получено и находится на проверке у бухгалтера. Будьте на связи.',
    color: 'blue',
  },
  revision: {
    label: 'Требуется доработка',
    description: 'Бухгалтер вернул заявление на доработку. Ознакомьтесь с комментарием.',
    color: 'orange',
  },
  awaiting_approval: {
    label: 'На стадии подписания с нашей стороны',
    description: 'Договор проходит внутреннее согласование.',
    color: 'blue',
  },
  awaiting_payment: {
    label: 'Ожидание оплаты',
    description: 'Договор подписан. Ожидается оплата.',
    color: 'yellow',
  },
  pending_delivery: {
    label: 'Оплата получена',
    description: 'Оплата подтверждена. Заявка передаётся на исполнение.',
    color: 'green',
  },
  awaiting_delivery: {
    label: 'Направляется в лабораторию',
    description: 'Заявка направляется в лабораторию.',
    color: 'purple',
  },
  received_in_lab: {
    label: 'Принято в лабораторию',
    description: 'Ваша заявка принята в лабораторию и ожидает начала исполнения.',
    color: 'purple',
  },
  in_work: {
    label: 'В работе',
    description: 'Специалисты лаборатории выполняют услугу.',
    color: 'pink',
  },
  under_review: {
    label: 'На проверке результатов',
    description: 'Результаты проверяются.',
    color: 'orange',
  },
  completed: {
    label: 'Выполнено',
    description: 'Услуга выполнена. Документы доступны для скачивания.',
    color: 'green',
  },
  cancelled: {
    label: 'Отменено',
    description: 'Заявка отменена.',
    color: 'gray',
  },
  annulled: {
    label: 'Аннулировано',
    description: 'Договор аннулирован.',
    color: 'red',
  },
  terminated: {
    label: 'Расторгнуто',
    description: 'Договор расторгнут.',
    color: 'red',
  },
};

const colorMap: Record<string, { bg: string; text: string; border: string }> = {
  blue:   { bg: 'bg-blue-50',   text: 'text-blue-700',   border: 'border-blue-200' },
  orange: { bg: 'bg-orange-50', text: 'text-orange-700', border: 'border-orange-200' },
  yellow: { bg: 'bg-yellow-50', text: 'text-yellow-700', border: 'border-yellow-200' },
  green:  { bg: 'bg-green-50',  text: 'text-green-700',  border: 'border-green-200' },
  purple: { bg: 'bg-purple-50', text: 'text-purple-700', border: 'border-purple-200' },
  pink:   { bg: 'bg-pink-50',   text: 'text-pink-700',   border: 'border-pink-200' },
  gray:   { bg: 'bg-gray-100',  text: 'text-gray-500',   border: 'border-gray-200' },
  red:    { bg: 'bg-red-50',    text: 'text-red-600',    border: 'border-red-200' },
};

const fieldLabels: Record<string, string> = {
  si_name: 'Наименование СИ',
  characteristics: 'Метрологические характеристики',
  producer: 'Производитель',
  has_software: 'Наличие ПО',
  date_place: 'Дата и место',
  serial_numbers: 'Заводские номера',
  modified_characteristics: 'Изменённые характеристики',
  quantity: 'Количество',
  org_name: 'Наименование организации',
  director_name: 'Руководитель',
  requisites: 'Реквизиты',
  legal_address: 'Юридический адрес',
  mailing_address: 'Адрес рассылки',
  program_type: 'Программа МЛС',
  si_description: 'Описание СИ',
  contact_name: 'Контактное лицо',
  contact_position: 'Должность',
  contact_phone: 'Телефон',
  contact_email: 'Email',
  needs_contract: 'Форма договора',
  applicant_fullname: 'ФИО заявителя',
  applicant_email: 'Email заявителя',
  applicant_phone: 'Телефон заявителя',
  applicant_iin: 'ИИН заявителя',
  company_name: 'Организация',
  company_bin: 'БИН',
  company_director_name: 'Руководитель',
  company_director_position: 'Должность',
  company_iik: 'ИИК',
  company_bank_name: 'Банк',
  company_bik: 'БИК',
  company_kbe: 'КБЕ',
  company_legal_address: 'Юр. адрес',
  company_address: 'Факт. адрес',
  company_phone: 'Телефон орг.',
};

export default function ClientOrders() {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [orders, setOrders] = useState<Order[]>([]);
  const [services, setServices] = useState<Service[]>([]);
  const [subservices, setSubservices] = useState<Record<number, Subservice>>({});
  const [contracts, setContracts] = useState<Record<number, Contract>>({});
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [expandedOrderId, setExpandedOrderId] = useState<number | null>(null);
  const [orderFields, setOrderFields] = useState<Record<number, any[]>>({});

  useEffect(() => { fetchAll(); }, []);

  const fetchAll = async () => {
    try {
      setIsLoading(true);
      const [ordersRes, servicesRes] = await Promise.all([
        orderApi.getMyOrders(user?.id || 0),
        serviceApi.getAll(),
      ]);
      setOrders([...ordersRes.data].reverse());
      setServices(servicesRes.data);

      ordersRes.data.forEach((order: Order) => {
        contractApi.getByOrderId(order.id)
          .then(res => setContracts(prev => ({ ...prev, [order.id]: res.data })))
          .catch(() => {});

        if (order.subserviceId) {
          subserviceApi.getByServiceId(order.serviceId)
            .then(res => {
              const found = res.data.find((s: any) => s.id === order.subserviceId);
              if (found) setSubservices(prev => ({ ...prev, [order.subserviceId!]: found }));
            })
            .catch(() => {});
        }
      });
    } catch {
      setError('Ошибка при загрузке заявок');
    } finally {
      setIsLoading(false);
    }
  };

  const loadOrderFields = async (orderId: number) => {
    if (expandedOrderId === orderId) {
      setExpandedOrderId(null);
      return;
    }
    if (orderFields[orderId]) {
      setExpandedOrderId(orderId);
      return;
    }
    try {
      const res = await orderApi.getFields(orderId);
      setOrderFields(prev => ({ ...prev, [orderId]: res.data }));
      setExpandedOrderId(orderId);
    } catch {}
  };

  const handleCancel = async (orderId: number) => {
    if (!window.confirm('Отменить заявку?')) return;
    try {
      await orderApi.updateStatus(orderId, 'cancelled');
      setOrders(prev => prev.map(o =>
        o.id === orderId ? { ...o, status: 'cancelled' as Order['status'] } : o
      ));
    } catch {
      setError('Ошибка при отмене');
    }
  };

  const handleSign = async (orderId: number) => {
    if (!window.confirm('Подписать договор?')) return;
    try {
      await contractApi.signByClient(orderId, user?.id || 0);
      setContracts(prev => ({
        ...prev,
        [orderId]: { ...prev[orderId], clientSigned: true }
      }));
    } catch {
      setError('Ошибка при подписании');
    }
  };

  const handleDownloadContract = async (orderId: number) => {
    try {
      const token = localStorage.getItem('token');
      const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8081/api';
      const response = await fetch(`${baseUrl}/contracts/${orderId}/download`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (!response.ok) throw new Error();
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `contract_${orderId}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    } catch {
      setError('Ошибка при скачивании договора');
    }
  };

  const getServiceName = (serviceId: number) =>
    services.find(s => s.id === serviceId)?.name || `Услуга #${serviceId}`;

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
      <div className="max-w-3xl mx-auto">

        <div className="mb-8 flex items-center justify-between flex-wrap gap-4">
          <div>
            <h1 className="text-2xl font-bold text-[#0A2E5C]" style={{ margin: 0, fontSize: '1.75rem' }}>
              Мои заявления
            </h1>
            <p className="text-gray-500 text-sm mt-1" style={{ margin: '4px 0 0' }}>
              Статус ваших заявлений на метрологические услуги
            </p>
          </div>
          <button onClick={() => navigate('/catalog')}
            className="flex items-center gap-2 px-5 py-3 bg-[#00B2FF] hover:bg-[#0095D9] text-white font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors"
            style={{ marginBottom: 0 }}>
            <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10"/><path d="M12 8v8M8 12h8"/>
            </svg>
            Новое заявление
          </button>
        </div>

        {error && (
          <div className="flex items-center gap-3 p-4 bg-red-50 border border-red-200 rounded-xl mb-6 text-red-600 text-sm">
            <svg className="w-5 h-5 shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10"/><path d="M12 8v4m0 4h.01"/>
            </svg>
            {error}
            <button onClick={() => setError('')} className="ml-auto border-none bg-transparent cursor-pointer text-red-400 hover:text-red-600 text-lg" style={{ marginBottom: 0 }}>×</button>
          </div>
        )}

        {orders.length === 0 ? (
          <div className="bg-white border border-gray-100 rounded-2xl p-16 text-center shadow-sm">
            <svg className="w-12 h-12 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" strokeWidth={1.5} viewBox="0 0 24 24">
              <path d="M11 21.73a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73z"/>
            </svg>
            <p className="text-gray-400 mb-4" style={{ margin: '0 0 16px' }}>У вас пока нет заявлений</p>
            <button onClick={() => navigate('/catalog')}
              className="px-6 py-3 bg-[#00B2FF] hover:bg-[#0095D9] text-white font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors"
              style={{ marginBottom: 0 }}>
              Перейти в каталог услуг
            </button>
          </div>
        ) : (
          <div className="flex flex-col gap-4">
            {orders.map(order => {
              const st = statusLabels[order.status] || { label: order.status, description: '', color: 'gray' };
              const colors = colorMap[st.color] || colorMap.gray;
              const contract = contracts[order.id];
              const subservice = order.subserviceId ? subservices[order.subserviceId] : null;
              const serviceName = getServiceName(order.serviceId);
              const canSign = contract?.approverSigned && contract?.financierSigned &&
                              contract?.directorSigned && !contract?.clientSigned;
              const isCancellable = ['pending_contract', 'revision'].includes(order.status);
              const isExpanded = expandedOrderId === order.id;
              const fields = orderFields[order.id] || [];

              return (
                <div key={order.id} className="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm hover:shadow-md transition-all">

                  {/* Header */}
                  <div className="flex items-start justify-between gap-3 mb-4 flex-wrap">
                    <div>
                      <p className="text-xs text-gray-400 mb-1" style={{ margin: '0 0 4px' }}>
                        Заявление №{order.orderNumber}
                        {order.applicationCode && (
                          <span className="ml-2 font-semibold text-[#0A2E5C]">{order.applicationCode}</span>
                        )}
                      </p>
                      <h3 className="font-semibold text-[#0A2E5C] text-base" style={{ margin: 0 }}>
                        {serviceName}
                      </h3>
                      {subservice && (
                        <p className="text-sm text-gray-500 mt-0.5" style={{ margin: '4px 0 0' }}>
                          {subservice.fullCode} — {subservice.name}
                        </p>
                      )}
                    </div>
                    <span className={`text-xs font-semibold px-3 py-1.5 rounded-full ${colors.bg} ${colors.text} shrink-0`}>
                      {st.label}
                    </span>
                  </div>

                  {/* Status description */}
                  {st.description && (
                    <div className={`flex items-start gap-3 p-3 mb-4 rounded-xl border ${colors.bg} ${colors.border}`}>
                      <svg className={`w-4 h-4 shrink-0 mt-0.5 ${colors.text}`} fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                        <circle cx="12" cy="12" r="10"/><path d="M12 8v4m0 4h.01"/>
                      </svg>
                      <p className={`text-sm ${colors.text}`} style={{ margin: 0 }}>{st.description}</p>
                    </div>
                  )}

                  {/* Secondary status */}
                  {order.secondaryStatus === 'awaiting_client_response' && (
                    <div className="flex items-start gap-3 p-3 mb-4 rounded-xl bg-amber-50 border border-amber-200">
                      <svg className="w-4 h-4 shrink-0 mt-0.5 text-amber-600" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                        <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/>
                      </svg>
                      <p className="text-sm text-amber-700" style={{ margin: 0 }}>
                        Специалист ожидает ответа или дополнительных материалов от вас.
                      </p>
                    </div>
                  )}

                  {/* Manager comment on revision */}
                  {order.status === 'revision' && order.managerComment && (
                    <div className="flex gap-3 p-4 mb-4 bg-orange-50 border border-orange-200 rounded-xl">
                      <svg className="w-5 h-5 text-orange-500 shrink-0 mt-0.5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                      </svg>
                      <div>
                        <p className="text-xs font-semibold text-orange-700 mb-1" style={{ margin: '0 0 4px' }}>Комментарий</p>
                        <p className="text-sm text-orange-800" style={{ margin: 0 }}>{order.managerComment}</p>
                      </div>
                    </div>
                  )}

                  {/* Actions */}
                  <div className="flex flex-wrap gap-2 mt-2">
                    <button onClick={() => loadOrderFields(order.id)}
                      className="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-600 font-medium rounded-lg border-none cursor-pointer text-sm transition-colors flex items-center gap-1.5"
                      style={{ marginBottom: 0 }}>
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                        <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1"/>
                      </svg>
                      {isExpanded ? 'Скрыть' : 'Содержимое заявки'}
                    </button>

                    {isCancellable && (
                      <button onClick={() => handleCancel(order.id)}
                        className="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-600 font-medium rounded-lg border-none cursor-pointer text-sm transition-colors"
                        style={{ marginBottom: 0 }}>
                        Отменить
                      </button>
                    )}

                    {order.status === 'revision' && (
                      <button onClick={() => navigate('/create-order', { state: { orderId: order.id, serviceId: order.serviceId, subserviceId: order.subserviceId } })}
                        className="px-4 py-2 bg-[#0A2E5C] hover:bg-[#0d3a73] text-white font-medium rounded-lg border-none cursor-pointer text-sm transition-colors"
                        style={{ marginBottom: 0 }}>
                        Исправить заявление
                      </button>
                    )}

                    {canSign && (
                      <button onClick={() => handleSign(order.id)}
                        className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-medium rounded-lg border-none cursor-pointer text-sm transition-colors flex items-center gap-2"
                        style={{ marginBottom: 0 }}>
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                        </svg>
                        Подписать договор
                      </button>
                    )}

                    {contract?.clientSigned && (
                      <span className="flex items-center gap-1 text-sm text-emerald-600 font-medium px-2">
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2.5} viewBox="0 0 24 24">
                          <path d="m9 11 3 3L22 4"/>
                        </svg>
                        Вы подписали
                      </span>
                    )}

                    {contract?.contractFileName && !['pending_contract', 'revision'].includes(order.status) && (
                      <button onClick={() => handleDownloadContract(order.id)}
                        className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg border-none cursor-pointer text-sm transition-colors flex items-center gap-2"
                        style={{ marginBottom: 0 }}>
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                          <path d="M6 22a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h8a2.4 2.4 0 0 1 1.704.706l3.588 3.588A2.4 2.4 0 0 1 20 8v12a2 2 0 0 1-2 2z"/>
                          <path d="M14 2v5a1 1 0 0 0 1 1h5"/>
                        </svg>
                        Договор
                      </button>
                    )}
                  </div>

                  {/* Expanded field values */}
                  {isExpanded && (
                    <div className="mt-4 border-t border-gray-100 pt-4">
                      {fields.length === 0 ? (
                        <p className="text-sm text-gray-400">Данные не найдены</p>
                      ) : (
                        <div className="flex flex-col gap-1">
                          {(() => {
                            const nonFile = fields.filter((f: any) => f.fieldValue && !f.fieldValue.startsWith('data:') && f.fieldValue !== 'true');
                            const snapshotFields = nonFile.filter((f: any) => f.filledByRole === 'client_snapshot');
                            const formFields = nonFile.filter((f: any) => f.filledByRole === 'client');
                            const hasFiles = fields.some((f: any) => f.fieldValue?.startsWith('data:'));
                            const hasObligation = fields.some((f: any) => f.fieldValue === 'true');

                            const rowGroups: Record<number, any[]> = {};
                            const staticFormFields: any[] = [];
                            formFields.forEach((f: any) => {
                              if (f.rowIndex > 0 || formFields.filter((x: any) => x.fieldKey === f.fieldKey).length > 1) {
                                if (!rowGroups[f.rowIndex]) rowGroups[f.rowIndex] = [];
                                rowGroups[f.rowIndex].push(f);
                              } else {
                                staticFormFields.push(f);
                              }
                            });

                            return (
                              <>
                                {snapshotFields.length > 0 && (
                                  <div className="bg-blue-50 rounded-xl p-3 mb-3">
                                    <p className="text-xs font-semibold text-blue-700 mb-2" style={{ margin: '0 0 8px' }}>
                                      Данные заявителя на момент подачи
                                    </p>
                                    {snapshotFields.map((f: any, i: number) => (
                                      <div key={i} className="flex justify-between items-start gap-3 py-1.5 border-b border-blue-100 last:border-0">
                                        <span className="text-xs text-blue-500">{fieldLabels[f.fieldKey] || f.fieldKey}</span>
                                        <span className="text-xs text-blue-800 text-right max-w-[60%]">{f.fieldValue}</span>
                                      </div>
                                    ))}
                                  </div>
                                )}

                                {Object.keys(rowGroups).length > 0 && Object.entries(rowGroups).map(([rowIdx, rowFields]) => (
                                  <div key={rowIdx} className="bg-gray-50 rounded-xl p-3 mb-2">
                                    <p className="text-xs font-semibold text-gray-500 mb-2" style={{ margin: '0 0 8px' }}>
                                      Прибор #{parseInt(rowIdx) + 1}
                                    </p>
                                    {(rowFields as any[]).map((f: any, i: number) => (
                                      <div key={i} className="flex justify-between items-start gap-3 py-1.5 border-b border-gray-100 last:border-0">
                                        <span className="text-xs text-gray-400">{fieldLabels[f.fieldKey] || f.fieldKey}</span>
                                        <span className="text-xs text-gray-700 text-right max-w-[60%]">{f.fieldValue}</span>
                                      </div>
                                    ))}
                                  </div>
                                ))}

                                {staticFormFields.map((f: any, i: number) => (
                                  <div key={i} className="flex justify-between items-start gap-3 py-1.5 border-b border-gray-50">
                                    <span className="text-xs text-gray-400">{fieldLabels[f.fieldKey] || f.fieldKey}</span>
                                    <span className="text-xs text-gray-700 text-right max-w-[60%]">{f.fieldValue}</span>
                                  </div>
                                ))}

                                {hasFiles && (
                                  <p className="text-xs text-blue-600 mt-2">📎 Документы прикреплены</p>
                                )}
                                {hasObligation && (
                                  <p className="text-xs text-green-600 mt-1">✓ Обязательство подтверждено</p>
                                )}
                              </>
                            );
                          })()}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}