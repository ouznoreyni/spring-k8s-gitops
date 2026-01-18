import { Menu, X, LogOut, User, LayoutDashboard } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useUIStore } from '../../store/uiStore';
import { useAuthStore } from '../../store/authStore';
import { Button } from '../ui/Button';

export const Header = () => {
  const { isMenuOpen, toggleMenu, closeMenu } = useUIStore();
  const { isAuthenticated, user, isAdmin, logout } = useAuthStore();

  return (
    <header className="fixed top-0 left-0 right-0 bg-white/80 backdrop-blur-md z-50 border-b border-gray-100">
      <nav className="max-w-7xl mx-auto px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <Link to="/" className="flex items-center space-x-2" onClick={closeMenu}>
            <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-lg flex items-center justify-center">
              <span className="text-white font-bold text-sm">M</span>
            </div>
            <span className="text-xl font-semibold text-gray-900">ModernBlog</span>
          </Link>

          <div className="hidden md:flex items-center space-x-8">
            <Link to="/" className="text-gray-600 hover:text-gray-900 transition-colors">
              Accueil
            </Link>
            <Link to="/articles" className="text-gray-600 hover:text-gray-900 transition-colors">
              Articles
            </Link>
            {isAdmin && (
              <Link to="/admin" className="text-blue-600 hover:text-blue-700 font-semibold transition-colors flex items-center gap-1.5">
                <LayoutDashboard size={18} />
                Admin
              </Link>
            )}
          </div>

          <div className="hidden md:flex items-center space-x-4">
            {isAuthenticated ? (
              <div className="flex items-center gap-4">
                <div className="flex items-center gap-2 text-sm text-gray-700 bg-gray-50 px-3 py-1.5 rounded-full border border-gray-100">
                  <User size={16} className="text-blue-500" />
                  <span className="font-medium">{user?.email}</span>
                </div>
                <Button variant="ghost" size="sm" onClick={logout} className="gap-2">
                  <LogOut size={18} />
                  Déconnexion
                </Button>
              </div>
            ) : (
              <>
                <Link to="/login">
                  <Button variant="ghost">
                    Connexion
                  </Button>
                </Link>
                <Link to="/register">
                  <Button variant="primary">
                    S'inscrire
                  </Button>
                </Link>
              </>
            )}
          </div>

          <button
            className="md:hidden text-gray-600"
            onClick={toggleMenu}
          >
            {isMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>

        {isMenuOpen && (
          <div className="md:hidden py-4 space-y-4 border-t border-gray-100">
            <Link to="/" className="block text-gray-600 hover:text-gray-900" onClick={closeMenu}>
              Accueil
            </Link>
            <Link to="/articles" className="block text-gray-600 hover:text-gray-900" onClick={closeMenu}>
              Articles
            </Link>
            {isAdmin && (
              <Link to="/admin" className="block text-blue-600 hover:text-blue-700 font-semibold" onClick={closeMenu}>
                Admin
              </Link>
            )}
            <div className="flex flex-col space-y-2 pt-4 border-t border-gray-100">
              {isAuthenticated ? (
                <>
                  <div className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700">
                    <User size={16} className="text-blue-500" />
                    <span className="font-medium truncate">{user?.email}</span>
                  </div>
                  <Button variant="ghost" className="justify-start px-4 gap-2" onClick={() => { logout(); closeMenu(); }}>
                    <LogOut size={18} />
                    Déconnexion
                  </Button>
                </>
              ) : (
                <>
                  <Link to="/login" onClick={closeMenu}>
                    <Button variant="ghost" className="justify-start px-4 w-full">
                      Connexion
                    </Button>
                  </Link>
                  <Link to="/register" onClick={closeMenu}>
                    <Button variant="primary" className="justify-start px-4">
                      S'inscrire
                    </Button>
                  </Link>
                </>
              )}
            </div>
          </div>
        )}
      </nav>
    </header>
  );
};
