export const Footer = () => {
  return (
    <footer className="bg-gray-900 text-gray-400 py-12 px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8 mb-8">
          <div>
            <div className="flex items-center space-x-2 mb-4">
              <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-sm">M</span>
              </div>
              <span className="text-xl font-semibold text-white">ModernBlog</span>
            </div>
            <p className="text-sm">
              Une plateforme moderne pour partager vos idées avec le monde.
            </p>
          </div>

          <div>
            <h3 className="text-white font-semibold mb-4">Navigation</h3>
            <ul className="space-y-2 text-sm">
              <li>
                <a href="#accueil" className="hover:text-white transition-colors">
                  Accueil
                </a>
              </li>
              <li>
                <a href="#articles" className="hover:text-white transition-colors">
                  Articles
                </a>
              </li>
            </ul>
          </div>

          <div>
            <h3 className="text-white font-semibold mb-4">Compte</h3>
            <ul className="space-y-2 text-sm">
              <li>
                <a href="#connexion" className="hover:text-white transition-colors">
                  Connexion
                </a>
              </li>
              <li>
                <a href="#inscription" className="hover:text-white transition-colors">
                  Inscription
                </a>
              </li>
            </ul>
          </div>

          <div>
            <h3 className="text-white font-semibold mb-4">Légal</h3>
            <ul className="space-y-2 text-sm">
              <li>
                <a href="#mentions" className="hover:text-white transition-colors">
                  Mentions légales
                </a>
              </li>
              <li>
                <a href="#confidentialite" className="hover:text-white transition-colors">
                  Confidentialité
                </a>
              </li>
            </ul>
          </div>
        </div>

        <div className="border-t border-gray-800 pt-8 text-sm text-center">
          <p>© 2026 ModernBlog. Tous droits réservés.</p>
        </div>
      </div>
    </footer>
  );
};
