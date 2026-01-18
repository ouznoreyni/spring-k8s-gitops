import type { AuthResponse, LoginRequest, RegisterRequest } from "../types/auth";

export class ApiAuthRepository {
  private baseUrl = import.meta.env.VITE_AUTH_URL || "http://localhost:8080/api/auth";

  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await fetch(`${this.baseUrl}/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(credentials),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error("Email ou mot de passe incorrect");
      }
      throw new Error("Une erreur est survenue lors de la connexion");
    }

    return response.json();
  }

  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await fetch(`${this.baseUrl}/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      if (response.status === 400) {
        throw new Error("Données d'inscription invalides ou utilisateur déjà existant");
      }
      throw new Error("Une erreur est survenue lors de l'inscription");
    }

    return response.json();
  }
}
