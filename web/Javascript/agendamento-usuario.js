/**
 * web/javascript/agendamento-usuario.js
 * Interface de agendamento exclusiva para Alunos e Professores
 */
document.addEventListener("DOMContentLoaded", () => {
  // 1. Garante que está logado
  const usuarioLogado = Auth.exigirAutenticacao();
  if (!usuarioLogado) return;

  // Elementos do DOM
  const selectPredio = document.getElementById("campo-predio");
  const selectSala = document.getElementById("campo-sala");
  const selectTipo = document.getElementById("campo-tipo");
  const campoInicio = document.getElementById("campo-inicio");
  const campoFim = document.getElementById("campo-fim");
  const campoMotivo = document.getElementById("campo-motivo");
  const formAgendamento = document.getElementById("form-novo-agendamento");
  const tabelaMeusAgendamentos = document.getElementById(
    "tabela-meus-agendamentos",
  );
  const btnRecarregar = document.getElementById("btn-recarregar");
  const infoDuracao = document.getElementById("info-duracao-tipo");

  let listaPredios = [];
  let listaSalas = [];
  let listaTipos = [];
  let meusAgendamentos = [];

  // --- Utilitários de Alerta ---
  function exibirErro(msg) {
    const box = document.getElementById("box-alerta-erro");
    const txt = document.getElementById("texto-alerta-erro");
    txt.textContent = msg;
    box.classList.remove("hidden");
    document.getElementById("box-alerta-sucesso").classList.add("hidden");
  }

  function exibirSucesso(msg) {
    const box = document.getElementById("box-alerta-sucesso");
    const txt = document.getElementById("texto-alerta-sucesso");
    txt.textContent = msg;
    box.classList.remove("hidden");
    document.getElementById("box-alerta-erro").classList.add("hidden");
  }

  function formatarDataHora(isoString) {
    if (!isoString) return "-";
    try {
      const data = new Date(isoString.replace(" ", "T"));
      if (isNaN(data.getTime())) return isoString;
      return data.toLocaleString("pt-BR", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      });
    } catch (e) {
      return isoString;
    }
  }

  // Badge com cores por situação
  function obterBadgeSituacao(situacao) {
    const s = (situacao || "").toUpperCase();
    switch (s) {
      case "SOLICITADO":
        return '<span class="px-2.5 py-1 text-xs font-semibold rounded-full bg-amber-50 text-amber-700 border border-amber-200">🟡 Solicitado</span>';
      case "APROVADO":
        return '<span class="px-2.5 py-1 text-xs font-semibold rounded-full bg-blue-50 text-blue-700 border border-blue-200">🔵 Aprovado</span>';
      case "CONFIRMADO":
        return '<span class="px-2.5 py-1 text-xs font-semibold rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">🟢 Confirmado</span>';
      case "REALIZADO":
        return '<span class="px-2.5 py-1 text-xs font-semibold rounded-full bg-slate-100 text-slate-700 border border-slate-200">⚪ Realizado</span>';
      case "CANCELADO":
        return '<span class="px-2.5 py-1 text-xs font-semibold rounded-full bg-red-50 text-red-700 border border-red-200">🔴 Cancelado</span>';
      default:
        return `<span class="px-2.5 py-1 text-xs font-semibold rounded-full bg-slate-100 text-slate-600">${s}</span>`;
    }
  }

  // --- Carga dos Dados Auxiliares ---
  async function carregarPredios() {
    try {
      const predios = await API.get("/predios");
      listaPredios = Array.isArray(predios) ? predios : [];
      selectPredio.innerHTML =
        '<option value="">Selecione o prédio...</option>' +
        listaPredios
          .map(
            (p) =>
              `<option value="${p.id_predio}">Prédio ${p.nome || p.id_predio}</option>`,
          )
          .join("");
    } catch (erro) {
      selectPredio.innerHTML =
        '<option value="">Erro ao carregar prédios</option>';
    }
  }

  async function carregarSalas() {
    try {
      const salas = await API.get("/salas");
      listaSalas = Array.isArray(salas) ? salas : [];
    } catch (erro) {
      listaSalas = [];
    }
  }

  async function carregarTipos() {
    try {
      const tipos = await API.get("/tipos-agendamento");
      listaTipos = Array.isArray(tipos) ? tipos : [];
      selectTipo.innerHTML =
        '<option value="">Selecione o tipo...</option>' +
        listaTipos
          .map(
            (t) =>
              `<option value="${t.id_tipo}" data-duracao="${t.duracao_minutos}">${t.descricao} (${t.duracao_minutos} min)</option>`,
          )
          .join("");
    } catch (erro) {
      selectTipo.innerHTML = '<option value="">Erro ao carregar tipos</option>';
    }
  }

  // Filtrar salas conforme o prédio selecionado
  selectPredio.addEventListener("change", () => {
    const idPredio = selectPredio.value;
    if (!idPredio) {
      selectSala.disabled = true;
      selectSala.classList.add("bg-slate-50");
      selectSala.innerHTML =
        '<option value="">Selecione primeiro um prédio</option>';
      return;
    }

    const salasDoPredio = listaSalas.filter(
      (s) => String(s.id_predio) === String(idPredio),
    );
    if (salasDoPredio.length === 0) {
      selectSala.disabled = true;
      selectSala.innerHTML =
        '<option value="">Nenhuma sala cadastrada neste prédio</option>';
      return;
    }

    selectSala.disabled = false;
    selectSala.classList.remove("bg-slate-50");
    selectSala.innerHTML =
      '<option value="">Selecione a sala...</option>' +
      salasDoPredio
        .map(
          (s) =>
            `<option value="${s.numero_sala}">Sala ${s.numero_sala} (${s.tipo_sala || "Geral"})</option>`,
        )
        .join("");
  });

  // Auto-ajuste de horário de fim conforme a duração do tipo selecionado
  selectTipo.addEventListener("change", () => {
    const option = selectTipo.selectedOptions[0];
    const duracao = option ? option.dataset.duracao : null;
    if (duracao) {
      infoDuracao.textContent = `Duração prevista: ${duracao} minutos`;
      if (campoInicio.value) {
        const inicioDate = new Date(campoInicio.value);
        if (!isNaN(inicioDate.getTime())) {
          inicioDate.setMinutes(inicioDate.getMinutes() + parseInt(duracao));
          campoFim.value = inicioDate.toISOString().slice(0, 16);
        }
      }
    } else {
      infoDuracao.textContent = "";
    }
  });

  campoInicio.addEventListener("change", () => {
    const option = selectTipo.selectedOptions[0];
    const duracao = option ? option.dataset.duracao : null;
    if (duracao && campoInicio.value) {
      const inicioDate = new Date(campoInicio.value);
      if (!isNaN(inicioDate.getTime())) {
        inicioDate.setMinutes(inicioDate.getMinutes() + parseInt(duracao));
        campoFim.value = inicioDate.toISOString().slice(0, 16);
      }
    }
  });

  // --- Listagem de Meus Agendamentos ---
  async function carregarMeusAgendamentos() {
    tabelaMeusAgendamentos.innerHTML = `
            <tr>
                <td colspan="5" class="py-6 text-center text-slate-400">Carregando seus agendamentos...</td>
            </tr>
        `;

    try {
      const todos = await API.get("/agendamentos");
      const lista = Array.isArray(todos) ? todos : [];

      // Filtra apenas os agendamentos pertencentes ao usuário logado
      const idMeuUsuario = usuarioLogado.id_usuario;
      meusAgendamentos = lista.filter(
        (a) => Number(a.id_usuario_solicitante) === Number(idMeuUsuario),
      );

      renderizarTabela();
    } catch (erro) {
      tabelaMeusAgendamentos.innerHTML = `
                <tr>
                    <td colspan="5" class="py-6 text-center text-red-500 font-medium">
                        Não foi possível carregar seus agendamentos. ${erro.message || ""}
                    </td>
                </tr>
            `;
    }
  }

  function renderizarTabela() {
    if (meusAgendamentos.length === 0) {
      tabelaMeusAgendamentos.innerHTML = `
                <tr>
                    <td colspan="5" class="py-8 text-center text-slate-400">
                        Você ainda não solicitou nenhum agendamento.
                    </td>
                </tr>
            `;
      return;
    }

    tabelaMeusAgendamentos.innerHTML = meusAgendamentos
      .map((a) => {
        const predio = listaPredios.find(
          (p) => Number(p.id_predio) === Number(a.id_predio),
        );
        const nomePredio = predio
          ? predio.nome || `Prédio ${a.id_predio}`
          : `Prédio ${a.id_predio}`;
        const tipo = listaTipos.find(
          (t) => Number(t.id_tipo) === Number(a.id_tipo),
        );
        const nomeTipo = tipo ? tipo.descricao : `Tipo #${a.id_tipo}`;

        const podeCancelar =
          a.situacao_atual === "SOLICITADO" || a.situacao_atual === "APROVADO";

        return `
                <tr class="hover:bg-slate-50 transition">
                    <td class="py-3 px-3">
                        <p class="font-bold text-slate-800">${nomePredio}</p>
                        <p class="text-xs text-slate-500">Sala ${a.numero_sala}</p>
                    </td>
                    <td class="py-3 px-3">
                        <p class="text-xs font-semibold text-slate-700">${formatarDataHora(a.data_hora_inicio)}</p>
                        <p class="text-xs text-slate-400">até ${formatarDataHora(a.data_hora_fim)}</p>
                    </td>
                    <td class="py-3 px-3">
                        <p class="text-xs font-semibold text-slate-800">${nomeTipo}</p>
                        <p class="text-xs text-slate-500 truncate max-w-xs" title="${a.motivo_descricao || ""}">${a.motivo_descricao || "-"}</p>
                    </td>
                    <td class="py-3 px-3">
                        ${obterBadgeSituacao(a.situacao_atual)}
                    </td>
                    <td class="py-3 px-3 text-right">
                        ${
                          podeCancelar
                            ? `
                            <button onclick="cancelarAgendamento(${a.id_agendamento})" class="px-2.5 py-1 text-xs font-medium text-red-600 hover:bg-red-50 rounded-lg transition border border-red-200">
                                Cancelar
                            </button>
                        `
                            : `
                            <span class="text-xs text-slate-400">-</span>
                        `
                        }
                    </td>
                </tr>
            `;
      })
      .join("");
  }

  // --- Submissão de Novo Agendamento ---
  formAgendamento.addEventListener("submit", async (e) => {
    e.preventDefault();

    const idPredio = selectPredio.value;
    const numeroSala = selectSala.value;
    const idTipo = selectTipo.value;
    const inicioValor = campoInicio.value;
    const fimValor = campoFim.value;
    const motivo = campoMotivo.value.trim();

    if (
      !idPredio ||
      !numeroSala ||
      !idTipo ||
      !inicioValor ||
      !fimValor ||
      !motivo
    ) {
      exibirErro("Preencha todos os campos obrigatórios.");
      return;
    }

    // Valida se fim é maior que início
    if (new Date(fimValor) <= new Date(inicioValor)) {
      exibirErro(
        "A data/hora de término deve ser posterior à data/hora de início.",
      );
      return;
    }

    // Monta o payload exatamente com os campos esperados pelo AgendamentoController
    const payload = {
      id_usuario: usuarioLogado.id_usuario,
      id_predio: parseInt(idPredio),
      numero_sala: numeroSala,
      id_tipo: parseInt(idTipo),
      inicio: inicioValor.replace("T", " ") + ":00",
      fim: fimValor.replace("T", " ") + ":00",
      motivo: motivo,
      situacao: "SOLICITADO",
    };

    const btnSubmit = document.getElementById("btn-enviar-agendamento");
    btnSubmit.disabled = true;
    btnSubmit.textContent = "Enviando...";

    try {
      await API.post("/agendamentos", payload);
      exibirSucesso("Agendamento solicitado com sucesso!");
      formAgendamento.reset();
      selectSala.disabled = true;
      selectSala.classList.add("bg-slate-50");
      infoDuracao.textContent = "";
      await carregarMeusAgendamentos();
    } catch (erro) {
      exibirErro(
        erro.message ||
          "Erro ao solicitar agendamento. Verifique se o horário está disponível.",
      );
    } finally {
      btnSubmit.disabled = false;
      btnSubmit.textContent = "Confirmar Agendamento";
    }
  });

  // Função global de cancelamento
  window.cancelarAgendamento = async (idAgendamento) => {
    if (!confirm("Deseja realmente cancelar esta solicitação de agendamento?"))
      return;

    try {
      await API.delete(`/agendamentos?id=${idAgendamento}`);
      exibirSucesso("Agendamento cancelado com sucesso.");
      await carregarMeusAgendamentos();
    } catch (erro) {
      exibirErro(erro.message || "Erro ao cancelar o agendamento.");
    }
  };

  btnRecarregar.addEventListener("click", carregarMeusAgendamentos);

  // Inicialização
  Promise.all([carregarPredios(), carregarSalas(), carregarTipos()]).then(
    () => {
      carregarMeusAgendamentos();
    },
  );
});
