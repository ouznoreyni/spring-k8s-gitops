import { FileText, Users, PlusCircle, TrendingUp, Clock, MessageSquare } from 'lucide-react';
import { Button } from '../components/ui/Button';

export const AdminPage = () => {
  const stats = [
    { label: 'Total Articles', value: '24', icon: FileText, color: 'text-blue-600', bg: 'bg-blue-50' },
    { label: 'Utilisateurs', value: '1,203', icon: Users, color: 'text-purple-600', bg: 'bg-purple-50' },
    { label: 'Commentaires', value: '458', icon: MessageSquare, color: 'text-emerald-600', bg: 'bg-emerald-50' },
    { label: 'Vues Total', value: '12.4k', icon: TrendingUp, color: 'text-orange-600', bg: 'bg-orange-50' },
  ];

  const recentArticles = [
    { id: 1, title: 'Comment maîtriser React en 2026', status: 'Publié', date: '12 Jan 2026' },
    { id: 2, title: 'Introduction à la Clean Architecture', status: 'Brouillon', date: '10 Jan 2026' },
    { id: 3, title: 'Le futur du développement Web', status: 'Publié', date: '08 Jan 2026' },
  ];

  return (
    <div className="space-y-10">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">Espace Administrateur</h1>
          <p className="text-gray-600 mt-1">Gérez vos articles et suivez vos performances.</p>
        </div>
        <Button className="gap-2 shadow-lg shadow-blue-500/20">
          <PlusCircle size={20} />
          Nouvel Article
        </Button>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat, index) => (
          <div key={index} className="bg-white p-6 rounded-3xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow">
            <div className="flex items-center gap-4">
              <div className={`${stat.bg} ${stat.color} p-4 rounded-2xl`}>
                <stat.icon size={24} />
              </div>
              <div>
                <p className="text-sm font-medium text-gray-500">{stat.label}</p>
                <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="grid lg:grid-cols-3 gap-8">
        {/* Main Content Area */}
        <div className="lg:col-span-2 space-y-8">
          <div className="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
            <div className="p-6 border-b border-gray-50 flex items-center justify-between">
              <h3 className="font-bold text-gray-900">Articles Récents</h3>
              <Button variant="ghost" size="sm">Tout voir</Button>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead>
                  <tr className="bg-gray-50/50">
                    <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Titre</th>
                    <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Statut</th>
                    <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Date</th>
                    <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {recentArticles.map((article) => (
                    <tr key={article.id} className="hover:bg-gray-50/50 transition-colors">
                      <td className="px-6 py-4">
                        <span className="font-medium text-gray-900">{article.title}</span>
                      </td>
                      <td className="px-6 py-4">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          article.status === 'Publié' ? 'bg-green-50 text-green-700' : 'bg-yellow-50 text-yellow-700'
                        }`}>
                          {article.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-500">{article.date}</td>
                      <td className="px-6 py-4">
                        <button className="text-blue-600 hover:text-blue-800 font-medium text-sm">Éditer</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Sidebar Area */}
        <div className="space-y-8">
          <div className="bg-white p-6 rounded-3xl border border-gray-100 shadow-sm">
            <h3 className="font-bold text-gray-900 mb-6 flex items-center gap-2">
              <Clock size={20} className="text-blue-500" />
              Activité Récente
            </h3>
            <div className="space-y-6">
              {[1, 2, 3].map((i) => (
                <div key={i} className="flex gap-4">
                  <div className="w-2 h-2 mt-2 rounded-full bg-blue-500 flex-shrink-0"></div>
                  <div>
                    <p className="text-sm text-gray-900"><span className="font-semibold">Saliou</span> a publié un nouvel article.</p>
                    <p className="text-xs text-gray-500 mt-1">Il y a 2 heures</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="bg-gradient-to-br from-gray-900 to-blue-900 p-6 rounded-3xl shadow-xl text-white">
            <h3 className="font-bold mb-2">Besoin d'aide ?</h3>
            <p className="text-gray-300 text-sm mb-4">Consultez la documentation ou contactez le support technique.</p>
            <Button variant="secondary" size="sm" className="w-full">Support</Button>
          </div>
        </div>
      </div>
    </div>
  );
};
