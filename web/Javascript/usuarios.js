/**
 * javascript/usuarios.js
 * Controller de Usuários - Acesso restrito a ADMINISTRATIVO
 */

let listaUsuariosCache = [];

document.addEventListener("DOMContentLoaded", () => {
  // 1. Barreira de Proteção: Apenas Administrativo entra
  const admin = Auth.exigirAdministrativo();
  if (!admin) return;

  inicializarEventos();
  carregarUsuarios();
});

/**
 * Valida os 11 dígitos do CPF aplicando o cálculo dos dígitos verificadores
 */
function validarCPF(cpf) {
    // Remove qualquer caractere que não seja número
    const limpo = String(cpf).replace(/\D/g, '');

    // Deve ter exatamente 11 dígitos
    if (limpo.length !== 11) return false;

    // Elimina CPFs com todos os dígitos iguais (ex: 111.111.111-11, 000.000.000-00)
    if (/^(\d)\1{10}$/.test(limpo)) return false;

    // Cálculo do 1º Dígito Verificador
    let soma = 0;
    for (let i = 0; i < 9; i++) {
        soma += parseInt(limpo.charAt(i)) * (10 - i);
    }
    let resto = (soma * 10) % 11;
    if (resto === 10 || resto === 11) resto = 0;
    if (resto !== parseInt(limpo.charAt(9))) return false;

    // Cálculo do 2º Dígito Verificador
    soma = 0;
    for (let i = 0; i < 10; i++) {
        soma += parseInt(limpo.charAt(i)) * (11 - i);
    }
    resto = (soma * 10) % 11;
    if (resto === 10 || resto === 11) resto = 0;
    if (resto !== parseInt(limpo.charAt(10))) return false;

    return true;
}




function inicializarEventos() {
  document
    .getElementById("btn-novo-usuario")
    .addEventListener("click", abrirModalParaCriar);
  document
    .getElementById("btn-fechar-modal")
    .addEventListener("click", fecharModal);
  document
    .getElementById("btn-cancelar-modal")
    .addEventListener("click", fecharModal);
  document
    .getElementById("form-usuario")
    .addEventListener("submit", salvarUsuario);
  document
    .getElementById("filtro-busca")
    .addEventListener("input", filtrarTabela);

  // Exibe/oculta campos de aluno dinamicamente
  document.getElementById("campo-tipo").addEventListener("change", (e) => {
    const extraAluno = document.getElementById("extra-aluno");
    if (e.target.value === "ALUNO") {
      extraAluno.classList.remove("hidden");
    } else {
      extraAluno.classList.add("hidden");
    }
  });
}

// ======================= FEEDBACK DE ERRO / SUCESSO =======================

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
  }

  texto.textContent = mensagem;
  if (tipo === "sucesso") setTimeout(fecharAlerta, 5000);
}

function fecharAlerta() {
  document.getElementById("alerta-feedback").classList.add("hidden");
}

function exibirErroModal(msg) {
  const box = document.getElementById("modal-alerta-erro");
  document.getElementById("modal-alerta-texto").textContent = msg;
  box.classList.remove("hidden");
}

function fecharErroModal() {
  document.getElementById("modal-alerta-erro").classList.add("hidden");
}

// ======================= READ (LISTAGEM) =======================

async function carregarUsuarios() {
  const tbody = document.getElementById("tabela-usuarios-corpo");
  tbody.innerHTML = `
    <tr>
      <td colspan="6" class="px-6 py-8 text-center text-slate-400">
        <span class="inline-block animate-spin mr-2">⏳</span> Carregando usuários...
      </td>
    </tr>
  `;

  try {
    const dados = await API.get("/usuarios");
    listaUsuariosCache = Array.isArray(dados) ? dados : dados.data || [];
    renderizarTabela(listaUsuariosCache);
  } catch (erro) {
    console.error("Erro ao listar usuários:", erro);
    exibirAlerta(`Falha ao obter usuários: ${erro.message}`, "erro");
    tbody.innerHTML = `
      <tr>
        <td colspan="6" class="px-6 py-8 text-center text-rose-500 font-medium">
          Erro ao consultar /api/usuarios: ${erro.message}
        </td>
      </tr>
    `;
  }
}

function renderizarTabela(lista) {
  const tbody = document.getElementById("tabela-usuarios-corpo");

  if (!lista || lista.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="6" class="px-6 py-8 text-center text-slate-400">
          Nenhum usuário cadastrado.
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = lista
    .map((item) => {
      const nomeCompleto =
        `${item.nome || ""} ${item.sobrenome || ""}`.trim() || "Sem nome";
      const tipo = item.tipo_usuario || "ALUNO";

      return `
      <tr class="hover:bg-slate-50 transition border-b border-slate-100">
        <td class="px-6 py-4 font-mono text-xs text-slate-500 font-bold">#${item.id_usuario}</td>
        <td class="px-6 py-4 font-semibold text-slate-800">${escapeHtml(nomeCompleto)}</td>
        <td class="px-6 py-4 text-slate-600">${escapeHtml(item.email_institucional)}</td>
        <td class="px-6 py-4 font-mono text-xs text-slate-600">${escapeHtml(item.cpf)}</td>
        <td class="px-6 py-4 text-center">
          <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold ${
            tipo === "ADMINISTRATIVO"
              ? "bg-purple-100 text-purple-700"
              : tipo === "PROFESSOR"
                ? "bg-amber-100 text-amber-700"
                : "bg-blue-100 text-blue-700"
          }">
            ${tipo}
          </span>
        </td>
        <td class="px-6 py-4 text-right space-x-2">
          <button 
            onclick="abrirModalParaEditar(${item.id_usuario})" 
            class="px-2.5 py-1 text-xs font-semibold bg-blue-50 text-blue-700 hover:bg-blue-100 rounded-md transition"
          >
            Editar
          </button>
          <button 
            onclick="deletarUsuario(${item.id_usuario}, '${escapeAttr(nomeCompleto)}')" 
            class="px-2.5 py-1 text-xs font-semibold bg-rose-50 text-rose-700 hover:bg-rose-100 rounded-md transition"
          >
            Excluir
          </button>
        </td>
      </tr>
    `;
    })
    .join("");
}

function filtrarTabela() {
  const termo = document
    .getElementById("filtro-busca")
    .value.toLowerCase()
    .trim();
  if (!termo) {
    renderizarTabela(listaUsuariosCache);
    return;
  }

  const filtrados = listaUsuariosCache.filter((item) => {
    const nomeCompleto = `${item.nome} ${item.sobrenome}`.toLowerCase();
    const email = (item.email_institucional || "").toLowerCase();
    const cpf = (item.cpf || "").toLowerCase();
    return (
      nomeCompleto.includes(termo) ||
      email.includes(termo) ||
      cpf.includes(termo)
    );
  });

  renderizarTabela(filtrados);
}

// ======================= CREATE & UPDATE =======================

function abrirModalParaCriar() {
  fecharErroModal();
  document.getElementById("modal-titulo").textContent =
    "Cadastrar Novo Usuário";
  document.getElementById("btn-salvar-texto").textContent = "Cadastrar";
  document.getElementById("form-usuario").reset();
  document.getElementById("campo-id").value = "";
  document.getElementById("campo-senha").required = true;
  document.getElementById("label-senha").textContent = "Senha *";
  document.getElementById("ajuda-senha").classList.add("hidden");
  document.getElementById("extra-aluno").classList.remove("hidden");
  document.getElementById("modal-usuario").classList.remove("hidden");
}

function abrirModalParaEditar(id) {
  fecharErroModal();
  const u = listaUsuariosCache.find((item) => item.id_usuario === id);
  if (!u) {
    exibirAlerta("Usuário não localizado.", "erro");
    return;
  }

  document.getElementById("modal-titulo").textContent =
    `Editar Usuário #${u.id_usuario}`;
  document.getElementById("btn-salvar-texto").textContent = "Atualizar";
  document.getElementById("campo-id").value = u.id_usuario;
  document.getElementById("campo-nome").value = u.nome || "";
  document.getElementById("campo-sobrenome").value = u.sobrenome || "";
  document.getElementById("campo-cpf").value = u.cpf || "";
  document.getElementById("campo-data").value = u.data_nascimento || "";
  document.getElementById("campo-email").value = u.email_institucional || "";
  document.getElementById("campo-tipo").value = u.tipo_usuario || "ALUNO";
  document.getElementById("campo-departamento").value = u.id_departamento || 1;

  // Na edição a senha é opcional
  document.getElementById("campo-senha").value = "";
  document.getElementById("campo-senha").required = false;
  document.getElementById("label-senha").textContent = "Nova Senha (Opcional)";
  document.getElementById("ajuda-senha").classList.remove("hidden");
  document.getElementById("extra-aluno").classList.add("hidden");

  document.getElementById("modal-usuario").classList.remove("hidden");
}

function fecharModal() {
  document.getElementById("modal-usuario").classList.add("hidden");
  fecharErroModal();
}

async function salvarUsuario(e) {
  e.preventDefault();
  fecharErroModal();

  const id = document.getElementById("campo-id").value;
  const tipo = document.getElementById("campo-tipo").value;

  const payload = {
    nome: document.getElementById("campo-nome").value.trim(),
    sobrenome: document.getElementById("campo-sobrenome").value.trim(),
    cpf: document.getElementById("campo-cpf").value.trim(),
    data_nascimento: document.getElementById("campo-data").value,
    email_institucional: document.getElementById("campo-email").value.trim(),
    tipo_usuario: tipo,
    id_departamento: parseInt(
      document.getElementById("campo-departamento").value,
      10,
    ),
  };

  const senha = document.getElementById("campo-senha").value;
  if (senha && senha.trim().length > 0) {
    payload.senha = senha;
  }

  // Se for criação de aluno
  if (!id && tipo === "ALUNO") {
    payload.matricula = document.getElementById("campo-matricula").value.trim();
    payload.curso = document.getElementById("campo-curso").value.trim();
    payload.semestre_atual = document.getElementById("campo-semestre").value;
  }

  const btn = document.getElementById("btn-salvar-modal");
  btn.disabled = true;

  try {
    if (id) {
      payload.id_usuario = parseInt(id, 10);
      const resp = await API.put(`/usuarios?id=${id}`, payload);
      exibirAlerta(
        resp.mensagem || "Usuário atualizado com sucesso!",
        "sucesso",
      );
    } else {
      if (!senha) {
        throw new Error(
          "A senha é obrigatória para cadastrar um novo usuário.",
        );
      }
      const resp = await API.post("/usuarios", payload);
      exibirAlerta(
        resp.mensagem || "Usuário cadastrado com sucesso!",
        "sucesso",
      );
    }

    fecharModal();
    await carregarUsuarios();
  } catch (erro) {
    console.error("Erro ao salvar usuário:", erro);
    exibirErroModal(erro.message || "Erro de comunicação com o servidor.");
  } finally {
    btn.disabled = false;
  }
}

// ======================= DELETE =======================

async function deletarUsuario(id, nome) {
  const adminLogado = Auth.obterUsuarioLogado();
  if (adminLogado && adminLogado.id_usuario === id) {
    alert("Você não pode excluir a sua própria conta logada.");
    return;
  }

  const confirmou = confirm(
    `Tem certeza que deseja excluir o usuário "${nome}" (ID: #${id})?`,
  );
  if (!confirmou) return;

  try {
    const resp = await API.delete(`/usuarios?id=${id}`);
    exibirAlerta(resp.mensagem || "Usuário removido com sucesso!", "sucesso");
    await carregarUsuarios();
  } catch (erro) {
    console.error("Erro ao deletar usuário:", erro);
    exibirAlerta(`Erro ao excluir: ${erro.message}`, "erro");
  }
}

function escapeHtml(str) {
  if (!str) return "";
  const d = document.createElement("div");
  d.textContent = String(str);
  return d.innerHTML;
}

function escapeAttr(str) {
  if (!str) return "";
  return String(str).replace(/'/g, "\\'");
}
