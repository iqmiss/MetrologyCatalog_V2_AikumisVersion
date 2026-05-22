import { useState, useEffect } from 'react';
import { useAuthStore } from '../store/authStore';
import { userApi } from '../services/api';
import type { User, Company } from '../types';

export default function Profile() {
  const { user, logout } = useAuthStore();
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [company, setCompany] = useState<Company | null>(null);

  const [formData, setFormData] = useState({
    fullName: user?.fullName || '',
    email: user?.email || '',
    phone: user?.phone || '',
    iin: user?.iin || '',
  });

  const [companyForm, setCompanyForm] = useState({
    directorName: '',
    directorPosition: '',
    iik: '',
    bankName: '',
    bik: '',
    kbe: '',
    legalAddress: '',
    address: '',
    phone: '',
  });

  useEffect(() => {
    if (!user) return;
    userApi.getProfile(user?.id).then((res: any) => {
      const data = res.data;
      if (data.company) {
        setCompany(data.company);
        setCompanyForm({
          directorName: data.company.directorName || '',
          directorPosition: data.company.directorPosition || '',
          iik: data.company.iik || '',
          bankName: data.company.bankName || '',
          bik: data.company.bik || '',
          kbe: data.company.kbe || '',
          legalAddress: data.company.legalAddress || '',
          address: data.company.address || '',
          phone: data.company.phone || '',
        });
      }
    }).catch(() => {});
  }, [user]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleCompanyChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setCompanyForm(prev => ({ ...prev, [name]: value }));
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    if (!formData.fullName || !formData.email) {
      setError('Заполните обязательные поля');
      return;
    }
    try {
      setIsSaving(true);
      await userApi.updateProfile({
        id: user?.id,
        ...formData,
        company: user?.companyId ? companyForm : undefined,
      });
      setSuccess('Профиль обновлён успешно!');
      setIsEditing(false);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка при обновлении профиля');
    } finally {
      setIsSaving(false);
    }
  };

  const roleLabels: Record<string, string> = {
    client: 'Клиент', metrolog: 'Метролог', manager: 'Менеджер',
    director: 'Директор', financier: 'Финансист', approver: 'Согласующий',
    admin: 'Администратор', gen_director: 'Ген.директор', yurist: 'Юрист',
  };

  const inputClass = "w-full px-4 py-3 border border-gray-200 rounded-xl text-gray-900 text-sm outline-none focus:border-[#00B2FF] focus:ring-2 focus:ring-[#00B2FF]/10 transition-all bg-white";

  return (
    <div className="min-h-screen bg-gray-50 py-10 px-4">
      <div className="max-w-2xl mx-auto">

        <div className="mb-8">
          <h1 className="text-2xl font-bold text-[#0A2E5C]" style={{ margin: 0, fontSize: '1.75rem' }}>Мой профиль</h1>
          <p className="text-gray-500 text-sm mt-1" style={{ margin: '4px 0 0' }}>Управляйте личными данными и реквизитами организации</p>
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

        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">

          {/* Header */}
          <div className="px-8 py-6 border-b border-gray-100" style={{ background: 'linear-gradient(135deg, #0A2E5C 0%, #1E4A7C 100%)' }}>
            <div className="flex items-center gap-5">
              <div className="w-16 h-16 bg-[#00B2FF] rounded-2xl flex items-center justify-center text-white text-2xl font-bold shrink-0">
                {user?.fullName?.charAt(0).toUpperCase() || 'U'}
              </div>
              <div>
                <h2 className="text-xl font-bold text-white" style={{ margin: '0 0 6px', fontSize: '1.25rem' }}>{user?.fullName}</h2>
                <span className="text-xs font-semibold px-3 py-1 rounded-full bg-white/20 text-white">
                  {roleLabels[user?.role || 'client']}
                </span>
              </div>
              <div className="ml-auto text-right">
                <p className="text-white/40 text-xs" style={{ margin: 0 }}>ID профиля</p>
                <p className="text-white/70 text-sm font-mono" style={{ margin: '2px 0 0' }}>#{user?.id}</p>
              </div>
            </div>
          </div>

          <div className="px-8 py-6">
            {!isEditing ? (
              <>
                {/* Personal info */}
                <div className="mb-6">
                  <p className="text-xs font-semibold text-[#00B2FF] uppercase tracking-wider mb-4" style={{ margin: '0 0 16px' }}>Личные данные</p>
                  <div className="flex flex-col gap-0">
                    {[
                      { label: 'ФИО', value: user?.fullName },
                      { label: 'Email', value: user?.email },
                      { label: 'Телефон', value: user?.phone || 'Не указан' },
                      { label: 'ИИН', value: user?.iin || 'Не указан' },
                      { label: 'Роль', value: roleLabels[user?.role || 'client'] },
                      { label: 'Статус', value: user?.isActive ? 'Активен' : 'Неактивен' },
                    ].map(item => (
                      <div key={item.label} className="flex justify-between items-center py-3 border-b border-gray-50">
                        <span className="text-sm text-gray-400">{item.label}</span>
                        <span className="text-sm font-medium text-gray-800">{item.value}</span>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Company info */}
                {company && (
                  <div className="mb-8">
                    <p className="text-xs font-semibold text-[#00B2FF] uppercase tracking-wider mb-4" style={{ margin: '0 0 16px' }}>Реквизиты организации</p>
                    <div className="flex flex-col gap-0">
                      {[
                        { label: 'Наименование', value: company.name },
                        { label: 'БИН', value: company.bin },
                        { label: 'Руководитель', value: company.directorName || 'Не указан' },
                        { label: 'Должность', value: company.directorPosition || 'Не указана' },
                        { label: 'ИИК', value: company.iik || 'Не указан' },
                        { label: 'Банк', value: company.bankName || 'Не указан' },
                        { label: 'БИК', value: company.bik || 'Не указан' },
                        { label: 'КБЕ', value: company.kbe || 'Не указан' },
                        { label: 'Юр. адрес', value: company.legalAddress || 'Не указан' },
                        { label: 'Факт. адрес', value: company.address || 'Не указан' },
                        { label: 'Телефон орг.', value: company.phone || 'Не указан' },
                      ].map(item => (
                        <div key={item.label} className="flex justify-between items-center py-3 border-b border-gray-50">
                          <span className="text-sm text-gray-400">{item.label}</span>
                          <span className="text-sm font-medium text-gray-800 text-right max-w-[60%]">{item.value}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                <div className="flex gap-3">
                  <button onClick={() => setIsEditing(true)}
                    className="flex-1 py-3 bg-[#00B2FF] hover:bg-[#0095D9] text-white font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors"
                    style={{ marginBottom: 0 }}>
                    Редактировать
                  </button>
                  <button onClick={() => { logout(); window.location.href = '/login'; }}
                    className="flex-1 py-3 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors"
                    style={{ marginBottom: 0 }}>
                    Выйти
                  </button>
                </div>
              </>
            ) : (
              <form onSubmit={handleSave} className="flex flex-col gap-5">

                <p className="text-xs font-semibold text-[#00B2FF] uppercase tracking-wider" style={{ margin: '0 0 4px' }}>Личные данные</p>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">ФИО *</label>
                  <input type="text" name="fullName" value={formData.fullName} onChange={handleChange}
                    required className={inputClass} style={{ fontFamily: 'inherit', marginBottom: 0 }} />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Email *</label>
                  <input type="email" name="email" value={formData.email} onChange={handleChange}
                    required className={inputClass} style={{ fontFamily: 'inherit', marginBottom: 0 }} />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Телефон</label>
                  <input type="tel" name="phone" value={formData.phone} onChange={handleChange}
                    placeholder="+7 (777) 123-45-67" className={inputClass}
                    style={{ fontFamily: 'inherit', marginBottom: 0 }} />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">ИИН</label>
                  <input type="text" name="iin" value={formData.iin} onChange={handleChange}
                    placeholder="123456789012" maxLength={12} className={inputClass}
                    style={{ fontFamily: 'inherit', marginBottom: 0 }} />
                </div>

                {/* Company fields for clients */}
                {company && (
                  <>
                    <p className="text-xs font-semibold text-[#00B2FF] uppercase tracking-wider mt-2" style={{ margin: '8px 0 4px' }}>
                      Реквизиты организации
                    </p>
                    <div className="bg-amber-50 border border-amber-200 rounded-xl p-3 text-xs text-amber-700">
                      Эти данные используются для автозаполнения договоров. Заполните точно как в регистрационных документах.
                    </div>
                    {[
                      { label: 'ФИО руководителя', name: 'directorName', placeholder: 'Иванов Иван Иванович' },
                      { label: 'Должность руководителя', name: 'directorPosition', placeholder: 'Директор' },
                      { label: 'ИИК (расчётный счёт)', name: 'iik', placeholder: 'KZ...' },
                      { label: 'Наименование банка', name: 'bankName', placeholder: 'АО «Народный Банк Казахстана»' },
                      { label: 'БИК', name: 'bik', placeholder: 'HSBKKZKX' },
                      { label: 'КБЕ', name: 'kbe', placeholder: '17' },
                      { label: 'Юридический адрес', name: 'legalAddress', placeholder: 'г. Алматы, ул. ...' },
                      { label: 'Фактический адрес', name: 'address', placeholder: 'г. Алматы, ул. ...' },
                      { label: 'Телефон организации', name: 'phone', placeholder: '+7 (727) 123-45-67' },
                    ].map(field => (
                      <div key={field.name}>
                        <label className="block text-sm font-medium text-gray-700 mb-1.5">{field.label}</label>
                        <input type="text" name={field.name}
                          value={companyForm[field.name as keyof typeof companyForm]}
                          onChange={handleCompanyChange}
                          placeholder={field.placeholder}
                          className={inputClass} style={{ fontFamily: 'inherit', marginBottom: 0 }} />
                      </div>
                    ))}
                  </>
                )}

                <div className="flex gap-3 mt-2">
                  <button type="submit" disabled={isSaving}
                    className="flex-1 py-3 bg-[#00B2FF] hover:bg-[#0095D9] text-white font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors"
                    style={{ marginBottom: 0 }}>
                    {isSaving ? 'Сохранение...' : 'Сохранить'}
                  </button>
                  <button type="button" disabled={isSaving}
                    onClick={() => setIsEditing(false)}
                    className="flex-1 py-3 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold rounded-xl border-none cursor-pointer text-sm transition-colors"
                    style={{ marginBottom: 0 }}>
                    Отменить
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}