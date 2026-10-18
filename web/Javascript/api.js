/**
 * js/api.js
 * Cliente HTTP Vanilla JS integrado ao RecursoController
 */
const API = {
  // Ajuste a URL base conforme o endereço da sua aplicação Java (ex: http://localhost:8080/api)
  baseURL: "http://localhost:8080/api",

  async request(endpoint, options = {}) {
    const url = endpoint.startsWith("http")
      ? endpoint
      : `${this.baseURL}${endpoint}`;

    const config = {
      headers: {
        "Content-Type": "application/json",
        ...(options.headers || {}),
      },
      ...options,
    };

    try {
      const response = await fetch(url, config);
      const text = await response.text();
      let data = null;

      try {
        data = text ? JSON.parse(text) : null;
      } catch (e) {
        data = { mensagem: text };
      }

      if (!response.ok) {
        // Extrai a mensagem de erro retornada pelo RecursoController ({"erro": "..."})
        const mensagemErro =
          data && data.erro
            ? data.erro
            : `Erro na requisição (${response.status})`;
        const error = new Error(mensagemErro);
        error.status = response.status;
        error.data = data;
        throw error;
      }

      return data;
    } catch (err) {
      if (!err.status) {
        // Erro de rede ou CORS
        err.message =
          "Não foi possível conectar ao servidor. Verifique se o backend está em execução.";
      }
      throw err;
    }
  },

  get(endpoint) {
    return this.request(endpoint, { method: "GET" });
  },

  post(endpoint, body) {
    return this.request(endpoint, {
      method: "POST",
      body: JSON.stringify(body),
    });
  },

  put(endpoint, body) {
    return this.request(endpoint, {
      method: "PUT",
      body: JSON.stringify(body),
    });
  },

  delete(endpoint) {
    return this.request(endpoint, { method: "DELETE" });
  },
};
