import React, { useState } from 'react';
import { ImageIcon } from 'lucide-react';
import { cn } from '../../lib/utils';

interface ImageProps extends React.ImgHTMLAttributes<HTMLImageElement> {
  containerClassName?: string;
}

export const Image = ({ src, alt, className, containerClassName, ...props }: ImageProps) => {
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);

  return (
    <div className={cn("relative overflow-hidden bg-gray-100", containerClassName)}>
      {isLoading && !hasError && (
        <div className="absolute inset-0 animate-pulse bg-gray-200 flex items-center justify-center">
          <ImageIcon className="w-8 h-8 text-gray-400" />
        </div>
      )}
      
      {hasError ? (
        <div className="absolute inset-0 bg-gray-50 flex flex-col items-center justify-center text-gray-400 p-4 text-center">
          <ImageIcon className="w-8 h-8 mb-2" />
          <span className="text-xs">Image non disponible</span>
        </div>
      ) : (
        <img
          src={src}
          alt={alt}
          loading="lazy"
          className={cn(
            "transition-all duration-500",
            isLoading ? "opacity-0 scale-105" : "opacity-100 scale-100",
            className
          )}
          onLoad={() => setIsLoading(false)}
          onError={() => {
            setIsLoading(false);
            setHasError(true);
          }}
          {...props}
        />
      )}
    </div>
  );
};
