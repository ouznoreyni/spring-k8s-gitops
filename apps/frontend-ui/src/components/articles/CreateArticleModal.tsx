import React, { useState } from 'react';
import { X, Loader2, Image as ImageIcon } from 'lucide-react';
import { Button } from '../ui/Button';
import { ApiArticleRepository } from '../../api/articles';
import { useAuthStore } from '../../store/authStore';
import { useQueryClient } from '@tanstack/react-query';

interface CreateArticleModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const articleRepo = new ApiArticleRepository();

export const CreateArticleModal = ({ isOpen, onClose }: CreateArticleModalProps) => {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const token = useAuthStore((state) => state.user?.token);
  const queryClient = useQueryClient();

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) return;

    setIsLoading(true);
    setError(null);

    try {
      await articleRepo.create({ title, content, imageUrl }, token);
      queryClient.invalidateQueries({ queryKey: ['articles'] });
      onClose();
      setTitle('');
      setContent('');
      setImageUrl('');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Une erreur est survenue');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 sm:p-6">
      <div 
        className="absolute inset-0 bg-gray-900/40 backdrop-blur-sm transition-opacity" 
        onClick={onClose}
      />
      
      <div className="relative w-full max-w-2xl bg-white rounded-3xl shadow-2xl border border-white/20 overflow-hidden animate-in fade-in zoom-in duration-200">
        <div className="flex items-center justify-between p-6 border-b border-gray-100">
          <h3 className="text-xl font-bold text-gray-900">Nouvel Article</h3>
          <button 
            onClick={onClose}
            className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-xl transition-all"
          >
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-5">
          {error && (
            <div className="bg-red-50 border border-red-100 text-red-600 px-4 py-3 rounded-xl text-sm animate-shake">
              {error}
            </div>
          )}

          <div className="space-y-1">
            <label className="block text-sm font-semibold text-gray-700 ml-1">
              Titre de l'article
            </label>
            <input
              type="text"
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="block w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-2xl text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all"
              placeholder="Ex: Les nouveautés de React en 2026"
            />
          </div>

          <div className="space-y-1">
            <label className="block text-sm font-semibold text-gray-700 ml-1">
              URL de l'image
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <ImageIcon className="h-5 w-5 text-gray-400" />
              </div>
              <input
                type="url"
                value={imageUrl}
                onChange={(e) => setImageUrl(e.target.value)}
                className="block w-full pl-11 pr-4 py-3 bg-gray-50 border border-gray-200 rounded-2xl text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all"
                placeholder="https://images.unsplash.com/..."
              />
            </div>
            <p className="text-[11px] text-gray-500 ml-1">Collez l'URL d'une image pour illustrer votre article.</p>
          </div>

          <div className="space-y-1">
            <label className="block text-sm font-semibold text-gray-700 ml-1">
              Contenu
            </label>
            <textarea
              required
              rows={6}
              value={content}
              onChange={(e) => setContent(e.target.value)}
              className="block w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-2xl text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all resize-none"
              placeholder="Rédigez votre article ici..."
            />
          </div>

          <div className="flex justify-end gap-3 pt-4">
            <Button type="button" variant="outline" onClick={onClose}>
              Annuler
            </Button>
            <Button type="submit" disabled={isLoading} className="min-w-[140px]">
              {isLoading ? (
                <>
                  <Loader2 className="animate-spin -ml-1 mr-3 h-5 w-5" />
                  Publication...
                </>
              ) : (
                'Publier l\'article'
              )}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
