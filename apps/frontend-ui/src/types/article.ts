export interface Article {
  id: number;
  title: string;
  content: string;
  imageUrl?: string;
  authorId: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateArticleRequest {
  title: string;
  content: string;
  imageUrl?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalPages: number;
  totalElements: number;
  last: boolean;
  first: boolean;
  number: number;
  size: number;
  numberOfElements: number;
  empty: boolean;
}

export interface ArticleRepository {
  getAll(page: number, size: number): Promise<PaginatedResponse<Article>>;
  create(article: CreateArticleRequest, token: string): Promise<Article>;
}
