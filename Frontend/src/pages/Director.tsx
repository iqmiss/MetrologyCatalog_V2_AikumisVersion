import { useState, useEffect } from 'react';
import { useAuthStore } from '../store/authStore';
import { orderApi, contractApi } from '../services/api';
import type { Order } from '../types';

export default function Director() {
  const { user } = useAuthStore();
  const [orders, setOrders] = useState<Order[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setIsLoading(true);
      const response = await orderApi.getByStatus('awaiting_director');
      setOrders(response.data);
    } catch {
      setError('Ошибка при загрузке заявок');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSign = async (orderId: number) => {
    if (!window.confirm('Подписать договор ЭЦП?')) return;
    try {
      await contractApi.signByDirector(orderId, user?.id || 0);
      setOrders(prev => prev.filter(o => o.id !== orderId));
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка при подписании');
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
            Подписание договоров
          </h1>
          <p className="text-gray-500 text-sm mt-1" style={{ margin: '4px 0 0' }}>
            Договоры ожидающие вашей подписи
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
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
            </svg>
            <p className="text-gray-400">Нет договоров для подписания</p>
          </div>
        ) : (
          <div className="flex flex-col gap-4">
            {orders.map(order => (
              <div key={order.id} className="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm">
                <div className="flex items-center justify-between mb-4 flex-wrap gap-3">
                  <h3 className="font-bold text-[#0A2E5C]" style={{ margin: 0, fontSize: '1rem' }}>
                    Заявка #{order.orderNumber}
                  </h3>
                  <span className="text-xs font-semibold px-3 py-1 rounded-full bg-indigo-100 text-indigo-700">
                    Ожидает подписи директора
                  </span>
                </div>

                <div className="bg-gray-50 rounded-xl p-4 mb-4 grid grid-cols-2 md:grid-cols-3 gap-3">
                  <div>
                    <p className="text-xs text-gray-400 mb-0.5" style={{ margin: '0 0 2px' }}>Стоимость</p>
                    <p className="text-sm font-bold text-[#0A2E5C]" style={{ margin: 0 }}>{order.totalPrice.toLocaleString()} ₸</p>
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

                <div className="flex gap-2">
                  <button
                    onClick={() => handleSign(order.id)}
                    className="px-4 py-2 bg-[#0A2E5C] hover:bg-[#0d3a73] text-white font-medium rounded-lg border-none cursor-pointer text-sm transition-colors flex items-center gap-2"
                    style={{ marginBottom: 0 }}
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                    Подписать ЭЦП
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