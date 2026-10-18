/**
 * javascript/agendamentos.js
 * Controller da tela de Agendamentos e Tipos de Agendamento (Vanilla JS)
 */

(function () {
  let listaAgendamentos = [];
  let listaTipos = [];
  let listaPredios = [];
  let listaSalas = [];
  let usuarioAtual = null;

  // Badges coloridos por Situação do Agendamento (SituacaoAgendamento enum)
  function obterBadgeSituacao(situacao) {
    const s = (situacao || "").toUpperCase();
    let classes = "bg-slate-100 text-slate-700 border border-slate-200";

    switch (s) {
      case "SOLICITADO":
        classes = "bg-amber-100 text-amber-800 border border-amber-200";
        break;
      case "APROVADO":
        classes = "bg-blue-100 text-blue-700 border border-blue-200";
        break;
      case "CONFIRMADO":
        classes = "bg-emerald-100 text-emerald-700 border border-emerald-200";
        break;
      case "REALIZADO":
        classes = "bg-indigo-100 text-indigo-700 border border-indigo-200";
        break;
      case "CANCELADO":
        classes = "bg-rose-100 text-rose-700 border border-rose-200";
        break;
    }

    return `<span class="px-2.5 py-0.5 rounded-full text-xs font-semibold ${classes}">${situacao || "-"}</span>`;
  }

  // Formatador de Data/Hora (Ex: 18/10/2026 às 14:00)
  function formatarDataHora(str) {
    if (!str) return "-";
    // Substitui traço e limpa milissegundos
    const limpo = str.replace(" ", "T").split(".")[0];
    const data = new Date(limpo);
    if (isNaN(data.getTime())) return str;
    return data.toLocaleString("pt-BR", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  // Converte data local para formato de input datetime-local
  function paraInputDatetime(str) {
    if (!str) return "";
    const limpo = str.replace(" ", "T").split(".")[0];
    return limpo.substring(0, 16);
  }

  // Converte input datetime-local (AAAA-MM-DDTHH:mm) para "AAAA-MM-DD HH:mm:ss"
  function paraFormatoBackend(str) {
    if (!str) return "";
    let f = str.replace("T", " ");
    if (f.length === 16) f += ":00";
    return f;
  }

  window.Agendamentos = {
    init: async function () {
      // 🔒 Exige login
      if (!window.Auth) return;
      usuarioAtual = window.Auth.exigirAutenticacao();
      if (!usuarioAtual) return;

      await Promise.all([
        this.carregarPrediosESalas(),
        this.carregarTipos(),
        this.carregarAgendamentos(),
      ]);
    },

    trocarAba: function (aba) {
      const secaoAg = document.getElementById("secao-agendamentos");
      const secaoTipos = document.getElementById("secao-tipos");
      const btnAg = document.getElementById("tab-btn-agendamentos");
      const btnTipos = document.getElementById("tab-btn-tipos");

      if (aba === "agendamentos") {
        secaoAg.classList.remove("hidden");
        secaoTipos.classList.add("hidden");
        btnAg.className =
          "px-5 py-2.5 font-semibold text-sm rounded-lg bg-blue-600 text-white shadow-sm flex items-center gap-2 transition";
        btnTipos.className =
          "px-5 py-2.5 font-semibold text-sm rounded-lg text-slate-600 hover:bg-slate-200 transition flex items-center gap-2";
      } else {
        secaoTipos.classList.remove("hidden");
        secaoAg.classList.add("hidden");
        btnTipos.className =
          "px-5 py-2.5 font-semibold text-sm rounded-lg bg-blue-600 text-white shadow-sm flex items-center gap-2 transition";
        btnAg.className =
          "px-5 py-2.5 font-semibold text-sm rounded-lg text-slate-600 hover:bg-slate-200 transition flex items-center gap-2";
      }
    },

    // ==========================================
    // CARREGAR DADOS RELACIONAIS
    // ==========================================
    carregarPrediosESalas: async function () {
      try {
        const [predios, salas] = await Promise.all([
          API.get("/predios").catch(() => []),
          API.get("/salas").catch(() => []),
        ]);

        listaPredios = Array.isArray(predios) ? predios : [];
        listaSalas = Array.isArray(salas) ? salas : [];

        // Preenche o select de prédios do modal
        const selectPredio = document.getElementById("agendamento-predio");
        if (selectPredio) {
          selectPredio.innerHTML =
            '<option value="">Selecione o Prédio</option>' +
            listaPredios
              .map(
                (p) =>
                  `<option value="${p.id_predio}">${p.nome} (ID #${p.id_predio})</option>`,
              )
              .join("");
        }
      } catch (e) {
        console.error("Falha ao obter dependências:", e);
      }
    },

    atualizarSalasPorPredio: function (salaPreSelecionada = null) {
      const idPredio = document.getElementById("agendamento-predio").value;
      const selectSala = document.getElementById("agendamento-sala");
      if (!selectSala) return;

      if (!idPredio) {
        selectSala.innerHTML =
          '<option value="">Selecione primeiro o Prédio</option>';
        return;
      }

      const salasFiltradas = listaSalas.filter(
        (s) => String(s.id_predio) === String(idPredio),
      );
      if (salasFiltradas.length === 0) {
        selectSala.innerHTML =
          '<option value="">Nenhuma sala cadastrada neste prédio</option>';
        return;
      }

      selectSala.innerHTML =
        '<option value="">Selecione a Sala</option>' +
        salasFiltradas
          .map(
            (s) => `
          <option value="${s.numero_sala}">
            Sala ${s.numero_sala} (${s.tipo_sala} - Cap: ${s.capacidade} alunos)
          </option>
        `,
          )
          .join("");

      if (salaPreSelecionada) {
        selectSala.value = salaPreSelecionada;
      }
    },

    obterDescricaoPredio: function (idPredio) {
      const p = listaPredios.find((item) => item.id_predio === idPredio);
      return p ? p.nome : `Prédio #${idPredio}`;
    },

    obterDescricaoTipo: function (idTipo) {
      const t = listaTipos.find((item) => item.id_tipo === idTipo);
      return t ? t.descricao : `Tipo #${idTipo}`;
    },

    // ==========================================
    // MÓDULO AGENDAMENTOS
    // ==========================================
    carregarAgendamentos: async function () {
      try {
        const dados = await API.get("/agendamentos");
        listaAgendamentos = Array.isArray(dados) ? dados : [];
        this.atualizarContadorAgendamentos();
        this.renderizarAgendamentos(listaAgendamentos);
      } catch (err) {
        this.mostrarErro(
          "Erro ao listar agendamentos: " + (err.message || "Falha de conexão"),
        );
      }
    },

    atualizarContadorAgendamentos: function () {
      const el = document.getElementById("contador-agendamentos");
      if (el) el.textContent = listaAgendamentos.length;
    },

    renderizarAgendamentos: function (lista) {
      const tbody = document.getElementById("tabela-agendamentos-corpo");
      if (!tbody) return;

      if (!lista || lista.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="p-8 text-center text-slate-400">Nenhum agendamento encontrado.</td></tr>`;
        return;
      }

      tbody.innerHTML = lista
        .map(
          (ag) => `
        <tr class="hover:bg-slate-50 transition border-b border-slate-100">
          <td class="p-4 font-mono font-bold text-slate-500 text-xs">#${ag.id_agendamento}</td>
          <td class="p-4">
            <div class="font-bold text-slate-800">${this.obterDescricaoPredio(ag.id_predio)}</div>
            <div class="text-xs text-blue-600 font-mono font-semibold">Sala: ${ag.numero_sala}</div>
          </td>
          <td class="p-4 text-xs">
            <div class="text-slate-800 font-semibold">${formatarDataHora(ag.data_hora_inicio)}</div>
            <div class="text-slate-500">até ${formatarDataHora(ag.data_hora_fim)}</div>
          </td>
          <td class="p-4">
            <span class="px-2 py-0.5 rounded-md text-xs font-medium bg-slate-100 text-slate-700">
              ${this.obterDescricaoTipo(ag.id_tipo)}
            </span>
          </td>
          <td class="p-4">
            ${obterBadgeSituacao(ag.situacao_atual)}
          </td>
          <td class="p-4 text-xs text-slate-600 max-w-[200px] truncate" title="${(ag.motivo_descricao || "").replace(/"/g, "&quot;")}">
            ${ag.motivo_descricao || '<span class="text-slate-400 italic">Sem motivo informado</span>'}
          </td>
          <td class="p-4 text-right space-x-2 whitespace-nowrap">
            <button onclick="Agendamentos.editarAgendamento(${ag.id_agendamento})" class="px-2.5 py-1 text-xs font-semibold bg-blue-50 text-blue-700 hover:bg-blue-100 rounded-md transition shadow-sm">
              Editar
            </button>
            <button onclick="Agendamentos.excluirAgendamento(${ag.id_agendamento})" class="px-2.5 py-1 text-xs font-semibold bg-rose-50 text-rose-700 hover:bg-rose-100 rounded-md transition shadow-sm">
              Excluir
            </button>
          </td>
        </tr>
      `,
        )
        .join("");
    },

    filtrarAgendamentos: function () {
      const situacao = (
        document.getElementById("filtro-situacao").value || ""
      ).toUpperCase();
      const termo = (document.getElementById("busca-agendamento").value || "")
        .toLowerCase()
        .trim();

      const filtrados = listaAgendamentos.filter((ag) => {
        const atendeSituacao =
          !situacao ||
          (ag.situacao_atual && ag.situacao_atual.toUpperCase() === situacao);
        const predioNome = this.obterDescricaoPredio(
          ag.id_predio,
        ).toLowerCase();
        const tipoNome = this.obterDescricaoTipo(ag.id_tipo).toLowerCase();
        const motivo = (ag.motivo_descricao || "").toLowerCase();
        const sala = (ag.numero_sala || "").toLowerCase();

        const atendeTermo =
          !termo ||
          predioNome.includes(termo) ||
          tipoNome.includes(termo) ||
          motivo.includes(termo) ||
          sala.includes(termo) ||
          String(ag.id_agendamento).includes(termo);

        return atendeSituacao && atendeTermo;
      });

      this.renderizarAgendamentos(filtrados);
    },

    abrirModalAgendamento: function (ag = null) {
      document.getElementById("form-agendamento").reset();
      const titulo = document.getElementById("modal-agendamento-titulo");
      const campoId = document.getElementById("agendamento-id");
      const campoUsuario = document.getElementById("agendamento-usuario");

      if (ag) {
        titulo.textContent = `Editar Agendamento #${ag.id_agendamento}`;
        campoId.value = ag.id_agendamento;

        document.getElementById("agendamento-predio").value = ag.id_predio;
        this.atualizarSalasPorPredio(ag.numero_sala);

        document.getElementById("agendamento-tipo").value = ag.id_tipo;
        document.getElementById("agendamento-situacao").value =
          ag.situacao_atual;
        document.getElementById("agendamento-inicio").value = paraInputDatetime(
          ag.data_hora_inicio,
        );
        document.getElementById("agendamento-fim").value = paraInputDatetime(
          ag.data_hora_fim,
        );
        campoUsuario.value = ag.id_usuario_solicitante;
        document.getElementById("agendamento-motivo").value =
          ag.motivo_descricao || "";
      } else {
        titulo.textContent = "Novo Agendamento";
        campoId.value = "";
        document.getElementById("agendamento-situacao").value = "SOLICITADO";

        // Auto-preenche com o usuário da sessão logada
        if (usuarioAtual && usuarioAtual.id_usuario) {
          campoUsuario.value = usuarioAtual.id_usuario;
        }

        // Sugere data/hora atual no início
        const agora = new Date();
        agora.setMinutes(0);
        agora.setSeconds(0);
        agora.setHours(agora.getHours() + 1);
        const inicioPadrao = agora.toISOString().slice(0, 16);

        const fimPadrao = new Date(agora.getTime() + 60 * 60 * 1000)
          .toISOString()
          .slice(0, 16);
        document.getElementById("agendamento-inicio").value = inicioPadrao;
        document.getElementById("agendamento-fim").value = fimPadrao;
      }

      document.getElementById("modal-agendamento").classList.remove("hidden");
    },

    fecharModalAgendamento: function () {
      document.getElementById("modal-agendamento").classList.add("hidden");
    },

    salvarAgendamento: async function (e) {
      e.preventDefault();
      const btn = document.getElementById("btn-salvar-agendamento");
      btn.disabled = true;
      btn.textContent = "Salvando...";

      const id = document.getElementById("agendamento-id").value;
      const inicioStr = document.getElementById("agendamento-inicio").value;
      const fimStr = document.getElementById("agendamento-fim").value;

      const payload = {
        inicio: paraFormatoBackend(inicioStr),
        fim: paraFormatoBackend(fimStr),
        situacao: document.getElementById("agendamento-situacao").value,
        motivo: document.getElementById("agendamento-motivo").value.trim(),
        id_usuario: parseInt(
          document.getElementById("agendamento-usuario").value,
          10,
        ),
        id_predio: parseInt(
          document.getElementById("agendamento-predio").value,
          10,
        ),
        numero_sala: document.getElementById("agendamento-sala").value,
        id_tipo: parseInt(
          document.getElementById("agendamento-tipo").value,
          10,
        ),
      };

      try {
        if (id) {
          payload.id_agendamento = parseInt(id, 10);
          await API.put("/agendamentos", payload);
          this.mostrarSucesso("Agendamento atualizado com sucesso!");
        } else {
          await API.post("/agendamentos", payload);
          this.mostrarSucesso("Agendamento cadastrado com sucesso!");
        }

        this.fecharModalAgendamento();
        await this.carregarAgendamentos();
      } catch (err) {
        // Exibe erro de conflito de sala (409) ou outro erro do backend
        this.mostrarErro(err.message || "Erro ao processar o agendamento.");
      } finally {
        btn.disabled = false;
        btn.textContent = "Salvar Agendamento";
      }
    },

    editarAgendamento: function (id) {
      const ag = listaAgendamentos.find((item) => item.id_agendamento === id);
      if (ag) {
        this.abrirModalAgendamento(ag);
      }
    },

    excluirAgendamento: async function (id) {
      if (
        !confirm(
          `Tem certeza que deseja cancelar e excluir o agendamento #${id}?`,
        )
      ) {
        return;
      }

      try {
        await API.delete("/agendamentos?id=" + id);
        this.mostrarSucesso("Agendamento removido com sucesso!");
        await this.carregarAgendamentos();
      } catch (err) {
        this.mostrarErro(err.message || "Erro ao excluir agendamento.");
      }
    },

    // ==========================================
    // MÓDULO TIPOS DE AGENDAMENTO
    // ==========================================
    carregarTipos: async function () {
      try {
        const dados = await API.get("/tipos-agendamento");
        listaTipos = Array.isArray(dados) ? dados : [];
        this.atualizarContadorTipos();
        this.renderizarTipos(listaTipos);
        this.atualizarSelectTipos();
      } catch (err) {
        console.warn("Erro ao carregar tipos de agendamento:", err.message);
      }
    },

    atualizarContadorTipos: function () {
      const el = document.getElementById("contador-tipos");
      if (el) el.textContent = listaTipos.length;
    },

    atualizarSelectTipos: function () {
      const select = document.getElementById("agendamento-tipo");
      if (!select) return;
      const valorAtual = select.value;
      select.innerHTML =
        '<option value="">Selecione o Tipo</option>' +
        listaTipos
          .map(
            (t) =>
              `<option value="${t.id_tipo}">${t.descricao} (${t.duracao_minutos} min)</option>`,
          )
          .join("");
      select.value = valorAtual;
    },

    renderizarTipos: function (tipos) {
      const tbody = document.getElementById("tabela-tipos-corpo");
      if (!tbody) return;

      if (!tipos || tipos.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="p-8 text-center text-slate-400">Nenhum tipo cadastrado.</td></tr>`;
        return;
      }

      tbody.innerHTML = tipos
        .map(
          (t) => `
        <tr class="hover:bg-slate-50 transition border-b border-slate-100">
          <td class="p-4 font-mono font-bold text-slate-500 text-xs">#${t.id_tipo}</td>
          <td class="p-4 font-semibold text-slate-800">${t.descricao}</td>
          <td class="p-4 text-center font-medium">${t.duracao_minutos} minutos</td>
          <td class="p-4 text-center">
            <span class="px-2.5 py-0.5 rounded-full text-xs font-semibold ${
              t.aprovacao
                ? "bg-amber-100 text-amber-800 border border-amber-200"
                : "bg-slate-100 text-slate-600 border border-slate-200"
            }">
              ${t.aprovacao ? "✓ Requer Aprovação" : "✕ Automática"}
            </span>
          </td>
          <td class="p-4 text-right space-x-2 whitespace-nowrap">
            <button onclick="Agendamentos.editarTipo(${t.id_tipo})" class="px-2.5 py-1 text-xs font-semibold bg-blue-50 text-blue-700 hover:bg-blue-100 rounded-md transition shadow-sm">
              Editar
            </button>
            <button onclick="Agendamentos.excluirTipo(${t.id_tipo}, '${(t.descricao || "").replace(/'/g, "\\'")}')" class="px-2.5 py-1 text-xs font-semibold bg-rose-50 text-rose-700 hover:bg-rose-100 rounded-md transition shadow-sm">
              Excluir
            </button>
          </td>
        </tr>
      `,
        )
        .join("");
    },

    abrirModalTipo: function (tipo = null) {
      document.getElementById("form-tipo").reset();
      const titulo = document.getElementById("modal-tipo-titulo");
      const campoId = document.getElementById("tipo-id");

      if (tipo) {
        titulo.textContent = `Editar Tipo #${tipo.id_tipo}`;
        campoId.value = tipo.id_tipo;
        document.getElementById("tipo-descricao").value = tipo.descricao || "";
        document.getElementById("tipo-duracao").value =
          tipo.duracao_minutos || 60;
        document.getElementById("tipo-aprovacao").value = String(
          tipo.aprovacao,
        );
      } else {
        titulo.textContent = "Novo Tipo de Agendamento";
        campoId.value = "";
        document.getElementById("tipo-duracao").value = 60;
        document.getElementById("tipo-aprovacao").value = "false";
      }

      document.getElementById("modal-tipo").classList.remove("hidden");
    },

    fecharModalTipo: function () {
      document.getElementById("modal-tipo").classList.add("hidden");
    },

    salvarTipo: async function (e) {
      e.preventDefault();
      const btn = document.getElementById("btn-salvar-tipo");
      btn.disabled = true;
      btn.textContent = "Salvando...";

      const id = document.getElementById("tipo-id").value;
      const payload = {
        descricao: document.getElementById("tipo-descricao").value.trim(),
        duracao_minutos: parseInt(
          document.getElementById("tipo-duracao").value,
          10,
        ),
        aprovacao: document.getElementById("tipo-aprovacao").value === "true",
      };

      try {
        if (id) {
          payload.id_tipo = parseInt(id, 10);
          await API.put("/tipos-agendamento", payload);
          this.mostrarSucesso("Tipo de agendamento atualizado com sucesso!");
        } else {
          await API.post("/tipos-agendamento", payload);
          this.mostrarSucesso("Tipo de agendamento cadastrado com sucesso!");
        }

        this.fecharModalTipo();
        await this.carregarTipos();
      } catch (err) {
        this.mostrarErro(err.message || "Erro ao salvar tipo de agendamento.");
      } finally {
        btn.disabled = false;
        btn.textContent = "Salvar Tipo";
      }
    },

    editarTipo: function (id) {
      const tipo = listaTipos.find((t) => t.id_tipo === id);
      if (tipo) {
        this.abrirModalTipo(tipo);
      }
    },

    excluirTipo: async function (id, descricao) {
      if (!confirm(`Tem certeza que deseja excluir o tipo "${descricao}"?`)) {
        return;
      }

      try {
        await API.delete("/tipos-agendamento?id=" + id);
        this.mostrarSucesso("Tipo removido com sucesso!");
        await this.carregarTipos();
      } catch (err) {
        this.mostrarErro(err.message || "Erro ao excluir tipo de agendamento.");
      }
    },

    // ==========================================
    // ALERTAS
    // ==========================================
    mostrarSucesso: function (msg) {
      const el = document.getElementById("alerta-sucesso");
      const texto = document.getElementById("alerta-sucesso-texto");
      if (el && texto) {
        texto.textContent = msg;
        el.classList.remove("hidden");
        setTimeout(() => el.classList.add("hidden"), 5000);
      }
    },

    mostrarErro: function (msg) {
      const el = document.getElementById("alerta-erro");
      const texto = document.getElementById("alerta-erro-texto");
      if (el && texto) {
        texto.textContent = msg;
        el.classList.remove("hidden");
      }
    },

    fecharAlerta: function (id) {
      const el = document.getElementById(id);
      if (el) el.classList.add("hidden");
    },
  };

  document.addEventListener("DOMContentLoaded", () => Agendamentos.init());
})();
