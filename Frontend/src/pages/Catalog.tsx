import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { serviceApi } from '../services/api';
import type { Service } from '../types';
import './Catalog.css';

// Страница каталога метрологических услуг
// Доступна всем авторизованным пользователям
// Поддерживает поиск по названию/описанию и фильтрацию по типу прибора
export default function Catalog() {
  const navigate = useNavigate();
  const [services, setServices] = useState<Service[]>([]);
  const [filteredServices, setFilteredServices] = useState<Service[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string>('');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedType, setSelectedType] = useState<string>('');

  // Список типов приборов для фильтра
  const measurementTypes = [
    'Манометр',
    'Амперметр',
    'Вольтметр',
    'Термопара',
    'Осциллограф',
  ];

  // Загружаем услуги при монтировании компонента
  useEffect(() => {
    fetchServices();
  }, []);

  // Перефильтровываем при изменении поискового запроса или выбранного типа
  useEffect(() => {
    filterServices();
  }, [services, searchTerm, selectedType]);

  const fetchServices = async () => {
    try {
      setIsLoading(true);
      // GET /api/services — возвращает услуги с JOIN на laboratories (labName и standard)
      const response = await serviceApi.getAll();
      setServices(response.data);
    } catch (err: any) {
      setError('Ошибка при загрузке услуг');
    } finally {
      setIsLoading(false);
    }
  };

  // Фильтрация услуг на фронтенде без дополнительных запросов к бэкенду
  const filterServices = () => {
    let filtered = services;

    // Поиск по названию или описанию (регистронезависимый)
    if (searchTerm) {
      filtered = filtered.filter((service) =>
        service.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        service.description?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    // Фильтр по типу средства измерений
    if (selectedType) {
      filtered = filtered.filter(
        (service) => service.measurementType === selectedType
      );
    }

    setFilteredServices(filtered);
  };

  if (isLoading) {
    return (
      <div className="catalog-container">
        <div className="loading">Загрузка услуг...</div>
      </div>
    );
  }

  return (
    <div className="catalog-container">
      <h1>Каталог услуг</h1>

      {error && <div className="error-message">{error}</div>}

      {/* Панель поиска и фильтрации */}
      <div className="filters">
        <div className="search-group">
          <input
            type="text"
            placeholder="Поиск по названию или описанию..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
        </div>

        <div className="filter-group">
          <select
            value={selectedType}
            onChange={(e) => setSelectedType(e.target.value)}
            className="filter-select"
          >
            <option value="">Все типы приборов</option>
            {measurementTypes.map((type) => (
              <option key={type} value={type}>
                {type}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="services-count">
        Найдено услуг: {filteredServices.length}
      </div>

      {filteredServices.length === 0 ? (
        <div className="no-services">Услуги не найдены</div>
      ) : (
        <div className="services-grid">
          {filteredServices.map((service) => (
            <div key={service.id} className="service-card">
              <div className="service-header">
                <h3>{service.name}</h3>
                <span className="service-type">{service.measurementType}</span>
              </div>

              <p className="service-description">{service.description}</p>

              {/* Детали услуги — цена, срок, лаборатория и нормативный документ */}
              <div className="service-details">
                <div className="detail">
                  <span className="label">Стоимость:</span>
                  <span className="value">{service.price.toLocaleString()} ₸</span>
                </div>
                <div className="detail">
                  <span className="label">Срок:</span>
                  <span className="value">{service.durationDays} рабочих дней</span>
                </div>
                {/* labName подтягивается через JOIN с таблицей laboratories */}
                {service.labName && (
                  <div className="detail">
                    <span className="label">Лаборатория:</span>
                    <span className="value">{service.labName}</span>
                  </div>
                )}
                {/* standard — нормативный документ (ГОСТ) из колонки services.standard */}
                {service.standard && (
                  <div className="detail">
                    <span className="label">Норматив:</span>
                    <span className="value">{service.standard}</span>
                  </div>
                )}
              </div>

              {/* При нажатии передаём serviceId через location.state в CreateOrder */}
              <button
                className="btn-order"
                onClick={() => navigate('/create-order', { state: { serviceId: service.id } })}
              >
                Заказать услугу
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}