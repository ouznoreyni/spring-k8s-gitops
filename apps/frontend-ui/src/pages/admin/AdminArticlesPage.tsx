import { PlusCircle, Search, Edit2, Trash2, Eye } from 'lucide-react';
import { useState } from 'react';
import { Button } from '../../components/ui/Button';
import { CreateArticleModal } from '../../components/articles/CreateArticleModal';

export const AdminArticlesPage = () => {
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const articles = [
    { id: 1, title: 'Comment maîtriser React en 2026', author: 'Saliou', status: 'Publié', date: '12 Jan 2026', views: '1.2k' },
    { id: 2, title: 'Introduction à la Clean Architecture', author: 'Noreyni', status: 'Brouillon', date: '10 Jan 2026', views: '0' },
    { id: 3, title: 'Le futur du développement Web', author: 'Awa', status: 'Publié', date: '08 Jan 2026', views: '856' },
  ];

  return (
    <div className="space-y-8">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">Gestion des Articles</h1>
          <p className="text-gray-600 mt-1">Créez, éditez et gérez vos publications.</p>
        </div>
        <Button 
          className="gap-2 shadow-lg shadow-blue-500/20"
          onClick={() => setIsCreateModalOpen(true)}
        >
          <PlusCircle size={20} />
          Nouvel Article
        </Button>
      </div>

      <CreateArticleModal 
        isOpen={isCreateModalOpen} 
        onClose={() => setIsCreateModalOpen(false)} 
      />

      <div className="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
        <div className="p-6 border-b border-gray-50 flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="relative flex-grow max-w-md">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
            <input 
              type="text" 
              placeholder="Rechercher un article..." 
              className="w-full pl-10 pr-4 py-2 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all"
            />
          </div>
          <div className="flex items-center gap-2">
            <select className="bg-gray-50 border border-gray-200 rounded-xl px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20 transition-all">
              <option>Tous les statuts</option>
              <option>Publié</option>
              <option>Brouillon</option>
            </select>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left">
            <thead>
              <tr className="bg-gray-50/50">
                <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Article</th>
                <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Auteur</th>
                <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Statut</th>
                <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Stats</th>
                <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Date</th>
                <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {articles.map((article) => (
                <tr key={article.id} className="hover:bg-gray-50/50 transition-colors">
                  <td className="px-6 py-4">
                    <span className="font-medium text-gray-900 block">{article.title}</span>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <div className="w-7 h-7 bg-blue-100 rounded-full flex items-center justify-center text-blue-600 text-[10px] font-bold">
                        {article.author.substring(0, 2).toUpperCase()}
                      </div>
                      <span className="text-sm text-gray-600">{article.author}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                      article.status === 'Publié' ? 'bg-green-50 text-green-700' : 'bg-yellow-50 text-yellow-700'
                    }`}>
                      {article.status}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-1.5 text-sm text-gray-500">
                      <Eye size={14} />
                      {article.views}
                    </div>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500">{article.date}</td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <button className="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-all">
                        <Edit2 size={16} />
                      </button>
                      <button className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-all">
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        
        <div className="p-6 border-t border-gray-50 flex items-center justify-between">
          <p className="text-sm text-gray-500">Affichage de 3 articles sur 24</p>
          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm" disabled>Précédent</Button>
            <Button variant="outline" size="sm">Suivant</Button>
          </div>
        </div>
      </div>
    </div>
  );
};
