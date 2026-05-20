import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { serviceApi, subserviceApi } from '../services/api';
import type { Service, Subservice } from '../types';

export default function Catalog() {
  const navigate = useNavigate();
  const [services, setServices] = useState<Service[]>([]);
  const [filteredServices, setFilteredServices] = useState<Service[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedType, setSelectedType] = useState('');
  const [expandedServiceId, setExpandedServiceId] = useState<number | null>(null);
  const [subservices, setSubservices] = useState<Record<number, Subservice[]>>({});
  const [selectedSubservice, setSelectedSubservice] = useState<Record<number, number | null>>({});
  const [loadingSubservices, setLoadingSubservices] = useState<Record<number, boolean>>({});

  const measurementTypes = ['Манометр', 'Амперметр', 'Вольтметр', 'Термопара', 'Осциллограф'];

  useEffect(() => { fetchServices(); }, []);
  useEffect(() => { filterServices(); }, [services, searchTerm, selectedType]);

  const fetchServices = async () => {
    try {
      setIsLoading(true);
      const response = await serviceApi.getAll();
      setServices(response.data);
    } catch {
      setError('Ошибка при загрузке услуг');
    } finally {
      setIsLoading(false);
    }
  };

  const filterServices = () => {
    let filtered = services;
    if (searchTerm) {
      filtered = filtered.filter(s =>
        s.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        s.description?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }
    if (selectedType) {
      filtered = filtered.filter(s => s.measurementType === selectedType);
    }
    setFilteredServices(filtered);
  };

  const handleCardClick = async (serviceId: number) => {
    if (expandedServiceId === serviceId) {
      setExpandedServiceId(null);
      return;
    }
    setExpandedServiceId(serviceId);

    if (!subservices[serviceId]) {
      setLoadingSubservices(prev => ({ ...prev, [serviceId]: true }));
      try {
        const res = await subserviceApi.getByServiceId(serviceId);
        setSubservices(prev => ({ ...prev, [serviceId]: res.data }));
      } catch {
        setSubservices(prev => ({ ...prev, [serviceId]: [] }));
      } finally {
        setLoadingSubservices(prev => ({ ...prev, [serviceId]: false }));
      }
    }
  };

  const handleOrder = (service: Service) => {
    const subserviceId = selectedSubservice[service.id];
    const subs = subservices[service.id] || [];

    if (subs.length > 0 && !subserviceId) {
      alert('Пожалуйста, выберите подуслугу');
      return;
    }

    navigate('/create-order', {
      state: { serviceId: service.id, subserviceId: subserviceId || null }
    });
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex items-center gap-3 text-gray-400">
          <svg className="w-5 h-5 animate-spin" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
            <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
          </svg>
          Загрузка услуг...
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4">
      <div className="max-w-7xl mx-auto">

        <div className="mb-8">
          <h1 className="text-2xl font-bold text-[#0A2E5C]" style={{ margin: 0, fontSize: '1.75rem' }}>
            Каталог услуг
          </h1>
          <p className="text-gray-500 text-sm mt-1" style={{ margin: '4px 0 0' }}>
            Полный спектр метрологических услуг для вашего бизнеса
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

        <div className="flex gap-4 mb-6 flex-wrap">
          <div className="flex-1 min-w-[200px] relative">
            <svg className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none"
              fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <path d="m21 21-4.34-4.34"/><circle cx="11" cy="11" r="8"/>
            </svg>
            <input
              type="text"
              placeholder="Поиск по названию или описанию..."
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-3 bg-white border border-gray-200 rounded-xl text-sm text-gray-900 outline-none focus:border-[#00B2FF] focus:ring-2 focus:ring-[#00B2FF]/10 transition-all"
              style={{ fontFamily: 'inherit', marginBottom: 0 }}
            />
          </div>
          <div className="min-w-[200px]">
            <select
              value={selectedType}
              onChange={e => setSelectedType(e.target.value)}
              className="w-full px-4 py-3 bg-white border border-gray-200 rounded-xl text-sm text-gray-900 outline-none focus:border-[#00B2FF] focus:ring-2 focus:ring-[#00B2FF]/10 transition-all cursor-pointer"
              style={{ fontFamily: 'inherit', marginBottom: 0 }}
            >
              <option value="">Все типы приборов</option>
              {measurementTypes.map(type => (
                <option key={type} value={type}>{type}</option>
              ))}
            </select>
          </div>
        </div>

        <p className="text-sm text-gray-400 mb-6" style={{ margin: '0 0 24px' }}>
          Найдено услуг: <span className="font-semibold text-[#0A2E5C]">{filteredServices.length}</span>
        </p>

        {filteredServices.length === 0 ? (
          <div className="text-center py-20 text-gray-400">
            <svg className="w-12 h-12 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" strokeWidth={1.5} viewBox="0 0 24 24">
              <path d="m21 21-4.34-4.34"/><circle cx="11" cy="11" r="8"/>
            </svg>
            Услуги не найдены
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {filteredServices.map(service => {
              const isExpanded = expandedServiceId === service.id;
              const subs = subservices[service.id] || [];
              const loadingSubs = loadingSubservices[service.id];
              const selectedSubId = selectedSubservice[service.id];
              const selectedSub = subs.find(s => s.id === selectedSubId);

              return (
                <div key={service.id}
                  className="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm hover:shadow-md hover:border-[#00B2FF]/30 transition-all duration-300 flex flex-col">

                  {/* Header with code badge */}
                  <div className="flex items-start justify-between gap-3 mb-3">
                    <h3 className="font-semibold text-[#0A2E5C] leading-tight" style={{ margin: 0, fontSize: '1rem' }}>
                      {service.name}
                    </h3>
                    <div className="flex flex-col items-end gap-1 shrink-0">
                      {service.code && (
                        <span className="text-xs font-bold bg-[#0A2E5C]/10 text-[#0A2E5C] px-2 py-0.5 rounded-full">
                          {service.code}
                        </span>
                      )}
                      <span className="text-xs font-semibold bg-[#00B2FF]/10 text-[#00B2FF] px-3 py-1 rounded-full whitespace-nowrap">
                        {service.measurementType}
                      </span>
                    </div>
                  </div>

                  {/* Description */}
                  <p className="text-gray-500 text-sm leading-relaxed mb-4 flex-1" style={{ margin: '0 0 16px' }}>
                    {service.description}
                  </p>

                  {/* Details */}
                  <div className="bg-gray-50 rounded-xl p-4 mb-4 flex flex-col gap-2.5">
                    <div className="flex justify-between items-center">
                      <span className="text-xs text-gray-400">Стоимость</span>
                      <span className="text-sm font-bold text-[#0A2E5C]">
                        {service.price != null ? `${service.price.toLocaleString()} ₸` : 'По запросу'}
                      </span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="text-xs text-gray-400">Срок</span>
                      <span className="text-sm font-semibold text-gray-700">{service.durationDays} рабочих дней</span>
                    </div>
                    {service.labName && (
                      <div className="flex justify-between items-center">
                        <span className="text-xs text-gray-400">Лаборатория</span>
                        <span className="text-sm font-semibold text-gray-700">{service.labName}</span>
                      </div>
                    )}
                    {service.standard && (
                      <div className="flex justify-between items-center">
                        <span className="text-xs text-gray-400">Норматив</span>
                        <span className="text-sm font-semibold text-gray-700">{service.standard}</span>
                      </div>
                    )}
                  </div>

                  {/* Expand button */}
                  <button
                    onClick={() => handleCardClick(service.id)}
                    className="w-full py-2.5 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors mb-3"
                  >
                    {isExpanded ? '▲ Скрыть подуслуги' : '▼ Выбрать подуслугу'}
                  </button>

                  {/* Subservice dropdown */}
                  {isExpanded && (
                    <div className="mb-3">
                      {loadingSubs ? (
                        <p className="text-xs text-gray-400 text-center py-2">Загрузка...</p>
                      ) : subs.length === 0 ? (
                        <p className="text-xs text-gray-400 text-center py-2">Подуслуги не найдены</p>
                      ) : (
                        <>
                          <select
                            value={selectedSubId ?? ''}
                            onChange={e => setSelectedSubservice(prev => ({
                              ...prev,
                              [service.id]: e.target.value ? Number(e.target.value) : null
                            }))}
                            className="w-full px-3 py-2 bg-white border border-gray-200 rounded-xl text-sm text-gray-900 outline-none focus:border-[#00B2FF] transition-all cursor-pointer mb-2"
                            style={{ fontFamily: 'inherit', marginBottom: '8px' }}
                          >
                            <option value="">— Выберите подуслугу —</option>
                            {subs.map(sub => (
                              <option key={sub.id} value={sub.id}>
                                {sub.fullCode} — {sub.name}
                              </option>
                            ))}
                          </select>

                          {/* Selected subservice description */}
                          {selectedSub?.description && (
                            <div className="bg-blue-50 border border-blue-100 rounded-xl p-3 text-xs text-blue-700">
                              {selectedSub.description}
                            </div>
                          )}
                        </>
                      )}
                    </div>
                  )}

                  {/* Order button */}
                  <button
                    onClick={() => handleOrder(service)}
                    className="w-full py-3 bg-[#00B2FF] hover:bg-[#0095D9] text-white font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors mt-auto"
                    style={{ marginBottom: 0 }}
                  >
                    Заказать услугу
                  </button>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}