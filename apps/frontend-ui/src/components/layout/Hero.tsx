import { ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Button } from '../ui/Button';

export const Hero = () => {
  return (
    <section id="accueil" className="pt-32 pb-20 px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="max-w-3xl">
          <h1 className="text-5xl md:text-6xl lg:text-7xl font-bold text-gray-900 leading-tight mb-6">
            Partagez vos idées avec le monde
          </h1>
          <p className="text-xl text-gray-600 mb-8 leading-relaxed">
            ModernBlog est une plateforme simple pour écrire, publier et découvrir des articles sur les sujets qui vous passionnent.
          </p>
          <div className="flex flex-col sm:flex-row gap-4">
            <Link to="/articles">
              <Button size="lg" className="gap-2 group w-full sm:w-auto">
                Voir les articles
                <ArrowRight size={20} className="group-hover:translate-x-1 transition-transform" />
              </Button>
            </Link>
            <Link to="/register">
              <Button variant="outline" size="lg">
                Créer un compte
              </Button>
            </Link>
          </div>
        </div>
      </div>
    </section>
  );
};
