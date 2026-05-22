import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { orderApi, contractApi, serviceApi, subserviceApi, laboratoryApi } from '../services/api';
import type { Order, Service, Subservice, Laboratory, Contract } from '../types';

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

export default function BuhgalterOrder() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const orderId = parseInt(id || '0');

  const [order, setOrder] = useState<Order | null>(null);
  const [service, setService] = useState<Service | null>(null);
  const [subservice, setSubservice] = useState<Subservice | null>(null);
  const [laboratories, setLaboratories] = useState<Laboratory[]>([]);
  const [contract, setContract] = useState<Contract | null>(null);
  const [fieldValues, setFieldValues] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Бухгалтер fields
  const [buhForm, setBuhForm] = useState({
    labId: '',
    dueDate: '',
    serviceAddress: '',
    responsibleDepartment: '',
    contractNumber: '',
    contractDate: '',
    contractAmount: '',
    contractAmountWords: '',
    signerName: '',
    signerPosition: '',
    signerBasis: '',
    city: 'Астана',
  });

  // Return to revision
  const [showReturnModal, setShowReturnModal] = useState(false);
  const [returnComment, setReturnComment] = useState('');

  useEffect(() => { fetchAll(); }, [orderId]);

  const fetchAll = async () => {
    try {
      setIsLoading(true);
      const [orderRes, labsRes, fieldsRes] = await Promise.all([
        orderApi.getById(orderId),
        laboratoryApi.getAll(),
        orderApi.getFields(orderId),
      ]);
      const o = orderRes.data;
      setOrder(o);
      setLaboratories(labsRes.data);
      setFieldValues(fieldsRes.data);

      // Pre-fill buhgalter fields from order if already set
      setBuhForm(prev => ({
        ...prev,
        labId: o.labId?.toString() || '',
        dueDate: o.dueDate || '',
        serviceAddress: o.serviceAddress || '',
        responsibleDepartment: o.responsibleDepartment || '',
      }));

      // Load service
      if (o.serviceId) {
        serviceApi.getById(o.serviceId).then(res => setService(res.data)).catch(() => {});
      }

      // Load subservice
      if (o.subserviceId) {
        subserviceApi.getById(o.subserviceId).then(res => setSubservice(res.data)).catch(() => {});
      }

      // Load contract
      contractApi.getByOrderId(orderId).then(res => setContract(res.data)).catch(() => {});

    } catch {
      setError('Ошибка при загрузке заявки');
    } finally {
      setIsLoading(false);
    }
  };

  const handleBuhChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setBuhForm(prev => ({ ...prev, [name]: value }));
  };

  const handleSave = async () => {
    try {
      setIsSaving(true);
      await orderApi.update(orderId, {
        labId: buhForm.labId ? parseInt(buhForm.labId) : undefined,
        dueDate: buhForm.dueDate || undefined,
        serviceAddress: buhForm.serviceAddress || undefined,
        responsibleDepartment: buhForm.responsibleDepartment || undefined,
      });
      setSuccess('Сохранено');
      setTimeout(() => setSuccess(''), 2000);
    } catch {
      setError('Ошибка при сохранении');
    } finally {
      setIsSaving(false);
    }
  };

  const handleReturn = async () => {
    if (!returnComment.trim()) return;
    try {
      await orderApi.returnToRevision(orderId, returnComment.trim());
      navigate('/orders');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка при возврате');
    }
  };

  const handleSendForApproval = async () => {
    if (!contract?.contractFileName) {
      setError('Сначала загрузите файл договора');
      return;
    }
    try {
      await contractApi.submit(orderId);
      setSuccess('Отправлено на согласование');
      fetchAll();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка при отправке');
    }
  };

  // Helpers
  const snapshotFields = fieldValues.filter(f => f.filledByRole === 'client_snapshot');
  const formFields = fieldValues.filter(f => f.filledByRole !== 'client_snapshot' && f.fieldValue && !f.fieldValue.startsWith('data:') && f.fieldValue !== 'true');
  const hasFiles = fieldValues.some(f => f.fieldValue?.startsWith('data:'));
  const hasObligation = fieldValues.some(f => f.fieldValue === 'true');

  // Group repeating fields by rowIndex
  const rowGroups: Record<number, any[]> = {};
  const staticFormFields: any[] = [];
  formFields.forEach(f => {
    if (f.rowIndex > 0 || formFields.filter((x: any) => x.fieldKey === f.fieldKey).length > 1) {
      if (!rowGroups[f.rowIndex]) rowGroups[f.rowIndex] = [];
      rowGroups[f.rowIndex].push(f);
    } else {
      staticFormFields.push(f);
    }
  });

  // Generate contract preview text
  const clientName = snapshotFields.find(f => f.fieldKey === 'company_name')?.fieldValue || '___________';
  const clientBin = snapshotFields.find(f => f.fieldKey === 'company_bin')?.fieldValue || '___________';
  const clientDirector = snapshotFields.find(f => f.fieldKey === 'company_director_name')?.fieldValue || '___________';
  const clientPosition = snapshotFields.find(f => f.fieldKey === 'company_director_position')?.fieldValue || '___________';
  const clientIik = snapshotFields.find(f => f.fieldKey === 'company_iik')?.fieldValue || '___________';
  const clientBank = snapshotFields.find(f => f.fieldKey === 'company_bank_name')?.fieldValue || '___________';
  const clientBik = snapshotFields.find(f => f.fieldKey === 'company_bik')?.fieldValue || '___________';
  const clientKbe = snapshotFields.find(f => f.fieldKey === 'company_kbe')?.fieldValue || '___________';
  const clientAddress = snapshotFields.find(f => f.fieldKey === 'company_legal_address')?.fieldValue || '___________';
  const clientPhone = snapshotFields.find(f => f.fieldKey === 'company_phone')?.fieldValue || '___________';

  const inputClass = "w-full px-3 py-2 border border-gray-200 rounded-lg text-gray-900 text-sm outline-none focus:border-[#00B2FF] focus:ring-2 focus:ring-[#00B2FF]/10 transition-all bg-white";
  const selectClass = inputClass + ' cursor-pointer';

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex items-center gap-3 text-gray-400">
          <svg className="w-5 h-5 animate-spin" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
            <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
          </svg>
          Загрузка заявки...
        </div>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <p className="text-gray-400">Заявка не найдена</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-6 px-4">
      <div className="max-w-7xl mx-auto">

        {/* Header */}
        <div className="flex items-center gap-4 mb-6 flex-wrap">
          <button onClick={() => navigate('/orders')}
            className="flex items-center gap-2 text-gray-500 hover:text-gray-700 border-none bg-transparent cursor-pointer text-sm"
            style={{ marginBottom: 0 }}>
            <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <path d="M19 12H5m7-7-7 7 7 7"/>
            </svg>
            Все заявки
          </button>
          <div>
            <h1 className="text-xl font-bold text-[#0A2E5C]" style={{ margin: 0 }}>
              Заявка #{order.orderNumber}
            </h1>
            <p className="text-sm text-gray-500" style={{ margin: '2px 0 0' }}>
              {service?.name}{subservice ? ` — ${subservice.fullCode}` : ''}
            </p>
          </div>
          <span className="ml-auto px-3 py-1 rounded-full text-xs font-semibold bg-slate-100 text-slate-600">
            {order.status}
          </span>
        </div>

        {error && (
          <div className="flex items-center gap-3 p-4 bg-red-50 border border-red-200 rounded-xl mb-4 text-red-600 text-sm">
            <svg className="w-5 h-5 shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10"/><path d="M12 8v4m0 4h.01"/>
            </svg>
            {error}
            <button onClick={() => setError('')} className="ml-auto border-none bg-transparent cursor-pointer text-red-400 text-lg" style={{ marginBottom: 0 }}>×</button>
          </div>
        )}
        {success && (
          <div className="flex items-center gap-3 p-3 bg-green-50 border border-green-200 rounded-xl mb-4 text-green-600 text-sm">
            <svg className="w-4 h-4 shrink-0" fill="none" stroke="currentColor" strokeWidth={2.5} viewBox="0 0 24 24">
              <path d="m9 11 3 3L22 4"/>
            </svg>
            {success}
          </div>
        )}

        {/* Main layout: left form + right preview */}
        <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">

          {/* LEFT PANEL */}
          <div className="flex flex-col gap-4">

            {/* Client submitted data */}
            <div className="bg-white border border-gray-100 rounded-2xl p-5 shadow-sm">
              <p className="text-xs font-semibold text-[#00B2FF] uppercase tracking-wider mb-4" style={{ margin: '0 0 16px' }}>
                Данные из заявления клиента
              </p>

              {/* Snapshot */}
              {snapshotFields.length > 0 && (
                <div className="bg-blue-50 rounded-xl p-3 mb-4">
                  <p className="text-xs font-semibold text-blue-700 mb-2" style={{ margin: '0 0 8px' }}>Реквизиты заявителя</p>
                  {snapshotFields.map((f, i) => (
                    <div key={i} className="flex justify-between items-start gap-3 py-1 border-b border-blue-100 last:border-0">
                      <span className="text-xs text-blue-500">{fieldLabels[f.fieldKey] || f.fieldKey}</span>
                      <span className="text-xs text-blue-800 text-right max-w-[60%]">{f.fieldValue}</span>
                    </div>
                  ))}
                </div>
              )}

              {/* Device rows */}
              {Object.entries(rowGroups).map(([rowIdx, rowFields]) => (
                <div key={rowIdx} className="bg-gray-50 rounded-xl p-3 mb-3">
                  <p className="text-xs font-semibold text-gray-500 mb-2" style={{ margin: '0 0 8px' }}>Прибор #{parseInt(rowIdx) + 1}</p>
                  {(rowFields as any[]).map((f, i) => (
                    <div key={i} className="flex justify-between items-start gap-3 py-1 border-b border-gray-100 last:border-0">
                      <span className="text-xs text-gray-400">{fieldLabels[f.fieldKey] || f.fieldKey}</span>
                      <span className="text-xs text-gray-700 text-right max-w-[60%]">{f.fieldValue}</span>
                    </div>
                  ))}
                </div>
              ))}

              {/* Static form fields */}
              {staticFormFields.map((f, i) => (
                <div key={i} className="flex justify-between items-start gap-3 py-1.5 border-b border-gray-50">
                  <span className="text-xs text-gray-400">{fieldLabels[f.fieldKey] || f.fieldKey}</span>
                  <span className="text-xs text-gray-700 text-right max-w-[60%]">{f.fieldValue}</span>
                </div>
              ))}

              {hasFiles && <p className="text-xs text-blue-600 mt-2">📎 Документы прикреплены клиентом</p>}
              {hasObligation && <p className="text-xs text-green-600 mt-1">✓ Обязательство подтверждено клиентом</p>}
            </div>

            {/* Бухгалтер fields */}
            <div className="bg-white border border-gray-100 rounded-2xl p-5 shadow-sm">
              <p className="text-xs font-semibold text-[#00B2FF] uppercase tracking-wider mb-4" style={{ margin: '0 0 16px' }}>
                Заполняется бухгалтером
              </p>
              <div className="flex flex-col gap-3">

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-medium text-gray-600 mb-1">Номер договора *</label>
                    <input type="text" name="contractNumber" value={buhForm.contractNumber}
                      onChange={handleBuhChange} placeholder="1405/М"
                      className={inputClass} style={{ fontFamily: 'inherit', marginBottom: 0 }} />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-600 mb-1">Дата договора *</label>
                    <input type="date" name="contractDate" value={buhForm.contractDate}
                      onChange={handleBuhChange}
                      className={inputClass} style={{ fontFamily: 'inherit', marginBottom: 0 }} />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-medium text-gray-600 mb-1">Город</label>
                    <input type="text" name="city" value={buhForm.city}
                      onChange={handleBuhChange} placeholder="Астана"
                      className={inputClass} style={{ fontFamily: 'inherit', marginBottom: 0 }} />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-600 mb-1">Плановая дата сдачи</label>
                    <input type="date" name="dueDate" value={buhForm.dueDate}
                      onChange={handleBuhChange}
                      className={inputClass} style={{ fontFamily: 'inherit', marginBottom: 0 }} />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Лаборатория</label>
                  <select name="labId" value={buhForm.labId} onChange={handleBuhChange}
                    className={selectClass} style={{ fontFamily: 'inherit', marginBottom: 0 }}>
                    <option value="">— Выберите лабораторию —</option>
                    {laboratories.map(lab => (
                      <option key={lab.id} value={lab.id}>{lab.name} {lab.city ? `(${lab.city})` : ''}</option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Адрес проведения услуги</label>
                  <input type="text" name="serviceAddress" value={buhForm.serviceAddress}
                    onChange={handleBuhChange} placeholder="г. Астана, ул. Мәңгілік ел, 11"
                    className={inputClass} style={{ fontFamily: 'inherit', marginBottom: 0 }} />
                </div>

                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Ответственный отдел</label>
                  <input type="text" name="responsibleDepartment" value={buhForm.responsibleDepartment}
                    onChange={handleBuhChange} placeholder="Отдел испытаний СИ"
                    className={inputClass} style={{ fontFamily: 'inherit', marginBottom: 0 }} />
                </div>

                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Сумма договора (цифрами)</label>
                  <input type="number" name="contractAmount" value={buhForm.contractAmount}
                    onChange={handleBuhChange} placeholder="734383.00"
                    className={inputClass} style={{ fontFamily: 'inherit', marginBottom: 0 }} />
                </div>

                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Сумма прописью</label>
                  <input type="text" name="contractAmountWords" value={buhForm.contractAmountWords}
                    onChange={handleBuhChange} placeholder="Семьсот тридцать четыре тысячи триста восемьдесят три тенге"
                    className={inputClass} style={{ fontFamily: 'inherit', marginBottom: 0 }} />
                </div>

                <p className="text-xs font-semibold text-gray-500 mt-1" style={{ margin: '4px 0 0' }}>Подписант от КСМ</p>

                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">ФИО подписанта</label>
                  <input type="text" name="signerName" value={buhForm.signerName}
                    onChange={handleBuhChange} placeholder="Мухамеджанов Б.Ж."
                    className={inputClass} style={{ fontFamily: 'inherit', marginBottom: 0 }} />
                </div>

                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Должность подписанта</label>
                  <input type="text" name="signerPosition" value={buhForm.signerPosition}
                    onChange={handleBuhChange} placeholder="Заместитель генерального директора"
                    className={inputClass} style={{ fontFamily: 'inherit', marginBottom: 0 }} />
                </div>

                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Действует на основании</label>
                  <input type="text" name="signerBasis" value={buhForm.signerBasis}
                    onChange={handleBuhChange} placeholder="доверенности от 30 декабря 2022 года № 90"
                    className={inputClass} style={{ fontFamily: 'inherit', marginBottom: 0 }} />
                </div>

              </div>
            </div>

            {/* Actions */}
            <div className="bg-white border border-gray-100 rounded-2xl p-5 shadow-sm flex flex-wrap gap-3">
              <button onClick={handleSave} disabled={isSaving}
                className="flex-1 py-2.5 bg-[#00B2FF] hover:bg-[#0095D9] text-white font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors"
                style={{ marginBottom: 0 }}>
                {isSaving ? 'Сохранение...' : 'Сохранить'}
              </button>
              <button onClick={() => setShowReturnModal(true)}
                className="flex-1 py-2.5 bg-orange-50 hover:bg-orange-100 text-orange-700 font-semibold rounded-xl border border-orange-200 cursor-pointer text-sm transition-colors"
                style={{ marginBottom: 0 }}>
                Вернуть клиенту
              </button>
              <button onClick={handleSendForApproval}
                className="flex-1 py-2.5 bg-[#0A2E5C] hover:bg-[#0d3a73] text-white font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors"
                style={{ marginBottom: 0 }}>
                На согласование
              </button>
            </div>
          </div>

          {/* RIGHT PANEL — Live contract preview */}
          <div className="bg-white border border-gray-100 rounded-2xl shadow-sm overflow-hidden">
            <div className="px-5 py-4 border-b border-gray-100 flex items-center justify-between">
              <p className="text-xs font-semibold text-[#00B2FF] uppercase tracking-wider" style={{ margin: 0 }}>
                Предпросмотр договора
              </p>
              <span className="text-xs text-gray-400">Обновляется в реальном времени</span>
            </div>
            <div className="p-6 overflow-y-auto max-h-[calc(100vh-200px)]">
              <div className="font-serif text-sm leading-relaxed text-gray-800" style={{ fontFamily: 'Times New Roman, serif' }}>

                {/* Contract header */}
                <div className="flex justify-between mb-6">
                  <span>г. {buhForm.city || '___________'}</span>
                  <span><strong>{buhForm.contractDate ? new Date(buhForm.contractDate).toLocaleDateString('ru-RU') : '«___» ___________ 20__ г.'}</strong></span>
                </div>

                <p className="text-center font-bold mb-4" style={{ margin: '0 0 16px' }}>
                  ДОГОВОР № {buhForm.contractNumber || '___/М'} на оказание метрологических услуг
                </p>

                <p className="mb-4 text-justify" style={{ margin: '0 0 16px' }}>
                  <strong>Республиканское государственное предприятие «Казахстанский центр метрологии»</strong> Комитета технического регулирования и метрологии Министерства торговли и интеграции Республики Казахстан (далее – РГП «КЦМ»), именуемое в дальнейшем «Исполнитель», в лице <strong>{buhForm.signerPosition || '___________'} {buhForm.signerName || '___________'}</strong>, действующего на основании <strong>{buhForm.signerBasis || '___________'}</strong>, с одной стороны, <strong>{clientName}</strong>, именуемое в дальнейшем «Заказчик», в лице {clientPosition} {clientDirector}, действующего на основании Устава, с другой стороны, заключили настоящий договор о нижеследующем:
                </p>

                <p className="font-bold mb-2" style={{ margin: '0 0 8px' }}>1. Предмет договора</p>
                <p className="mb-4 text-justify" style={{ margin: '0 0 16px' }}>
                  1.1 Исполнитель обязуется оказать Услугу(и) согласно условиям, требованиям и по ценам, указанным в приложении к настоящему Договору, а Заказчик обязуется выполнить условия настоящего Договора и произвести оплату на условиях настоящего Договора.
                </p>

                <p className="font-bold mb-2" style={{ margin: '0 0 8px' }}>2. Сумма Договора и условия оплаты</p>
                <p className="mb-4 text-justify" style={{ margin: '0 0 16px' }}>
                  2.1 Общая сумма Договора составляет <strong>{buhForm.contractAmount ? `${parseFloat(buhForm.contractAmount).toLocaleString('ru-RU', { minimumFractionDigits: 2 })}` : '___________'}</strong> ({buhForm.contractAmountWords || '___________'}) тенге, в том числе НДС. Оплата производится Заказчиком в течение 5 (пяти) банковских дней с момента подписания настоящего договора.
                </p>

                {/* Sections 3-10 abbreviated */}
                {['3. Обязательства Сторон', '4. Оказание Услуг', '5. Ответственность сторон', '6. Условия расторжения договора', '7. Антикоррупционные условия договора', '8. Форс-мажор', '9. Решение спорных вопросов', '10. Срок действия договора'].map((section, i) => (
                  <div key={i} className="mb-3">
                    <p className="font-bold text-xs text-gray-400" style={{ margin: '0 0 4px' }}>{section}</p>
                    <p className="text-xs text-gray-300 italic">[ стандартные условия договора ]</p>
                  </div>
                ))}

                <p className="font-bold mb-3 mt-4" style={{ margin: '16px 0 12px' }}>11. Реквизиты Сторон</p>
                <div className="grid grid-cols-2 gap-4 text-xs">
                  <div className="border border-gray-200 rounded p-3">
                    <p className="font-bold mb-1" style={{ margin: '0 0 4px' }}>«Исполнитель»: РГП «КЦМ»</p>
                    <p style={{ margin: '0 0 2px' }}>г. Астана, пр. Мәңгілік ел, 11</p>
                    <p style={{ margin: '0 0 2px' }}>БИН: 201040035452</p>
                    <p style={{ margin: '0 0 2px' }}>ИИК: KZ396018821001012411</p>
                    <p style={{ margin: '0 0 8px' }}>АО «Народный Банк Казахстана»</p>
                    <p style={{ margin: '0 0 2px' }}>от «Исполнителя»:</p>
                    <p style={{ margin: '0 0 2px' }}>{buhForm.signerPosition || '___________'}</p>
                    <p style={{ margin: '0 0 2px' }}>________________ {buhForm.signerName || '___________'}</p>
                  </div>
                  <div className="border border-gray-200 rounded p-3">
                    <p className="font-bold mb-1" style={{ margin: '0 0 4px' }}>«Заказчик»: {clientName}</p>
                    <p style={{ margin: '0 0 2px' }}>{clientAddress}</p>
                    <p style={{ margin: '0 0 2px' }}>Тел.: {clientPhone}</p>
                    <p style={{ margin: '0 0 2px' }}>БИН: {clientBin}</p>
                    <p style={{ margin: '0 0 2px' }}>ИИК: {clientIik}</p>
                    <p style={{ margin: '0 0 2px' }}>в {clientBank}</p>
                    <p style={{ margin: '0 0 2px' }}>БИК: {clientBik}</p>
                    <p style={{ margin: '0 0 8px' }}>КБЕ: {clientKbe}</p>
                    <p style={{ margin: '0 0 2px' }}>от «Заказчика»:</p>
                    <p style={{ margin: '0 0 2px' }}>{clientPosition}</p>
                    <p style={{ margin: '0 0 2px' }}>________________ {clientDirector}</p>
                  </div>
                </div>

                {/* Technical Specification preview */}
                <div className="mt-6 pt-4 border-t border-gray-200">
                  <p className="text-center font-bold mb-2" style={{ margin: '0 0 8px' }}>Приложение 1</p>
                  <p className="text-center font-bold mb-4" style={{ margin: '0 0 16px' }}>
                    к Договору № {buhForm.contractNumber || '___/М'} от {buhForm.contractDate ? new Date(buhForm.contractDate).toLocaleDateString('ru-RU') : '___'}
                  </p>
                  <p className="text-center font-bold mb-4" style={{ margin: '0 0 16px' }}>
                    Техническая спецификация<br/>
                    на {service?.name?.toLowerCase() || '___________'}
                  </p>

                  <p className="font-bold mb-1" style={{ margin: '0 0 4px' }}>1. Предмет оказания услуг:</p>
                  <p className="mb-3" style={{ margin: '0 0 12px' }}>{service?.name || '___________'}</p>

                  <p className="font-bold mb-1" style={{ margin: '0 0 4px' }}>2. Место оказания услуг:</p>
                  <p className="mb-3" style={{ margin: '0 0 12px' }}>{buhForm.serviceAddress || '___________'}</p>

                  <p className="font-bold mb-1" style={{ margin: '0 0 4px' }}>3. Срок оказания услуг:</p>
                  <p className="mb-3" style={{ margin: '0 0 12px' }}>
                    {buhForm.dueDate ? `до ${new Date(buhForm.dueDate).toLocaleDateString('ru-RU')}` : '___________'}
                  </p>

                  <p className="font-bold mb-2" style={{ margin: '0 0 8px' }}>4. Объем оказания услуг:</p>
                  <table className="w-full border-collapse text-xs mb-4">
                    <thead>
                      <tr className="bg-gray-50">
                        <th className="border border-gray-300 px-2 py-1 text-left">№</th>
                        <th className="border border-gray-300 px-2 py-1 text-left">Наименование СИ</th>
                        <th className="border border-gray-300 px-2 py-1 text-left">Производитель</th>
                        <th className="border border-gray-300 px-2 py-1 text-center">Кол-во</th>
                        <th className="border border-gray-300 px-2 py-1 text-right">Стоимость, тенге</th>
                      </tr>
                    </thead>
                    <tbody>
                      {Object.entries(rowGroups).length > 0 ? Object.entries(rowGroups).map(([rowIdx, rowFields]) => {
                        const getName = (key: string) => (rowFields as any[]).find(f => f.fieldKey === key)?.fieldValue || '—';
                        return (
                          <tr key={rowIdx}>
                            <td className="border border-gray-300 px-2 py-1">{parseInt(rowIdx) + 1}</td>
                            <td className="border border-gray-300 px-2 py-1">{getName('si_name')}</td>
                            <td className="border border-gray-300 px-2 py-1">{getName('producer')}</td>
                            <td className="border border-gray-300 px-2 py-1 text-center">{getName('quantity') || '1'}</td>
                            <td className="border border-gray-300 px-2 py-1 text-right text-gray-400 italic">—</td>
                          </tr>
                        );
                      }) : (
                        <tr>
                          <td colSpan={5} className="border border-gray-300 px-2 py-2 text-center text-gray-400">
                            Нет данных об устройствах
                          </td>
                        </tr>
                      )}
                      <tr className="bg-gray-50 font-bold">
                        <td colSpan={4} className="border border-gray-300 px-2 py-1 text-right">Итого:</td>
                        <td className="border border-gray-300 px-2 py-1 text-right">
                          {buhForm.contractAmount ? `${parseFloat(buhForm.contractAmount).toLocaleString('ru-RU', { minimumFractionDigits: 2 })}` : '—'}
                        </td>
                      </tr>
                    </tbody>
                  </table>

                  <div className="grid grid-cols-2 gap-4 text-xs mt-4">
                    <div>
                      <p style={{ margin: '0 0 2px' }}>от «Исполнителя»:</p>
                      <p style={{ margin: '0 0 2px' }}>{buhForm.signerPosition || '___________'}</p>
                      <p style={{ margin: '0 0 2px' }}>________________ {buhForm.signerName || '___________'}</p>
                      <p style={{ margin: 0 }}>М.П.</p>
                    </div>
                    <div>
                      <p style={{ margin: '0 0 2px' }}>от «Заказчика»:</p>
                      <p style={{ margin: '0 0 2px' }}>{clientPosition}</p>
                      <p style={{ margin: '0 0 2px' }}>________________ {clientDirector}</p>
                      <p style={{ margin: 0 }}>М.П.</p>
                    </div>
                  </div>
                </div>

              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Return modal */}
      {showReturnModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 px-4">
          <div className="bg-white rounded-2xl shadow-2xl p-8 w-full max-w-lg">
            <h2 className="font-bold text-[#0A2E5C] mb-2" style={{ margin: '0 0 8px', fontSize: '1.25rem' }}>
              Вернуть на доработку
            </h2>
            <p className="text-sm text-gray-500 mb-6" style={{ margin: '0 0 24px' }}>
              Укажите что нужно исправить клиенту.
            </p>
            <textarea value={returnComment} onChange={e => setReturnComment(e.target.value)}
              rows={4} placeholder="Причина возврата..."
              className="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm bg-white outline-none focus:border-orange-400 resize-none"
              style={{ fontFamily: 'inherit', marginBottom: '16px' }} autoFocus />
            <div className="flex gap-3">
              <button onClick={handleReturn} disabled={!returnComment.trim()}
                className="flex-1 py-3 bg-orange-500 hover:bg-orange-600 disabled:bg-orange-200 text-white font-semibold rounded-xl border-none cursor-pointer text-sm"
                style={{ marginBottom: 0 }}>
                Вернуть
              </button>
              <button onClick={() => { setShowReturnModal(false); setReturnComment(''); }}
                className="flex-1 py-3 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold rounded-xl border-none cursor-pointer text-sm"
                style={{ marginBottom: 0 }}>
                Отмена
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}