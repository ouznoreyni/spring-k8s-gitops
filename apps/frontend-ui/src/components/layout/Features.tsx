import type { LucideIcon } from 'lucide-react';
import { Pen, Users, Zap } from 'lucide-react';
import { cn } from '../../lib/utils';

interface FeatureItem {
  icon: LucideIcon;
  title: string;
  description: string;
  color: string;
}

const features: FeatureItem[] = [
  {
    icon: Pen,
    title: "Simple à utiliser",
    description: "Un éditeur intuitif pour rédiger vos articles facilement.",
    color: "from-blue-500 to-cyan-500"
  },
  {
    icon: Users,
    title: "Communauté active",
    description: "Connectez-vous avec des lecteurs passionnés.",
    color: "from-emerald-500 to-teal-500"
  },
  {
    icon: Zap,
    title: "Rapide et fiable",
    description: "Une plateforme performante pour une expérience fluide.",
    color: "from-orange-500 to-amber-500"
  }
];

export const Features = () => {
  return (
    <section className="py-20 px-6 lg:px-8 bg-gray-50">
      <div className="max-w-7xl mx-auto">
        <div className="text-center mb-16">
          <h2 className="text-4xl font-bold text-gray-900 mb-4">
            Pourquoi ModernBlog ?
          </h2>
        </div>

        <div className="grid md:grid-cols-3 gap-8">
          {features.map((feature, index) => {
            const Icon = feature.icon;
            return (
              <div
                key={index}
                className="bg-white p-8 rounded-2xl hover:shadow-xl transition-all duration-300 border border-gray-100 group"
              >
                <div className={cn(
                  "w-14 h-14 bg-gradient-to-br rounded-xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform",
                  feature.color
                )}>
                  <Icon size={28} className="text-white" />
                </div>
                <h3 className="text-xl font-semibold text-gray-900 mb-3">
                  {feature.title}
                </h3>
                <p className="text-gray-600 leading-relaxed">
                  {feature.description}
                </p>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
};
