import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useEffect } from 'react';
import { useAuthStore } from './store/authStore';
import { ProtectedRoute } from './components/ProtectedRoute';
import Header from './components/Header';
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import Catalog from './pages/Catalog';
import CreateOrder from './pages/CreateOrder';
import MyOrders from './pages/MyOrders';
import Profile from './pages/Profile';
import Queue from './pages/Queue';
import NotFound from './components/NotFound';
import Dashboard from './pages/Dashboard';
import Reports from './pages/Reports';
import AdminUsers from './pages/AdminUsers';
import './App.css';

function App() {
  const { loadFromStorage, isAuthenticated } = useAuthStore();

  useEffect(() => {
    loadFromStorage();
  }, []);

  return (
    <BrowserRouter>
      {isAuthenticated && <Header />}
      
      <main>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          <Route
            path="/catalog"
            element={
              <ProtectedRoute>
                <Catalog />
              </ProtectedRoute>
            }
          />

          <Route
            path="/create-order"
            element={
              <ProtectedRoute requiredRoles={['client']}>
                <CreateOrder />
              </ProtectedRoute>
            }
          />

          <Route
            path="/my-orders"
            element={
              <ProtectedRoute requiredRoles={['client']}>
                <MyOrders />
              </ProtectedRoute>
            }
          />

          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <Profile />
              </ProtectedRoute>
            }
          />

          <Route
            path="/queue"
            element={
              <ProtectedRoute requiredRoles={['metrolog', 'manager']}>
                <Queue />
              </ProtectedRoute>
            }
          />

          <Route
            path="/dashboard"
            element={
              <ProtectedRoute requiredRoles={['manager']}>
                <Dashboard />
              </ProtectedRoute>
            }
          />

          <Route
            path="/reports"
            element={
              <ProtectedRoute requiredRoles={['manager']}>
                <Reports />
              </ProtectedRoute>
            }
          />

          <Route
            path="/admin/users"
            element={
              <ProtectedRoute requiredRoles={['admin']}>
                <AdminUsers />
              </ProtectedRoute>
            }
          />

          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password" element={<ResetPassword />} />

          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/403" element={<NotFound code={403} />} />
          <Route path="*" element={<NotFound code={404} />} />
        </Routes>
      </main>
    </BrowserRouter>
  );
}

export default App;