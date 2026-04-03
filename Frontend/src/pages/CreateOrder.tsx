import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import api, { serviceApi, orderApi } from '../services/api';
import type { Service, Laboratory } from '../types';

// Страница создания новой заявки на метрологические услуги
// Может получить предвыбранную услугу через location.state от страницы каталога
export default function CreateOrder() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuthStore();
  const [services, setServices] = useState<Service[]>([]);
  const [laboratories, setLaboratories] = useState<Laboratory[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Если пришли с каталога — serviceId уже заполнен через location.state
  const [formData, setFormData] = useState({
    serviceId: location.state?.serviceId?.toString() || '',
    labId: '',
    deviceType: '',
    model: '',
    serialNumber: '',
    quantity: '1',
    dueDate: '',
  });

  // Загружаем услуги и лаборатории параллельно при монтировании
  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setIsLoading(true);
      // Promise.all выполняет оба запроса одновременно для ускорения загрузки
      const [servicesRes, labsRes] = await Promise.all([
        serviceApi.getAll(),
        api.get('/laboratories')
      ]);
      setServices(servicesRes.data);
      setLaboratories(labsRes.data);
    } catch {
      setError('Ошибка при загрузке данных');
    } finally {
      setIsLoading(false);
    }
  };

  // Универсальный обработчик изменения полей формы
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Валидация обязательных полей на фронтенде
    if (!formData.serviceId || !formData.labId || !formData.deviceType || !formData.serialNumber || !formData.dueDate) {
      setError('Заполните все обязательные поля');
      return;
    }

    try {
      // Находим выбранную услугу для расчёта стоимости
      const selectedService = services.find(s => s.id === parseInt(formData.serviceId));
      if (!selectedService) { setError('Услуга не найдена'); return; }

      // Формируем payload для бэкенда
      // totalPrice = цена услуги × количество приборов
      const orderPayload = {
        clientId: user?.id,
        serviceId: parseInt(formData.serviceId),
        labId: parseInt(formData.labId),
        status: 'new',
        totalPrice: selectedService.price * parseInt(formData.quantity),
        dueDate: formData.dueDate,
        orderItems: [{
          deviceType: formData.deviceType,
          model: formData.model,
          serialNumber: formData.serialNumber,
          quantity: parseInt(formData.quantity),
          unitPrice: selectedService.price,
        }],
      };

      // Бэкенд автоматически создаёт договор после сохранения заявки
      await orderApi.create(orderPayload);
      setSuccess('Заявка создана успешно!');

      // Перенаправляем в MyOrders через 1.5 секунды
      setTimeout(() => navigate('/my-orders'), 1500);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка при создании заявки');
    }
  };

  // Выбранная услуга — для отображения деталей и расчёта стоимости
  const selectedService = services.find(s => s.id === parseInt(formData.serviceId));
  const totalPrice = selectedService ? selectedService.price * parseInt(formData.quantity || '1') : 0;

  const inputClass = "w-full px-4 py-3 border border-gray-200 rounded-xl text-gray-900 text-sm outline-none focus:border-[#00B2FF] focus:ring-2 focus:ring-[#00B2FF]/10 transition-all bg-white";
  const selectClass = "w-full px-4 py-3 border border-gray-200 rounded-xl text-gray-900 text-sm outline-none focus:border-[#00B2FF] focus:ring-2 focus:ring-[#00B2FF]/10 transition-all bg-white cursor-pointer";

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex items-center gap-3 text-gray-400">
          <svg className="w-5 h-5 animate-spin" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
            <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
          </svg>
          Загрузка данных...
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4">
      <div className="max-w-2xl mx-auto">

        {/* Заголовок */}
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-[#0A2E5C]" style={{ margin: 0, fontSize: '1.75rem' }}>
            Новая заявка
          </h1>
          <p className="text-gray-500 text-sm mt-1" style={{ margin: '4px 0 0' }}>
            Заполните форму для подачи заявки на метрологическую услугу
          </p>
        </div>

        {/* Уведомления */}
        {error && (
          <div className="flex items-center gap-3 p-4 bg-red-50 border border-red-200 rounded-xl mb-6 text-red-600 text-sm">
            <svg className="w-5 h-5 shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10"/><path d="M12 8v4m0 4h.01"/>
            </svg>
            {error}
          </div>
        )}
        {success && (
          <div className="flex items-center gap-3 p-4 bg-green-50 border border-green-200 rounded-xl mb-6 text-green-600 text-sm">
            <svg className="w-5 h-5 shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <path d="M21.801 10A10 10 0 1 1 17 3.335"/><path d="m9 11 3 3L22 4"/>
            </svg>
            {success}
          </div>
        )}

        <form onSubmit={handleSubmit} className="flex flex-col gap-6">

          {/* Секция: выбор услуги из каталога */}
          <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm">
            <p className="text-xs font-semibold text-[#00B2FF] uppercase tracking-wider mb-4" style={{ margin: '0 0 16px' }}>
              Выберите услугу
            </p>
            <div className="flex flex-col gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Услуга *</label>
                <select name="serviceId" value={formData.serviceId} onChange={handleChange} required className={selectClass} style={{ fontFamily: 'inherit', marginBottom: 0 }}>
                  <option value="">— Выберите услугу —</option>
                  {services.map(s => (
                    <option key={s.id} value={s.id}>
                      {s.name} ({s.measurementType}) — {s.price.toLocaleString()} ₸
                    </option>
                  ))}
                </select>
              </div>

              {/* Показываем описание выбранной услуги */}
              {selectedService && (
                <div className="bg-[#00B2FF]/5 border-l-4 border-[#00B2FF] rounded-lg p-4">
                  <p className="text-sm text-gray-600 mb-1" style={{ margin: '0 0 4px' }}>
                    <span className="font-semibold text-[#0A2E5C]">Описание: </span>
                    {selectedService.description}
                  </p>
                  <p className="text-sm text-gray-600" style={{ margin: 0 }}>
                    <span className="font-semibold text-[#0A2E5C]">Срок выполнения: </span>
                    {selectedService.durationDays} рабочих дней
                  </p>
                </div>
              )}
            </div>
          </div>

          {/* Секция: данные прибора для поверки */}
          <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm">
            <p className="text-xs font-semibold text-[#00B2FF] uppercase tracking-wider mb-4" style={{ margin: '0 0 16px' }}>
              Информация о приборе
            </p>
            <div className="flex flex-col gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Тип прибора *</label>
                <input type="text" name="deviceType" value={formData.deviceType} onChange={handleChange}
                  placeholder="Манометр, Амперметр и т.д." required className={inputClass}
                  style={{ fontFamily: 'inherit', marginBottom: 0 }} />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Модель</label>
                <input type="text" name="model" value={formData.model} onChange={handleChange}
                  placeholder="Модель прибора" className={inputClass}
                  style={{ fontFamily: 'inherit', marginBottom: 0 }} />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Серийный номер *</label>
                <input type="text" name="serialNumber" value={formData.serialNumber} onChange={handleChange}
                  placeholder="Введите серийный номер" required className={inputClass}
                  style={{ fontFamily: 'inherit', marginBottom: 0 }} />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Количество *</label>
                <input type="number" name="quantity" value={formData.quantity} onChange={handleChange}
                  min="1" required className={inputClass}
                  style={{ fontFamily: 'inherit', marginBottom: 0 }} />
              </div>
            </div>
          </div>

          {/* Секция: выбор лаборатории и даты */}
          <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm">
            <p className="text-xs font-semibold text-[#00B2FF] uppercase tracking-wider mb-4" style={{ margin: '0 0 16px' }}>
              Место и дата
            </p>
            <div className="flex flex-col gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Лаборатория *</label>
                {/* Лаборатории загружаются с бэкенда из таблицы laboratories */}
                <select name="labId" value={formData.labId} onChange={handleChange} required className={selectClass} style={{ fontFamily: 'inherit', marginBottom: 0 }}>
                  <option value="">— Выберите лабораторию —</option>
                  {laboratories.map(lab => (
                    <option key={lab.id} value={lab.id}>{lab.name} ({lab.city})</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Плановая дата сдачи *</label>
                <input type="date" name="dueDate" value={formData.dueDate} onChange={handleChange}
                  required className={inputClass}
                  style={{ fontFamily: 'inherit', marginBottom: 0 }} />
              </div>
            </div>
          </div>

          {/* Секция: итоговая стоимость — рассчитывается автоматически */}
          {selectedService && (
            <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm">
              <p className="text-xs font-semibold text-[#00B2FF] uppercase tracking-wider mb-4" style={{ margin: '0 0 16px' }}>
                Итого
              </p>
              <div className="flex flex-col gap-2">
                <div className="flex justify-between items-center py-2 border-b border-gray-50">
                  <span className="text-sm text-gray-500">Стоимость услуги</span>
                  <span className="text-sm font-medium text-gray-700">{selectedService.price.toLocaleString()} ₸</span>
                </div>
                <div className="flex justify-between items-center py-2 border-b border-gray-50">
                  <span className="text-sm text-gray-500">Количество</span>
                  <span className="text-sm font-medium text-gray-700">{formData.quantity} шт.</span>
                </div>
                <div className="flex justify-between items-center py-3">
                  <span className="text-base font-bold text-[#0A2E5C]">Итого</span>
                  <span className="text-xl font-bold text-[#00B2FF]">{totalPrice.toLocaleString()} ₸</span>
                </div>
              </div>
            </div>
          )}

          <button
            type="submit"
            className="w-full py-4 bg-[#00B2FF] hover:bg-[#0095D9] text-white font-semibold rounded-xl border-none cursor-pointer text-base transition-colors"
            style={{ marginBottom: 0 }}
          >
            Создать заявку
          </button>
        </form>
      </div>
    </div>
  );
}