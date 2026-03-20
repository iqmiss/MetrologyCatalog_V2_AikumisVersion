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
  const [error, setError] = useState<string>('');
  const [success, setSuccess] = useState<string>('');

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
    } catch (err) {
      setError('Ошибка при загрузке данных');
    } finally {
      setIsLoading(false);
    }
  };

  // Универсальный обработчик изменения полей формы
  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Валидация обязательных полей на фронтенде
    if (
      !formData.serviceId ||
      !formData.labId ||
      !formData.deviceType ||
      !formData.serialNumber ||
      !formData.dueDate
    ) {
      setError('Заполните все обязательные поля');
      return;
    }

    try {
      // Находим выбранную услугу для расчёта стоимости
      const selectedService = services.find(
        (s) => s.id === parseInt(formData.serviceId)
      );

      if (!selectedService) {
        setError('Услуга не найдена');
        return;
      }

      // Формируем payload для бэкенда
      // totalPrice = цена услуги × количество приборов
      const orderPayload = {
        clientId: user?.id,
        serviceId: parseInt(formData.serviceId),
        labId: parseInt(formData.labId),
        status: 'new',
        totalPrice: selectedService.price * parseInt(formData.quantity),
        dueDate: formData.dueDate,
        orderItems: [
          {
            deviceType: formData.deviceType,
            model: formData.model,
            serialNumber: formData.serialNumber,
            quantity: parseInt(formData.quantity),
            unitPrice: selectedService.price,
          },
        ],
      };

      // Бэкенд автоматически создаёт договор после сохранения заявки
      await orderApi.create(orderPayload);
      setSuccess('Заявка создана успешно!');

      // Перенаправляем в MyOrders через 1.5 секунды
      setTimeout(() => {
        navigate('/my-orders');
      }, 1500);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка при создании заявки');
    }
  };

  if (isLoading) {
    return (
      <div className="create-order-container">
        <div className="loading">Загрузка данных...</div>
      </div>
    );
  }

  return (
    <div className="create-order-container">
      <div className="create-order-box">
        <h1>Создать новую заявку</h1>

        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        <form onSubmit={handleSubmit}>
          {/* Секция: выбор услуги из каталога */}
          <div className="form-section">
            <h3>Выберите услугу</h3>

            <div className="form-group">
              <label htmlFor="serviceId">Услуга *</label>
              <select
                id="serviceId"
                name="serviceId"
                value={formData.serviceId}
                onChange={handleChange}
                required
              >
                <option value="">-- Выберите услугу --</option>
                {services.map((service) => (
                  <option key={service.id} value={service.id}>
                    {service.name} ({service.measurementType}) - {service.price}₸
                  </option>
                ))}
              </select>
            </div>

            {/* Показываем описание выбранной услуги */}
            {formData.serviceId && (
              <div className="service-info">
                {services.find((s) => s.id === parseInt(formData.serviceId)) && (
                  <>
                    <p>
                      <strong>Описание:</strong>{' '}
                      {services.find((s) => s.id === parseInt(formData.serviceId))?.description}
                    </p>
                    <p>
                      <strong>Срок выполнения:</strong>{' '}
                      {services.find((s) => s.id === parseInt(formData.serviceId))?.durationDays}{' '}
                      рабочих дней
                    </p>
                  </>
                )}
              </div>
            )}
          </div>

          {/* Секция: данные прибора для поверки */}
          <div className="form-section">
            <h3>Информация о приборе</h3>

            <div className="form-group">
              <label htmlFor="deviceType">Тип прибора *</label>
              <input
                id="deviceType"
                type="text"
                name="deviceType"
                value={formData.deviceType}
                onChange={handleChange}
                placeholder="Манометр, Амперметр и т.д."
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="model">Модель</label>
              <input
                id="model"
                type="text"
                name="model"
                value={formData.model}
                onChange={handleChange}
                placeholder="Модель прибора"
              />
            </div>

            <div className="form-group">
              <label htmlFor="serialNumber">Серийный номер *</label>
              <input
                id="serialNumber"
                type="text"
                name="serialNumber"
                value={formData.serialNumber}
                onChange={handleChange}
                placeholder="Введите серийный номер"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="quantity">Количество *</label>
              <input
                id="quantity"
                type="number"
                name="quantity"
                value={formData.quantity}
                onChange={handleChange}
                min="1"
                required
              />
            </div>
          </div>

          {/* Секция: выбор лаборатории и даты */}
          <div className="form-section">
            <h3>Место и дата</h3>

            <div className="form-group">
              <label htmlFor="labId">Лаборатория *</label>
              <select
                id="labId"
                name="labId"
                value={formData.labId}
                onChange={handleChange}
                required
              >
                <option value="">-- Выберите лабораторию --</option>
                {/* Лаборатории загружаются с бэкенда из таблицы laboratories */}
                {laboratories.map((lab) => (
                  <option key={lab.id} value={lab.id}>
                    {lab.name} ({lab.city})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="dueDate">Плановая дата сдачи *</label>
              <input
                id="dueDate"
                type="date"
                name="dueDate"
                value={formData.dueDate}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          {/* Секция: итоговая стоимость — рассчитывается автоматически */}
          <div className="form-section">
            <h3>Итого</h3>
            <div className="total-info">
              {formData.serviceId && (
                <>
                  <div className="total-row">
                    <span>Стоимость услуги:</span>
                    <span>
                      {(
                        (services.find((s) => s.id === parseInt(formData.serviceId))?.price || 0) *
                        parseInt(formData.quantity)
                      ).toLocaleString()}
                      ₸
                    </span>
                  </div>
                  <div className="total-row total">
                    <span>Итого:</span>
                    <span>
                      {(
                        (services.find((s) => s.id === parseInt(formData.serviceId))?.price || 0) *
                        parseInt(formData.quantity)
                      ).toLocaleString()}
                      ₸
                    </span>
                  </div>
                </>
              )}
            </div>
          </div>

          <button type="submit">Создать заявку</button>
        </form>
      </div>
    </div>
  );
}