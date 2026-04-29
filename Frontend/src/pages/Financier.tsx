import { useState, useEffect } from 'react';
import { orderApi } from '../services/api';
import type { Order } from '../types';

export default function Financier() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [comments, setComments] = useState<Record<number, string>>({});
  const [processing, setProcessing] = useState<number | null>(null);
  const [invoiceAmounts, setInvoiceAmounts] = useState<Record<number, string>>({});

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setIsLoading(true);
      const response = await orderApi.getByStatus('awaiting_payment');
      setOrders(response.data);
    } catch {
      setError('Ошибка при загрузке заявок');
    } finally {
      setIsLoading(false);
    }
  };

  const handlePayment = async (orderId: number, paid: boolean) => {
    const action = paid ? 'подтвердить оплату' : 'пропустить оплату';
    if (!window.confirm(`${action.charAt(0).toUpperCase() + action.slice(1)}?`)) return;
    try {
      setProcessing(orderId);
      const invoiceAmount = invoiceAmounts[orderId] ? parseFloat(invoiceAmounts[orderId]) : null;
      await orderApi.confirmPayment(orderId, paid, comments[orderId], invoiceAmount);
      setOrders(prev => prev.filter(o => o.id !== orderId));
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка при обработке оплаты');
    } finally {
      setProcessing(null);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex items-center gap-3 text-gray-400">
          <svg className="w-5 h-5 animate-spin" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
            <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
          </svg>
          Загрузка...
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4">
      <div className="max-w-4xl mx-auto">

        <div className="mb-8">
          <h1 className="text-2xl font-bold text-[#0A2E5C]" style={{ margin: 0, fontSize: '1.75rem' }}>
            Счета и оплаты
          </h1>
          <p className="text-gray-500 text-sm mt-1" style={{ margin: '4px 0 0' }}>
            Заявки ожидающие подтверждения оплаты
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
              <rect width="20" height="14" x="2" y="5" rx="2"/><line x1="2" x2="22" y1="10" y2="10"/>
            </svg>
            <p className="text-gray-400">Нет заявок ожидающих оплаты</p>
          </div>
        ) : (
          <div className="flex flex-col gap-4">
            {orders.map(order => (
              <div key={order.id} className="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm">
                <div className="flex items-center justify-between mb-4 flex-wrap gap-3">
                  <h3 className="font-bold text-[#0A2E5C]" style={{ margin: 0, fontSize: '1rem' }}>
                    Заявка #{order.orderNumber}
                  </h3>
                  <span className="text-xs font-semibold px-3 py-1 rounded-full bg-yellow-100 text-yellow-700">
                    Ожидает оплаты
                  </span>
                </div>

                <div className="bg-gray-50 rounded-xl p-4 mb-4 grid grid-cols-2 md:grid-cols-3 gap-3">
                  <div>
                    <p className="text-xs text-gray-400 mb-0.5" style={{ margin: '0 0 2px' }}>Сумма к оплате</p>
                    <p className="text-lg font-bold text-[#0A2E5C]" style={{ margin: 0 }}>{order.totalPrice.toLocaleString()} ₸</p>
                  </div>
                  <div>
                    <p className="text-xs text-gray-400 mb-0.5" style={{ margin: '0 0 2px' }}>Клиент ID</p>
                    <p className="text-sm font-semibold text-gray-700" style={{ margin: 0 }}>#{order.clientId}</p>
                  </div>
                  <div>
                    <p className="text-xs text-gray-400 mb-0.5" style={{ margin: '0 0 2px' }}>Лаборатория</p>
                    <p className="text-sm font-semibold text-gray-700" style={{ margin: 0 }}>#{order.labId}</p>
                  </div>
                </div>

                {/* Комментарий финансиста */}
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">
                    Комментарий об оплате (необязательно)
                  </label>
                  <textarea
                    value={comments[order.id] || ''}
                    onChange={e => setComments(prev => ({ ...prev, [order.id]: e.target.value }))}
                    placeholder="Например: оплата получена 27.04.2026, реквизиты №..."
                    className="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm outline-none focus:border-[#00B2FF] focus:ring-2 focus:ring-[#00B2FF]/10 transition-all resize-none"
                    rows={2}
                    style={{ fontFamily: 'inherit', marginBottom: 0 }}
                  />
                </div>

                {/* Поле для выставления счёта */}
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">
                    Сумма счёта (оставьте пустым если совпадает с договором)
                  </label>
                  <input
                    type="number"
                    value={invoiceAmounts[order.id] || ''}
                    onChange={e => setInvoiceAmounts(prev => ({ ...prev, [order.id]: e.target.value }))}
                    placeholder={`${order.totalPrice.toLocaleString()} ₸`}
                    className="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm outline-none focus:border-[#00B2FF] focus:ring-2 focus:ring-[#00B2FF]/10 transition-all"
                    style={{ fontFamily: 'inherit', marginBottom: 0 }}
                  />
                </div>

                <div className="flex gap-2">
                  <button
                    onClick={() => handlePayment(order.id, true)}
                    disabled={processing === order.id}
                    className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-medium rounded-lg border-none cursor-pointer text-sm transition-colors flex items-center gap-2"
                    style={{ marginBottom: 0 }}
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2.5} viewBox="0 0 24 24">
                      <path d="m9 11 3 3L22 4"/>
                    </svg>
                    Оплата получена
                  </button>
                  <button
                    onClick={() => handlePayment(order.id, false)}
                    disabled={processing === order.id}
                    className="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-600 font-medium rounded-lg border-none cursor-pointer text-sm transition-colors flex items-center gap-2"
                    style={{ marginBottom: 0 }}
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                      <path d="M5 12h14"/>
                    </svg>
                    Пропустить оплату
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}