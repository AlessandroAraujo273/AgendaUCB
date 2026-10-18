/**
 * javascript/dashboard.js
 * Controller da página inicial (Dashboard / Visão Geral)
 */

document.addEventListener("DOMContentLoaded", () => {
  carregarDashboard();
});

async function carregarDashboard() {
  const cardTotalRecursos = document.getElementById("card-total-recursos");
  const cardTotalAlunos = document.getElementById("card-total-alunos");
  const cardTotalReservas = document.getElementById("card-total-reservas");
  const tabelaCorpo = document.getElementById("tabela-ultimos-recursos");

  try {
    // 1. Busca Recursos, Usuários e Agendamentos em paralelo
    const [respRecursos, respUsuarios, respAgendamentos] = await Promise.all([
      API.get("/recursos").catch((err) => {
        console.error("Erro ao buscar recursos:", err);
        return [];
      }),
      API.get("/usuarios").catch((err) => {
        console.error("Erro ao buscar usuários:", err);
        return [];
      }),
      API.get("/agendamentos").catch((err) => {
        console.error("Erro ao buscar agendamentos:", err);
        return [];
      }),
    ]);

    const recursos = Array.isArray(respRecursos)
      ? respRecursos
      : respRecursos.data || [];
    const usuarios = Array.isArray(respUsuarios)
      ? respUsuarios
      : respUsuarios.data || [];
    const agendamentos = Array.isArray(respAgendamentos)
      ? respAgendamentos
      : respAgendamentos.data || [];

    // 2. Card 1: Total de Recursos
    if (cardTotalRecursos) {
      cardTotalRecursos.textContent = recursos.length;
    }

    // 3. Card 2: Total de Alunos (filtra tipo_usuario === 'ALUNO')
    if (cardTotalAlunos) {
      const totalAlunos = usuarios.filter(
        (u) => u.tipo_usuario && u.tipo_usuario.toUpperCase() === "ALUNO",
      ).length;
      cardTotalAlunos.textContent = totalAlunos;
    }

    // 4. Card 3: Total Real de Agendamentos / Reservas
    if (cardTotalReservas) {
      cardTotalReservas.textContent = agendamentos.length;
    }

    // 5. Tabela de Recursos Recentes (máx. 5 itens)
    if (tabelaCorpo) {
      renderizarTabelaRecentes(recursos.slice(0, 5), tabelaCorpo);
    }
  } catch (erro) {
    console.error("Erro geral ao carregar dados do dashboard:", erro);
    if (cardTotalRecursos) cardTotalRecursos.textContent = "0";
    if (cardTotalAlunos) cardTotalAlunos.textContent = "0";
    if (cardTotalReservas) cardTotalReservas.textContent = "0";
  }
}

/**
 * Renderiza as linhas da tabela de prévia de recursos
 */
function renderizarTabelaRecentes(lista, elementoTbody) {
  if (!elementoTbody) return;

  if (lista.length === 0) {
    elementoTbody.innerHTML = `
      <tr>
        <td colspan="5" class="px-6 py-6 text-center text-slate-400 text-xs">
          Nenhum recurso cadastrado no momento.
        </td>
      </tr>
    `;
    return;
  }

  // Cores de badge por tipo de recurso
  function obterBadgeTipoRecurso(tipo) {
    const t = (tipo || "").toUpperCase();
    let classes = "bg-slate-100 text-slate-700 border border-slate-200";

    switch (t) {
      case "EQUIPAMENTO":
        classes = "bg-blue-100 text-blue-700 border border-blue-200";
        break;
      case "CONSUMO":
        classes = "bg-amber-100 text-amber-800 border border-amber-200";
        break;
      case "CHAVE":
        classes = "bg-emerald-100 text-emerald-700 border border-emerald-200";
        break;
      default:
        classes = "bg-purple-100 text-purple-700 border border-purple-200";
    }

    return `<span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold ${classes}">${escapeHtml(tipo || "Geral")}</span>`;
  }

  elementoTbody.innerHTML = lista
    .map((item, index) => {
      const id = item.id_recurso || item.id || index + 1;
      const nome = item.nome || "Sem nome";
      const tipo = item.tipo_recurso || "Geral";
      const patrimonio = item.patrimonio || "-";
      const quantidade =
        item.quantidade_total_disponivel !== undefined
          ? item.quantidade_total_disponivel
          : 1;

      return `
      <tr class="hover:bg-slate-50 transition border-b border-slate-100">
        <td class="px-6 py-4 font-mono text-xs text-slate-500 font-bold">#${id}</td>
        <td class="px-6 py-4 font-semibold text-slate-800">${escapeHtml(nome)}</td>
        <td class="px-6 py-4">${obterBadgeTipoRecurso(tipo)}</td>
        <td class="px-6 py-4 font-mono text-xs text-slate-600">${escapeHtml(patrimonio)}</td>
        <td class="px-6 py-4 text-center">
          <span class="font-bold ${quantidade > 0 ? "text-emerald-600" : "text-rose-600"}">
            ${quantidade} unid.
          </span>
        </td>
      </tr>
    `;
    })
    .join("");
}

function escapeHtml(texto) {
  if (texto === undefined || texto === null) return "";
  const div = document.createElement("div");
  div.textContent = String(texto);
  return div.innerHTML;
}
