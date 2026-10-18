/**
 * web/javascript/auth.js
 * Gerenciamento de Sessão, Permissões e Menu Dinâmico por Perfil
 */
(function () {
  const CHAVE_SESSAO = "agendaucb_usuario";

  const Auth = {
    // Salva a sessão no LocalStorage
    salvarSessao(usuario) {
      localStorage.setItem(CHAVE_SESSAO, JSON.stringify(usuario));
    },

    // Alias para compatibilidade
    salvarUsuario(usuario) {
      this.salvarSessao(usuario);
    },

    // Recupera o usuário da sessão
    obterUsuario() {
      try {
        const dados = localStorage.getItem(CHAVE_SESSAO);
        return dados ? JSON.parse(dados) : null;
      } catch (e) {
        return null;
      }
    },

    // Alias para obterSessao
    obterSessao() {
      return this.obterUsuario();
    },

    // Encerra a sessão
    logout() {
      localStorage.removeItem(CHAVE_SESSAO);
      window.location.href = "/login.html";
    },

    limparSessao() {
      this.logout();
    },

    // Checagens de perfil
    estaAutenticado() {
      return this.obterUsuario() !== null;
    },

    eAdministrativo() {
      const u = this.obterUsuario();
      return u && (u.tipo_usuario || "").toUpperCase() === "ADMINISTRATIVO";
    },

    // Guardas de rota
    exigirAutenticacao() {
      if (!this.estaAutenticado()) {
        window.location.href = "/login.html";
        return null;
      }
      return this.obterUsuario();
    },

    exigirAdministrativo() {
      const u = this.exigirAutenticacao();
      if (!u) return null;

      // Se for Aluno ou Professor tentando acessar tela restrita, redireciona para agendamento simples
      if ((u.tipo_usuario || "").toUpperCase() !== "ADMINISTRATIVO") {
        window.location.href = "/agendamento-usuario.html";
        return null;
      }
      return u;
    },

    // Controle visual de links e menu conforme o perfil logado
    aplicarPermissoesMenu() {
      const isAdmin = this.eAdministrativo();

      // Oculta abas e links restritos caso não seja administrativo
      document.querySelectorAll(".somente-admin").forEach((el) => {
        el.style.display = isAdmin ? "" : "none";
      });

      // Ajusta o destino do link de Agendamentos na navegação
      const linkAgendamento = document.getElementById("link-nav-agendamentos");
      if (linkAgendamento) {
        if (isAdmin) {
          linkAgendamento.href = "/agendamentos.html";
        } else {
          linkAgendamento.href = "/agendamento-usuario.html";
        }
      }
    },

    // Renderiza o cabeçalho do usuário
    renderizarHeader() {
      const container = document.getElementById("user-header-profile");
      if (!container) return;

      const usuario = this.obterUsuario();

      if (!usuario) {
        container.innerHTML = `
                    <div class="flex items-center gap-2">
                        <a href="/login.html" class="px-3 py-1.5 text-sm font-medium text-blue-700 hover:text-blue-800">Entrar</a>
                        <a href="/cadastro.html" class="px-3 py-1.5 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700">Cadastrar</a>
                    </div>
                `;
        return;
      }

      const nome = usuario.nome || "Usuário";
      const iniciais = nome
        .split(" ")
        .map((n) => n[0])
        .slice(0, 2)
        .join("")
        .toUpperCase();
      const tipo = usuario.tipo_usuario || "Aluno";

      container.innerHTML = `
                <div class="relative">
                    <button id="btn-perfil-menu" class="flex items-center gap-3 p-1 rounded-lg hover:bg-slate-100 transition">
                        <div class="w-9 h-9 rounded-full bg-blue-600 text-white font-semibold flex items-center justify-center text-sm shadow">
                            ${iniciais}
                        </div>
                        <span class="text-sm font-medium text-slate-700 hidden sm:inline">${nome}</span>
                        <svg class="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                        </svg>
                    </button>

                    <div id="dropdown-perfil" class="hidden absolute right-0 mt-2 w-56 bg-white rounded-xl shadow-lg border border-slate-100 py-2 z-50">
                        <div class="px-4 py-2 border-b border-slate-100">
                            <p class="text-xs text-slate-400 uppercase tracking-wider font-semibold">Tipo de Usuário</p>
                            <p class="text-sm font-bold text-blue-600">${tipo}</p>
                            <p class="text-xs text-slate-500 truncate">${usuario.email || ""}</p>
                        </div>
                        <a href="/perfil.html" class="flex items-center gap-2 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50">
                            <span>✏️</span> Editar meus dados
                        </a>
                        <button id="btn-sair" class="w-full text-left flex items-center gap-2 px-4 py-2 text-sm text-red-600 hover:bg-red-50">
                            <span>🚪</span> Sair
                        </button>
                    </div>
                </div>
            `;

      const btnMenu = document.getElementById("btn-perfil-menu");
      const dropdown = document.getElementById("dropdown-perfil");
      const btnSair = document.getElementById("btn-sair");

      if (btnMenu && dropdown) {
        btnMenu.addEventListener("click", (e) => {
          e.stopPropagation();
          dropdown.classList.toggle("hidden");
        });

        document.addEventListener("click", () =>
          dropdown.classList.add("hidden"),
        );
      }

      if (btnSair) {
        btnSair.addEventListener("click", () => {
          Auth.logout();
        });
      }
    },
  };

  window.Auth = Auth;

  document.addEventListener("DOMContentLoaded", () => {
    Auth.renderizarHeader();
    Auth.aplicarPermissoesMenu();
  });
})();
