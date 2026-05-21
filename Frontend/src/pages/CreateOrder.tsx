import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { serviceApi, orderApi, userApi, subserviceApi } from '../services/api';
import type { Service, User, Subservice, SubserviceField } from '../types';

export default function CreateOrder() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuthStore();

  const preselectedServiceId = location.state?.serviceId;
  const preselectedSubserviceId = location.state?.subserviceId;

  const [services, setServices] = useState<Service[]>([]);
  const [clients, setClients] = useState<User[]>([]);
  const [selectedClientId, setSelectedClientId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [selectedServiceId, setSelectedServiceId] = useState<number | null>(preselectedServiceId || null);
  const [selectedSubserviceId, setSelectedSubserviceId] = useState<number | null>(preselectedSubserviceId || null);
  const [subservice, setSubservice] = useState<Subservice | null>(null);
  const [fields, setFields] = useState<SubserviceField[]>([]);

  const [fieldValues, setFieldValues] = useState<Record<string, Record<number, string>>>({});
  const [tableRows, setTableRows] = useState<number[]>([0]);

  useEffect(() => { fetchData(); }, []);

    useEffect(() => {
      if (user?.role === 'client' && !location.state?.subserviceId) {
        navigate('/catalog');
      }
    }, []);

  useEffect(() => {
    if (selectedSubserviceId) loadSubserviceFields(selectedSubserviceId);
    else { setFields([]); setSubservice(null); }
  }, [selectedSubserviceId]);

  const fetchData = async () => {
    try {
      setIsLoading(true);
      const requests: Promise<any>[] = [serviceApi.getAll()];
      if (user?.role === 'manager') requests.push(userApi.getClients());
      const results = await Promise.all(requests);
      setServices(results[0].data);
      if (user?.role === 'manager' && results[1]) setClients(results[1].data);
    } catch {
      setError('Ошибка при загрузке данных');
    } finally {
      setIsLoading(false);
    }
  };

  const loadSubserviceFields = async (subserviceId: number) => {
    try {
      const [subRes, fieldsRes] = await Promise.all([
        subserviceApi.getById(subserviceId),
        subserviceApi.getFields(subserviceId),
      ]);
      setSubservice(subRes.data);
      setFields(fieldsRes.data);
      setFieldValues({});
      setTableRows([0]);
    } catch {
      setError('Ошибка при загрузке полей формы');
    }
  };

  const getFieldValue = (fieldKey: string, rowIndex: number) =>
    fieldValues[fieldKey]?.[rowIndex] || '';

  const setFieldValue = (fieldKey: string, rowIndex: number, value: string) => {
    setFieldValues(prev => ({
      ...prev,
      [fieldKey]: { ...(prev[fieldKey] || {}), [rowIndex]: value }
    }));
  };

  const addTableRow = () => {
    setTableRows(prev => [...prev, prev[prev.length - 1] + 1]);
  };

  const removeTableRow = (rowIndex: number) => {
    if (tableRows.length <= 1) return;
    setTableRows(prev => prev.filter(r => r !== rowIndex));
    setFieldValues(prev => {
      const updated = { ...prev };
      Object.keys(updated).forEach(key => {
        const { [rowIndex]: _, ...rest } = updated[key];
        updated[key] = rest;
      });
      return updated;
    });
  };

  const repeatingFields = fields.filter(f => f.isRepeating);
  const staticFields = fields.filter(f => !f.isRepeating);

  const renderField = (field: SubserviceField, rowIndex: number = 0) => {
    const value = getFieldValue(field.fieldKey, rowIndex);
    const inputClass = "w-full px-4 py-3 border border-gray-200 rounded-xl text-gray-900 text-sm outline-none focus:border-[#00B2FF] focus:ring-2 focus:ring-[#00B2FF]/10 transition-all bg-white";

    if (field.fieldType === 'select') {
      const options = field.optionsJson ? JSON.parse(field.optionsJson) : [];
      return (
        <select value={value} onChange={e => setFieldValue(field.fieldKey, rowIndex, e.target.value)}
          className={inputClass + ' cursor-pointer'} style={{ fontFamily: 'inherit', marginBottom: 0 }}
          required={field.required}>
          <option value="">— Выберите —</option>
          {options.map((opt: string) => <option key={opt} value={opt}>{opt}</option>)}
        </select>
      );
    }


    if (field.fieldType === 'checkbox') {
      return (
        <label className="flex items-start gap-3 cursor-pointer">
          <input
            type="checkbox"
            checked={value === 'true'}
            onChange={e => setFieldValue(field.fieldKey, rowIndex, e.target.checked ? 'true' : '')}
            className="mt-1 w-4 h-4 accent-[#00B2FF] shrink-0 cursor-pointer"
            required={field.required}
          />
          <span className="text-sm text-gray-700 leading-relaxed">{field.labelRu}</span>
        </label>
      );
    }

    if (field.fieldType === 'file') {
      return (
        <input type="file" onChange={e => {
          const file = e.target.files?.[0];
          if (!file) return;
          const reader = new FileReader();
          reader.onload = () => setFieldValue(field.fieldKey, rowIndex, reader.result as string);
          reader.readAsDataURL(file);
        }} className={inputClass} style={{ fontFamily: 'inherit', marginBottom: 0 }} />
      );
    }

    return (
      <input type={field.fieldType === 'number' ? 'number' : field.fieldType === 'date' ? 'date' : 'text'}
        value={value} onChange={e => setFieldValue(field.fieldKey, rowIndex, e.target.value)}
        required={field.required} className={inputClass}
        style={{ fontFamily: 'inherit', marginBottom: 0 }} />
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (user?.role === 'manager' && !selectedClientId) { setError('Выберите клиента'); return; }
    if (!selectedServiceId) { setError('Выберите услугу'); return; }

    for (const field of fields) {
      if (!field.required) continue;
      if (field.isRepeating) {
        for (const rowIndex of tableRows) {
          if (!getFieldValue(field.fieldKey, rowIndex)) {
            setError(`Заполните обязательное поле: ${field.labelRu}`);
            return;
          }
        }
      } else {
        if (!getFieldValue(field.fieldKey, 0)) {
          setError(`Заполните обязательное поле: ${field.labelRu}`);
          return;
        }
      }
    }

    try {
      const orderPayload: any = {
        clientId: user?.role === 'manager' ? selectedClientId : user?.id,
        serviceId: selectedServiceId,
        labId: 1,
        dueDate: null,
        subserviceId: selectedSubserviceId || null,
        orderItems: repeatingFields.length === 0
          ? [{ deviceType: 'Не указан', model: '', serialNumber: 'Н/А', quantity: 1 }]
          : tableRows.map(rowIndex => ({
              deviceType: getFieldValue('si_name', rowIndex) || 'Не указан',
              model: getFieldValue('producer', rowIndex) || '',
              serialNumber: getFieldValue('serial_number', rowIndex) || String(rowIndex + 1),
              quantity: parseInt(getFieldValue('quantity', rowIndex) || '1'),
            })),
      };

      const orderRes = await orderApi.create(orderPayload);
      const orderId = orderRes.data.id;

      if (fields.length > 0) {
        const fieldPayload: any[] = [];
        for (const field of fields) {
          if (field.isRepeating) {
            tableRows.forEach(rowIndex => {
              fieldPayload.push({
                fieldKey: field.fieldKey,
                fieldValue: getFieldValue(field.fieldKey, rowIndex),
                rowIndex,
                filledByRole: 'client',
              });
            });
          } else {
            fieldPayload.push({
              fieldKey: field.fieldKey,
              fieldValue: getFieldValue(field.fieldKey, 0),
              rowIndex: 0,
              filledByRole: 'client',
            });
          }
        }
        await orderApi.saveFields(orderId, fieldPayload);
      }

      setSuccess('Заявка успешно оформлена!');
      setTimeout(() => navigate(user?.role === 'client' ? '/my-orders' : '/orders'), 1500);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка при создании заявки');
    }
  };

  const selectedService = services.find(s => s.id === selectedServiceId);
  const inputClass = "w-full px-4 py-3 border border-gray-200 rounded-xl text-gray-900 text-sm outline-none focus:border-[#00B2FF] focus:ring-2 focus:ring-[#00B2FF]/10 transition-all bg-white";
  const selectClass = inputClass + ' cursor-pointer';

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

        <div className="mb-8">
          <h1 className="text-2xl font-bold text-[#0A2E5C]" style={{ margin: 0, fontSize: '1.75rem' }}>
            Новая заявка
          </h1>
          <p className="text-gray-500 text-sm mt-1" style={{ margin: '4px 0 0' }}>
            Заполните форму для подачи заявки на метрологическую услугу
          </p>
        </div>

        {/* Warning banner */}
        <div className="flex items-start gap-3 p-4 bg-amber-50 border border-amber-200 rounded-xl mb-6 text-amber-700 text-sm">
          <svg className="w-5 h-5 shrink-0 mt-0.5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
            <path d="M12 9v4m0 4h.01"/><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
          </svg>
          <span>Данная заявка оформляется только на ваше имя. Если услуга требуется другому лицу, оно должно зарегистрироваться самостоятельно.</span>
        </div>

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

          {user?.role === 'manager' && (
            <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm">
              <p className="text-xs font-semibold text-[#00B2FF] uppercase tracking-wider mb-4" style={{ margin: '0 0 16px' }}>Клиент</p>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Выберите клиента *</label>
                <select value={selectedClientId || ''} onChange={e => setSelectedClientId(parseInt(e.target.value))}
                  className={selectClass} style={{ fontFamily: 'inherit', marginBottom: 0 }}>
                  <option value="">— Выберите клиента —</option>
                  {clients.map(c => <option key={c.id} value={c.id}>{c.fullName} ({c.email})</option>)}
                </select>
              </div>
            </div>
          )}

          {/* Service selection */}
          <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm">
            <p className="text-xs font-semibold text-[#00B2FF] uppercase tracking-wider mb-4" style={{ margin: '0 0 16px' }}>Выберите услугу</p>
            <div className="flex flex-col gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Услуга *</label>
                <select value={selectedServiceId || ''} onChange={e => { setSelectedServiceId(Number(e.target.value)); setSelectedSubserviceId(null); }}
                  required className={selectClass} style={{ fontFamily: 'inherit', marginBottom: 0 }}>
                  <option value="">— Выберите услугу —</option>
                  {services.map(s => <option key={s.id} value={s.id}>{s.name}{s.code ? ` (${s.code})` : ''}</option>)}
                </select>
              </div>
              {selectedService?.description && (
                <div className="bg-[#00B2FF]/5 border-l-4 border-[#00B2FF] rounded-lg p-4">
                  <p className="text-sm text-gray-600 mb-1" style={{ margin: '0 0 4px' }}>
                    <span className="font-semibold text-[#0A2E5C]">Описание: </span>{selectedService.description}
                  </p>
                  <p className="text-sm text-gray-600" style={{ margin: 0 }}>
                    <span className="font-semibold text-[#0A2E5C]">Срок: </span>{selectedService.durationDays} рабочих дней
                  </p>
                </div>
              )}

              {subservice && (
                <div className="bg-blue-50 border border-blue-200 rounded-xl p-4">
                  <p className="text-sm font-semibold text-blue-800 mb-1" style={{ margin: '0 0 4px' }}>
                    Подуслуга: {subservice.fullCode} — {subservice.name}
                  </p>
                  {subservice.description && (
                    <p className="text-xs text-blue-600" style={{ margin: 0 }}>{subservice.description}</p>
                  )}
                </div>
              )}
            </div>
          </div>

          {/* Dynamic fields */}
          {fields.length > 0 && (
            <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm">
              <p className="text-xs font-semibold text-[#00B2FF] uppercase tracking-wider mb-4" style={{ margin: '0 0 16px' }}>
                Данные заявки
              </p>

              {repeatingFields.length > 0 && (
                <div className="flex flex-col gap-4 mb-6">
                  {tableRows.map((rowIndex, i) => (
                    <div key={rowIndex} className="border border-gray-200 rounded-xl p-4 relative">
                      <p className="text-xs font-semibold text-gray-500 mb-3" style={{ margin: '0 0 12px' }}>
                        Прибор #{i + 1}
                      </p>
                      {tableRows.length > 1 && (
                        <button type="button" onClick={() => removeTableRow(rowIndex)}
                          className="absolute top-3 right-3 text-red-400 hover:text-red-600 text-xs border-none bg-transparent cursor-pointer">
                          Удалить
                        </button>
                      )}
                      <div className="flex flex-col gap-3">
                        {repeatingFields.map(field => (
                          <div key={field.fieldKey}>
                            <label className="block text-sm font-medium text-gray-700 mb-1.5">
                              {field.labelRu} {field.required && '*'}
                            </label>
                            {renderField(field, rowIndex)}
                          </div>
                        ))}
                      </div>
                    </div>
                  ))}
                  <button type="button" onClick={addTableRow}
                    className="w-full py-2.5 border-2 border-dashed border-[#00B2FF]/40 text-[#00B2FF] rounded-xl text-sm font-medium hover:border-[#00B2FF] hover:bg-[#00B2FF]/5 transition-all cursor-pointer bg-transparent">
                    + Добавить прибор
                  </button>
                </div>
              )}

              {staticFields.length > 0 && (
                <div className="flex flex-col gap-4">
                    {staticFields.map(field => (
                      <div key={field.fieldKey}>
                        {field.fieldType !== 'checkbox' && (
                          <label className="block text-sm font-medium text-gray-700 mb-1.5">
                            {field.labelRu} {field.required && '*'}
                            {!field.required && <span className="text-gray-400 font-normal"> (при наличии)</span>}
                          </label>
                        )}
                        {renderField(field, 0)}
                      </div>
                    ))}
                </div>
              )}
            </div>
          )}

          <button type="submit"
            className="w-full py-4 bg-[#00B2FF] hover:bg-[#0095D9] text-white font-semibold rounded-xl border-none cursor-pointer text-base transition-colors"
            style={{ marginBottom: 0 }}>
            Оформить заявление
          </button>
        </form>
      </div>
    </div>
  );
}