/**
 * javascript/dependencias.js
 * Gerenciamento de Prédios e Salas (CRUD Completo com Vanilla JS)
 * Restrito exclusivamente a usuários ADMINISTRATIVO
 */

(function () {
  let listaPredios = [];
  let listaSalas = [];
  let abaAtiva = 'predios';
  let modoEdicaoSala = false;

  // Função auxiliar para cores de badges por tipo de sala
  function obterBadgeTipoSala(tipo) {
    const t = (tipo || '').toUpperCase();
    let classes = 'bg-slate-100 text-slate-700';

    switch (t) {
      case 'AULA':
        classes = 'bg-blue-100 text-blue-700 border border-blue-200';
        break;
      case 'LABORATORIO':
        classes = 'bg-purple-100 text-purple-700 border border-purple-200';
        break;
      case 'AUDITORIO':
        classes = 'bg-amber-100 text-amber-800 border border-amber-200';
        break;
      case 'REUNIAO':
        classes = 'bg-emerald-100 text-emerald-700 border border-emerald-200';
        break;
      case 'INFORMATICA':
        classes = 'bg-indigo-100 text-indigo-700 border border-indigo-200';
        break;
      default:
        classes = 'bg-slate-100 text-slate-700 border border-slate-200';
    }

    return `<span class="px-2.5 py-0.5 rounded-full text-xs font-semibold ${classes}">${tipo || 'AULA'}</span>`;
  }

  window.Dependencias = {
    init: async function () {
      if (!window.Auth || !window.Auth.exigirAdministrativo()) {
        return;
      }

      await this.carregarPredios();
      await this.carregarSalas();
    },

    trocarAba: function (aba) {
      abaAtiva = aba;
      const secaoPredios = document.getElementById('secao-predios');
      const secaoSalas = document.getElementById('secao-salas');
      const btnPredios = document.getElementById('tab-btn-predios');
      const btnSalas = document.getElementById('tab-btn-salas');

      if (aba === 'predios') {
        secaoPredios.classList.remove('hidden');
        secaoSalas.classList.add('hidden');
        btnPredios.className = 'px-5 py-2.5 font-semibold text-sm rounded-lg bg-blue-600 text-white shadow-sm flex items-center gap-2 transition';
        btnSalas.className = 'px-5 py-2.5 font-semibold text-sm rounded-lg text-slate-600 hover:bg-slate-200 transition flex items-center gap-2';
      } else {
        secaoSalas.classList.remove('hidden');
        secaoPredios.classList.add('hidden');
        btnSalas.className = 'px-5 py-2.5 font-semibold text-sm rounded-lg bg-blue-600 text-white shadow-sm flex items-center gap-2 transition';
        btnPredios.className = 'px-5 py-2.5 font-semibold text-sm rounded-lg text-slate-600 hover:bg-slate-200 transition flex items-center gap-2';
      }
    },

    // ==========================================
    // MÓDULO PRÉDIOS
    // ==========================================
    carregarPredios: async function () {
      try {
        const dados = await API.get('/predios');
        listaPredios = Array.isArray(dados) ? dados : [];
        this.atualizarContadorPredios();
        this.renderizarPredios(listaPredios);
        this.atualizarSelectsPredios();
      } catch (err) {
        this.mostrarErro('Erro ao carregar lista de prédios: ' + (err.message || 'Falha de conexão'));
      }
    },

    atualizarContadorPredios: function () {
      const el = document.getElementById('contador-predios');
      if (el) el.textContent = listaPredios.length;
    },

    atualizarSelectsPredios: function () {
      const filtro = document.getElementById('filtro-sala-predio');
      if (filtro) {
        const valorAtual = filtro.value;
        filtro.innerHTML = '<option value="">Todos os Prédios</option>' +
          listaPredios.map(p => `<option value="${p.id_predio}">${p.nome} (ID ${p.id_predio})</option>`).join('');
        filtro.value = valorAtual;
      }

      const selectModal = document.getElementById('sala-predio');
      if (selectModal) {
        selectModal.innerHTML = '<option value="">Selecione o Prédio</option>' +
          listaPredios.map(p => `<option value="${p.id_predio}">${p.nome} (ID ${p.id_predio})</option>`).join('');
      }
    },

    renderizarPredios: function (predios) {
      const tbody = document.getElementById('tabela-predios-corpo');
      if (!tbody) return;

      if (!predios || predios.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="p-8 text-center text-slate-400">Nenhum prédio cadastrado.</td></tr>`;
        return;
      }

      tbody.innerHTML = predios.map(p => `
        <tr class="hover:bg-slate-50 transition border-b border-slate-100">
          <td class="p-4 font-mono font-bold text-slate-500 text-xs">#${p.id_predio}</td>
          <td class="p-4 font-semibold text-slate-900">${p.nome || '-'}</td>
          <td class="p-4 text-slate-600">${p.logradouro || '-'}</td>
          <td class="p-4 font-mono text-slate-600 text-xs">${p.cep || '-'}</td>
          <td class="p-4 text-center font-medium">${p.quantidade_andares || p.qntd_andares || 1} andar(es)</td>
          <td class="p-4 text-right space-x-2 whitespace-nowrap">
            <button onclick="Dependencias.editarPredio(${p.id_predio})" class="px-2.5 py-1 text-xs font-semibold bg-blue-50 text-blue-700 hover:bg-blue-100 rounded-md transition shadow-sm">
              Editar
            </button>
            <button onclick="Dependencias.excluirPredio(${p.id_predio}, '${(p.nome || '').replace(/'/g, "\\'")}')" class="px-2.5 py-1 text-xs font-semibold bg-rose-50 text-rose-700 hover:bg-rose-100 rounded-md transition shadow-sm">
              Excluir
            </button>
          </td>
        </tr>
      `).join('');
    },

    filtrarPredios: function () {
      const termo = (document.getElementById('busca-predio').value || '').toLowerCase().trim();
      if (!termo) {
        this.renderizarPredios(listaPredios);
        return;
      }
      const filtrados = listaPredios.filter(p => 
        (p.nome && p.nome.toLowerCase().includes(termo)) ||
        (p.logradouro && p.logradouro.toLowerCase().includes(termo)) ||
        (p.cep && p.cep.includes(termo))
      );
      this.renderizarPredios(filtrados);
    },

    abrirModalPredio: function (predio = null) {
      document.getElementById('form-predio').reset();
      const titulo = document.getElementById('modal-predio-titulo');
      const campoId = document.getElementById('predio-id');

      if (predio) {
        titulo.textContent = 'Editar Prédio #' + predio.id_predio;
        campoId.value = predio.id_predio;
        document.getElementById('predio-nome').value = predio.nome || '';
        document.getElementById('predio-logradouro').value = predio.logradouro || '';
        document.getElementById('predio-cep').value = predio.cep || '';
        document.getElementById('predio-andares').value = predio.quantidade_andares || predio.qntd_andares || 1;
      } else {
        titulo.textContent = 'Novo Prédio';
        campoId.value = '';
      }

      document.getElementById('modal-predio').classList.remove('hidden');
    },

    fecharModalPredio: function () {
      document.getElementById('modal-predio').classList.add('hidden');
    },

    salvarPredio: async function (e) {
      e.preventDefault();
      const btn = document.getElementById('btn-salvar-predio');
      btn.disabled = true;
      btn.textContent = 'Salvando...';

      const id = document.getElementById('predio-id').value;
      const dados = {
        nome: document.getElementById('predio-nome').value.trim(),
        logradouro: document.getElementById('predio-logradouro').value.trim(),
        cep: document.getElementById('predio-cep').value.trim(),
        quantidade_andares: parseInt(document.getElementById('predio-andares').value, 10)
      };

      try {
        if (id) {
          dados.id_predio = parseInt(id, 10);
          await API.put('/predios', dados);
          this.mostrarSucesso('Prédio atualizado com sucesso!');
        } else {
          await API.post('/predios', dados);
          this.mostrarSucesso('Prédio cadastrado com sucesso!');
        }
        this.fecharModalPredio();
        await this.carregarPredios();
      } catch (err) {
        this.mostrarErro(err.message || 'Erro ao salvar o prédio.');
      } finally {
        btn.disabled = false;
        btn.textContent = 'Salvar Prédio';
      }
    },

    editarPredio: function (id) {
      const predio = listaPredios.find(p => p.id_predio === id);
      if (predio) {
        this.abrirModalPredio(predio);
      }
    },

    excluirPredio: async function (id, nome) {
      if (!confirm(`Tem certeza que deseja excluir o prédio "${nome}" (ID ${id})?\n\nAtenção: a exclusão falhará caso haja salas vinculadas a ele.`)) {
        return;
      }

      try {
        await API.delete('/predios?id=' + id);
        this.mostrarSucesso('Prédio excluído com sucesso!');
        await this.carregarPredios();
        await this.carregarSalas();
      } catch (err) {
        this.mostrarErro(err.message || 'Erro ao excluir o prédio.');
      }
    },

    // ==========================================
    // MÓDULO SALAS
    // ==========================================
    carregarSalas: async function () {
      const filtroPredio = document.getElementById('filtro-sala-predio')?.value;
      const url = filtroPredio ? `/salas?id_predio=${filtroPredio}` : '/salas';

      try {
        const dados = await API.get(url);
        listaSalas = Array.isArray(dados) ? dados : [];
        this.atualizarContadorSalas();
        this.renderizarSalas(listaSalas);
      } catch (err) {
        this.mostrarErro('Erro ao carregar salas: ' + (err.message || 'Falha de conexão'));
      }
    },

    atualizarContadorSalas: function () {
      const el = document.getElementById('contador-salas');
      if (el) el.textContent = listaSalas.length;
    },

    obterNomePredio: function (idPredio) {
      const predio = listaPredios.find(p => p.id_predio === idPredio);
      return predio ? predio.nome : `Prédio #${idPredio}`;
    },

    renderizarSalas: function (salas) {
      const tbody = document.getElementById('tabela-salas-corpo');
      if (!tbody) return;

      if (!salas || salas.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="p-8 text-center text-slate-400">Nenhuma sala encontrada.</td></tr>`;
        return;
      }

      tbody.innerHTML = salas.map(s => {
        const temProjetor = String(s.possui_projetor) === 'true';
        return `
          <tr class="hover:bg-slate-50 transition border-b border-slate-100">
            <td class="p-4 font-semibold text-slate-800">${this.obterNomePredio(s.id_predio)}</td>
            <td class="p-4 font-mono font-bold text-blue-600">${s.numero_sala}</td>
            <td class="p-4">
              ${obterBadgeTipoSala(s.tipo_sala)}
            </td>
            <td class="p-4 text-center font-medium">${s.capacidade} alunos</td>
            <td class="p-4 text-center">
              <span class="px-2.5 py-0.5 rounded-full text-xs font-semibold ${
                temProjetor ? 'bg-emerald-100 text-emerald-700 border border-emerald-200' : 'bg-slate-100 text-slate-500 border border-slate-200'
              }">
                ${temProjetor ? '✓ Sim' : '✕ Não'}
              </span>
            </td>
            <td class="p-4 text-right space-x-2 whitespace-nowrap">
              <button onclick="Dependencias.editarSala(${s.id_predio}, '${s.numero_sala}')" class="px-2.5 py-1 text-xs font-semibold bg-blue-50 text-blue-700 hover:bg-blue-100 rounded-md transition shadow-sm">
                Editar
              </button>
              <button onclick="Dependencias.excluirSala(${s.id_predio}, '${s.numero_sala}')" class="px-2.5 py-1 text-xs font-semibold bg-rose-50 text-rose-700 hover:bg-rose-100 rounded-md transition shadow-sm">
                Excluir
              </button>
            </td>
          </tr>
        `;
      }).join('');
    },

    filtrarSalas: function () {
      const termo = (document.getElementById('busca-sala').value || '').toLowerCase().trim();
      if (!termo) {
        this.renderizarSalas(listaSalas);
        return;
      }
      const filtradas = listaSalas.filter(s =>
        (s.numero_sala && s.numero_sala.toLowerCase().includes(termo)) ||
        (s.tipo_sala && s.tipo_sala.toLowerCase().includes(termo))
      );
      this.renderizarSalas(filtradas);
    },

    abrirModalSala: function (sala = null) {
      document.getElementById('form-sala').reset();
      const titulo = document.getElementById('modal-sala-titulo');
      const selectPredio = document.getElementById('sala-predio');
      const inputNumero = document.getElementById('sala-numero');

      if (sala) {
        modoEdicaoSala = true;
        titulo.textContent = `Editar Sala ${sala.numero_sala}`;
        selectPredio.value = sala.id_predio;
        selectPredio.disabled = true;
        inputNumero.value = sala.numero_sala;
        inputNumero.readOnly = true;
        inputNumero.classList.add('bg-slate-100', 'cursor-not-allowed');

        document.getElementById('sala-capacidade').value = sala.capacidade;
        document.getElementById('sala-tipo').value = sala.tipo_sala;
        document.getElementById('sala-projetor').value = String(sala.possui_projetor);
      } else {
        modoEdicaoSala = false;
        titulo.textContent = 'Nova Sala';
        selectPredio.disabled = false;
        inputNumero.readOnly = false;
        inputNumero.classList.remove('bg-slate-100', 'cursor-not-allowed');

        const filtroAtual = document.getElementById('filtro-sala-predio')?.value;
        if (filtroAtual) {
          selectPredio.value = filtroAtual;
        }
      }

      document.getElementById('modal-sala').classList.remove('hidden');
    },

    fecharModalSala: function () {
      document.getElementById('modal-sala').classList.add('hidden');
    },

    salvarSala: async function (e) {
      e.preventDefault();
      const btn = document.getElementById('btn-salvar-sala');
      btn.disabled = true;
      btn.textContent = 'Salvando...';

      const selectPredio = document.getElementById('sala-predio');
      const inputNumero = document.getElementById('sala-numero');

      const dados = {
        id_predio: parseInt(selectPredio.value, 10),
        numero_sala: inputNumero.value.trim(),
        capacidade: parseInt(document.getElementById('sala-capacidade').value, 10),
        tipo_sala: document.getElementById('sala-tipo').value,
        possui_projetor: document.getElementById('sala-projetor').value === 'true'
      };

      try {
        if (modoEdicaoSala) {
          await API.put('/salas', dados);
          this.mostrarSucesso('Sala atualizada com sucesso!');
        } else {
          await API.post('/salas', dados);
          this.mostrarSucesso('Sala cadastrada com sucesso!');
        }
        this.fecharModalSala();
        await this.carregarSalas();
      } catch (err) {
        this.mostrarErro(err.message || 'Erro ao salvar a sala.');
      } finally {
        btn.disabled = false;
        btn.textContent = 'Salvar Sala';
      }
    },

    editarSala: function (idPredio, numeroSala) {
      const sala = listaSalas.find(s => s.id_predio === idPredio && s.numero_sala === numeroSala);
      if (sala) {
        this.abrirModalSala(sala);
      }
    },

    excluirSala: async function (idPredio, numeroSala) {
      if (!confirm(`Tem certeza que deseja excluir a sala "${numeroSala}" do prédio?`)) {
        return;
      }

      try {
        await API.delete(`/salas?id_predio=${idPredio}&numero_sala=${encodeURIComponent(numeroSala)}`);
        this.mostrarSucesso('Sala excluída com sucesso!');
        await this.carregarSalas();
      } catch (err) {
        this.mostrarErro(err.message || 'Erro ao excluir a sala.');
      }
    },

    mostrarSucesso: function (msg) {
      const el = document.getElementById('alerta-sucesso');
      const texto = document.getElementById('alerta-sucesso-texto');
      if (el && texto) {
        texto.textContent = msg;
        el.classList.remove('hidden');
        setTimeout(() => el.classList.add('hidden'), 5000);
      }
    },

    mostrarErro: function (msg) {
      const el = document.getElementById('alerta-erro');
      const texto = document.getElementById('alerta-erro-texto');
      if (el && texto) {
        texto.textContent = msg;
        el.classList.remove('hidden');
      }
    },

    fecharAlerta: function (id) {
      const el = document.getElementById(id);
      if (el) el.classList.add('hidden');
    }
  };

  document.addEventListener('DOMContentLoaded', () => Dependencias.init());
})();
