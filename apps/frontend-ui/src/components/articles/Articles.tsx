import { Calendar, ArrowRight, Loader2 } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useArticles } from '../../hooks/useArticles';
import { Button } from '../ui/Button';
import { Image } from '../ui/Image';
import { useEffect, useRef } from 'react';

export const Articles = () => {
  const { 
    data, 
    isLoading, 
    isError, 
    fetchNextPage, 
    hasNextPage, 
    isFetchingNextPage 
  } = useArticles();

  const loadMoreRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasNextPage && !isFetchingNextPage) {
          fetchNextPage();
        }
      },
      { threshold: 1.0 }
    );

    if (loadMoreRef.current) {
      observer.observe(loadMoreRef.current);
    }

    return () => observer.disconnect();
  }, [hasNextPage, isFetchingNextPage, fetchNextPage]);

  if (isLoading) {
    return (
      <section id="articles" className="py-20 px-6 lg:px-8">
        <div className="max-w-7xl mx-auto text-center">
          <Loader2 className="w-8 h-8 animate-spin mx-auto mb-4 text-blue-600" />
          <p className="text-gray-600">Chargement des articles...</p>
        </div>
      </section>
    );
  }

  if (isError) {
    return (
      <section id="articles" className="py-20 px-6 lg:px-8">
        <div className="max-w-7xl mx-auto text-center text-red-600">
          Une erreur est survenue lors de la récupération des articles.
        </div>
      </section>
    );
  }

  const articles = data?.pages.flatMap((page) => page.content) || [];

  return (
    <section id="articles" className="py-20 px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="flex items-end justify-between mb-12">
          <div>
            <h2 className="text-4xl font-bold text-gray-900 mb-2">
              Articles récents
            </h2>
            <p className="text-gray-600">
              Découvrez nos dernières publications
            </p>
          </div>
          <Link to="/articles" className="hidden md:block">
            <Button variant="ghost" className="flex gap-2 group">
              Voir tous les articles
              <ArrowRight size={20} className="group-hover:translate-x-1 transition-transform" />
            </Button>
          </Link>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {articles.map((article) => (
            <article
              key={article.id}
              className="group bg-white border border-gray-200 rounded-2xl overflow-hidden hover:shadow-xl hover:border-gray-300 transition-all duration-300"
            >
              {article.imageUrl ? (
                <Image 
                  src={article.imageUrl} 
                  alt={article.title}
                  containerClassName="h-48"
                  className="w-full h-full object-cover group-hover:scale-105"
                />
              ) : (
                <div className="h-48 bg-gradient-to-br from-gray-100 to-gray-200 group-hover:from-blue-50 group-hover:to-cyan-50 transition-all"></div>
              )}
              <div className="p-6">
                <div className="flex items-center gap-2 text-sm text-gray-500 mb-3">
                  <Calendar size={16} />
                  <span>{new Date(article.createdAt).toLocaleDateString()}</span>
                </div>
                <h3 className="text-xl font-semibold text-gray-900 mb-2 group-hover:text-blue-600 transition-colors line-clamp-2">
                  {article.title}
                </h3>
                <p className="text-gray-600 mb-4 line-clamp-3">
                  {article.content}
                </p>
                <button className="text-blue-600 hover:text-blue-700 font-medium flex items-center gap-2 group">
                  Lire l'article
                  <ArrowRight size={18} className="group-hover:translate-x-1 transition-transform" />
                </button>
              </div>
            </article>
          ))}
        </div>

        <div ref={loadMoreRef} className="h-10 mt-8 flex justify-center items-center">
          {isFetchingNextPage && (
            <Loader2 className="w-6 h-6 animate-spin text-blue-600" />
          )}
        </div>

        <div className="flex justify-center mt-8 md:hidden">
          <Link to="/articles">
            <Button variant="ghost" className="gap-2 text-blue-600">
              Voir tous les articles
              <ArrowRight size={20} />
            </Button>
          </Link>
        </div>
      </div>
    </section>
  );
};
