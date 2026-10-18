/**
 * js/recursos.js
 * Controller da tela de Recursos: Listagem, Cadastro, Edição, Exclusão e Exibição de Erros
 */

let listaRecursosCache = [];

document.addEventListener("DOMContentLoaded", () => {
  // 🔒 Se não estiver logado como Administrativo, vai para login.html ou index.html:
  const admin = Auth.exigirAdministrativo();
  if (!admin) return;

  inicializarEventos();
  carregarRecursos();
});

function inicializarEventos() {
  document
    .getElementById("btn-novo-recurso")
    .addEventListener("click", abrirModalParaCriar);
  document
    .getElementById("btn-fechar-modal")
    .addEventListener("click", fecharModal);
  document
    .getElementById("btn-cancelar-modal")
    .addEventListener("click", fecharModal);
  document
    .getElementById("form-recurso")
    .addEventListener("submit", salvarRecurso);
  document
    .getElementById("filtro-busca")
    .addEventListener("input", filtrarTabela);
}

// ======================= EXIBIÇÃO DE ERROS E ALERTAS =======================

function exibirAlerta(mensagem, tipo = "erro") {
  const alerta = document.getElementById("alerta-feedback");
  const icone = document.getElementById("alerta-icone");
  const texto = document.getElementById("alerta-mensagem");

  alerta.className =
    "rounded-lg p-4 text-sm font-medium border transition-all duration-300 mb-4 block";

  if (tipo === "erro") {
    alerta.classList.add("bg-rose-50", "border-rose-200", "text-rose-800");
    icone.textContent = "❌";
  } else if (tipo === "sucesso") {
    alerta.classList.add(
      "bg-emerald-50",
      "border-emerald-200",
      "text-emerald-800",
    );
    icone.textContent = "✅";
  } else {
    alerta.classList.add("bg-amber-50", "border-amber-200", "text-amber-800");
    icone.textContent = "⚠️";
  }

  texto.textContent = mensagem;

  // Auto-fechar em 6 segundos se for sucesso
  if (tipo === "sucesso") {
    setTimeout(fecharAlerta, 6000);
  }
}

function fecharAlerta() {
  const alerta = document.getElementById("alerta-feedback");
  alerta.classList.add("hidden");
}

function exibirErroModal(mensagem) {
  const box = document.getElementById("modal-alerta-erro");
  const texto = document.getElementById("modal-alerta-texto");
  texto.textContent = mensagem;
  box.classList.remove("hidden");
}

function fecharErroModal() {
  const box = document.getElementById("modal-alerta-erro");
  box.classList.add("hidden");
}

// ======================= R - READ (LISTAGEM) =======================

async function carregarRecursos() {
  const tbody = document.getElementById("tabela-recursos-corpo");
  tbody.innerHTML = `
    <tr>
      <td colspan="6" class="px-6 py-8 text-center text-slate-400">
        <span class="inline-block animate-spin mr-2">⏳</span> Carregando recursos do banco de dados...
      </td>
    </tr>
  `;

  try {
    const dados = await API.get("/recursos");
    listaRecursosCache = Array.isArray(dados) ? dados : dados.data || [];
    renderizarTabela(listaRecursosCache);
  } catch (erro) {
    console.error("Erro ao listar recursos:", erro);
    exibirAlerta(`Falha ao obter recursos: ${erro.message}`, "erro");
    tbody.innerHTML = `
      <tr>
        <td colspan="6" class="px-6 py-8 text-center text-rose-500 font-medium">
          Erro ao conectar com o backend: ${erro.message}
        </td>
      </tr>
    `;
  }
}

function renderizarTabela(lista) {
  const tbody = document.getElementById("tabela-recursos-corpo");

  if (!lista || lista.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="6" class="px-6 py-8 text-center text-slate-400">
          Nenhum recurso cadastrado até o momento.
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = lista
    .map(
      (item) => `
    <tr class="hover:bg-slate-50 transition border-b border-slate-100">
      <td class="px-6 py-4 font-mono text-xs text-slate-500 font-bold">#${item.id_recurso}</td>
      <td class="px-6 py-4 font-semibold text-slate-800">${escapeHtml(item.nome)}</td>
      <td class="px-6 py-4">
        ${obterBadgeTipoRecurso(item.tipo_recurso)}
      </td>
      <td class="px-6 py-4 font-mono text-xs text-slate-600">${escapeHtml(item.patrimonio)}</td>
      <td class="px-6 py-4 text-center">
        <span class="font-bold ${item.quantidade_total_disponivel > 0 ? "text-emerald-600" : "text-rose-600"}">
          ${item.quantidade_total_disponivel}
        </span>
      </td>
      <td class="px-6 py-4 text-right space-x-2">
        <button 
          onclick="abrirModalParaEditar(${item.id_recurso})" 
          class="px-2.5 py-1 text-xs font-semibold bg-blue-50 text-blue-700 hover:bg-blue-100 rounded-md transition"
        >
          Editar
        </button>
        <button 
          onclick="deletarRecurso(${item.id_recurso}, '${escapeAttr(item.nome)}')" 
          class="px-2.5 py-1 text-xs font-semibold bg-rose-50 text-rose-700 hover:bg-rose-100 rounded-md transition"
        >
          Excluir
        </button>
      </td>
    </tr>
  `,
    )
    .join("");
}

function filtrarTabela() {
  const termo = document
    .getElementById("filtro-busca")
    .value.toLowerCase()
    .trim();
  if (!termo) {
    renderizarTabela(listaRecursosCache);
    return;
  }

  const filtrados = listaRecursosCache.filter(
    (item) =>
      (item.nome && item.nome.toLowerCase().includes(termo)) ||
      (item.patrimonio && item.patrimonio.toLowerCase().includes(termo)) ||
      (item.tipo_recurso && item.tipo_recurso.toLowerCase().includes(termo)),
  );

  renderizarTabela(filtrados);
}

// ======================= C & U - CREATE E UPDATE =======================

function abrirModalParaCriar() {
  fecharErroModal();
  document.getElementById("modal-titulo").textContent =
    "Cadastrar Novo Recurso";
  document.getElementById("btn-salvar-texto").textContent = "Cadastrar";
  document.getElementById("form-recurso").reset();
  document.getElementById("campo-id").value = "";
  document.getElementById("modal-recurso").classList.remove("hidden");
}

function abrirModalParaEditar(id) {
  fecharErroModal();
  const recurso = listaRecursosCache.find((r) => r.id_recurso === id);
  if (!recurso) {
    exibirAlerta("Recurso não encontrado para edição.", "erro");
    return;
  }

  document.getElementById("modal-titulo").textContent =
    `Editar Recurso #${recurso.id_recurso}`;
  document.getElementById("btn-salvar-texto").textContent = "Atualizar";
  document.getElementById("campo-id").value = recurso.id_recurso;
  document.getElementById("campo-nome").value = recurso.nome;
  document.getElementById("campo-tipo").value = recurso.tipo_recurso;
  document.getElementById("campo-patrimonio").value = recurso.patrimonio;
  document.getElementById("campo-quantidade").value =
    recurso.quantidade_total_disponivel;

  document.getElementById("modal-recurso").classList.remove("hidden");
}

function fecharModal() {
  document.getElementById("modal-recurso").classList.add("hidden");
  fecharErroModal();
}

async function salvarRecurso(evento) {
  evento.preventDefault();
  fecharErroModal();

  const id = document.getElementById("campo-id").value;
  const nome = document.getElementById("campo-nome").value.trim();
  const tipo = document.getElementById("campo-tipo").value.trim().toUpperCase();
  const patrimonio = document.getElementById("campo-patrimonio").value.trim();
  const quantidade = parseInt(
    document.getElementById("campo-quantidade").value,
    10,
  );

  if (!nome || !tipo || !patrimonio || isNaN(quantidade)) {
    exibirErroModal("Por favor, preencha todos os campos obrigatórios.");
    return;
  }

  const payload = {
    nome: nome,
    tipo_recurso: tipo,
    patrimonio: patrimonio,
    quantidade_total_disponivel: quantidade,
  };

  const btnSalvar = document.getElementById("btn-salvar-modal");
  btnSalvar.disabled = true;
  btnSalvar.classList.add("opacity-50");

  try {
    if (id) {
      payload.id_recurso = parseInt(id, 10);
      const resp = await API.put(`/recursos?id=${id}`, payload);
      exibirAlerta(
        resp.mensagem || "Recurso atualizado com sucesso!",
        "sucesso",
      );
    } else {
      const resp = await API.post("/recursos", payload);
      exibirAlerta(
        resp.mensagem || "Recurso cadastrado com sucesso!",
        "sucesso",
      );
    }

    fecharModal();
    await carregarRecursos();
  } catch (erro) {
    console.error("Erro ao salvar recurso:", erro);
    exibirErroModal(erro.message || "Erro ao comunicar com a API.");
  } finally {
    btnSalvar.disabled = false;
    btnSalvar.classList.remove("opacity-50");
  }
}

// ======================= D - DELETE =======================

async function deletarRecurso(id, nome) {
  const confirmou = confirm(
    `Tem certeza que deseja excluir o recurso "${nome}" (ID: #${id})?`,
  );
  if (!confirmou) return;

  try {
    const resp = await API.delete(`/recursos?id=${id}`);
    exibirAlerta(resp.mensagem || "Recurso removido com sucesso!", "sucesso");
    await carregarRecursos();
  } catch (erro) {
    console.error("Erro ao deletar recurso:", erro);
    exibirAlerta(`Erro ao remover: ${erro.message}`, "erro");
  }
}

// ======================= UTILITÁRIOS =======================

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

  return `<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold ${classes}">${escapeHtml(tipo)}</span>`;
}

function escapeHtml(texto) {
  if (texto === undefined || texto === null) return "";
  const div = document.createElement("div");
  div.textContent = String(texto);
  return div.innerHTML;
}

function escapeAttr(texto) {
  if (texto === undefined || texto === null) return "";
  return String(texto).replace(/'/g, "\\'");
}
