import type { Article, ArticleRepository, CreateArticleRequest, PaginatedResponse } from "../types/article";

export class ApiArticleRepository implements ArticleRepository {
  private baseUrl = `${import.meta.env.VITE_API_URL || "/api"}/articles`;

  async getAll(page: number, size: number): Promise<PaginatedResponse<Article>> {
    const response = await fetch(`${this.baseUrl}?page=${page}&size=${size}`);
    if (!response.ok) {
      throw new Error("Failed to fetch articles");
    }
    return response.json();
  }

  async create(article: CreateArticleRequest, token: string): Promise<Article> {
    const response = await fetch(this.baseUrl, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify(article),
    });

    if (!response.ok) {
      throw new Error("Failed to create article");
    }

    return response.json();
  }
}
