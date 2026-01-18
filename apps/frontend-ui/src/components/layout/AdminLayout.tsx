import { NavLink, Outlet } from 'react-router-dom';
import { 
  LayoutDashboard, 
  FileText, 
  Users, 
  LogOut,
  ChevronRight
} from 'lucide-react';
import { useAuthStore } from '../../store/authStore';
import { Header } from './Header';
import { cn } from '../../lib/utils';

export const AdminLayout = () => {
  const logout = useAuthStore((state) => state.logout);

  const menuItems = [
    { icon: LayoutDashboard, label: 'Dashboard', path: '/admin', end: true },
    { icon: FileText, label: 'Articles', path: '/admin/articles' },
    { icon: Users, label: 'Utilisateurs', path: '/admin/users' },
  ];

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Header />
      
      <div className="flex flex-grow pt-16 h-full">
        {/* Sidebar */}
        <aside className="fixed left-0 top-16 bottom-0 w-64 bg-white border-r border-gray-100 hidden md:flex flex-col z-40">
          <div className="flex-grow py-8 px-4 space-y-2">
            {menuItems.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                end={item.end}
                className={({ isActive }) => cn(
                  "flex items-center justify-between px-4 py-3 rounded-xl transition-all duration-200 group",
                  isActive 
                    ? "bg-blue-50 text-blue-600" 
                    : "text-gray-600 hover:bg-gray-50 hover:text-gray-900"
                )}
              >
                <div className="flex items-center gap-3">
                  <item.icon size={20} className={cn(
                    "transition-colors",
                    "group-hover:text-blue-500"
                  )} />
                  <span className="font-medium">{item.label}</span>
                </div>
                <ChevronRight size={16} className={cn(
                  "opacity-0 transition-all",
                  "group-hover:opacity-100"
                )} />
              </NavLink>
            ))}
          </div>

          <div className="p-4 border-t border-gray-50">
            <button
              onClick={logout}
              className="flex items-center gap-3 w-full px-4 py-3 text-gray-600 hover:text-red-600 hover:bg-red-50 rounded-xl transition-all duration-200 group"
            >
              <LogOut size={20} className="group-hover:text-red-500" />
              <span className="font-medium">Déconnexion</span>
            </button>
          </div>
        </aside>

        {/* Mobile Nav (Bottom/Top overlay would be too much, let's just make main content aware of sidebar) */}
        
        {/* Main Content */}
        <main className="flex-grow md:ml-64 p-6 lg:p-10">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
