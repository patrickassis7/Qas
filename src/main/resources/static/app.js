"use strict";

const API = "/api/v1";
const TOKEN_KEY = "organizacao-recursos.access-token";
const EXPIRY_KEY = "organizacao-recursos.expires-at";
const PAGE_SIZE = 20;
const state = {
  token: sessionStorage.getItem(TOKEN_KEY),
  expiresAt: sessionStorage.getItem(EXPIRY_KEY),
  capability: "requester",
  resourcePage: 0,
  adminResourcePage: 0,
  reservationPage: 0,
  operatorPage: 0,
  resources: [],
  available: [],
  currentView: "dashboard"
};

const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];
const node = (tag, className, text) => {
  const element = document.createElement(tag);
  if (className) element.className = className;
  if (text !== undefined && text !== null) element.textContent = String(text);
  return element;
};

class ApiFailure extends Error {
  constructor(status, body) {
    super(body?.message || statusMessage(status));
    this.status = status;
    this.body = body || {};
  }
}

function statusMessage(status) {
  const messages = {
    400: "Confira os campos informados e tente novamente.",
    401: "Sua sessão expirou. Entre novamente.",
    403: "Seu perfil não tem permissão para esta ação.",
    404: "O registro solicitado não foi encontrado.",
    409: "A operação conflita com o estado atual. Revise os dados e tente novamente.",
    500: "O servidor não conseguiu concluir a solicitação. Tente novamente mais tarde."
  };
  return messages[status] || "Não foi possível comunicar com o servidor.";
}

async function api(path, options = {}) {
  const headers = new Headers(options.headers || {});
  headers.set("Accept", "application/json");
  if (options.body !== undefined) headers.set("Content-Type", "application/json");
  if (state.token) headers.set("Authorization", `Bearer ${state.token}`);
  let response;
  try {
    response = await fetch(`${API}${path}`, { ...options, headers, cache: "no-store" });
  } catch (_) {
    throw new Error("Não foi possível conectar à API. Verifique se o servidor está em execução.");
  }
  if (response.status === 401 && path !== "/auth/login") {
    clearSession();
    showLogin("Sua sessão terminou. Entre novamente.");
  }
  const text = await response.text();
  let body = null;
  if (text) {
    try { body = JSON.parse(text); } catch (_) { body = { message: text }; }
  }
  if (!response.ok) throw new ApiFailure(response.status, body);
  return body;
}

function clearSession() {
  state.token = null;
  state.expiresAt = null;
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(EXPIRY_KEY);
}

function showLogin(message = "") {
  clearSession();
  $("#app-shell").hidden = true;
  $("#login-screen").hidden = false;
  $("#login-error").textContent = message;
  $("#login-password").value = "";
}

function showApp() {
  $("#login-screen").hidden = true;
  $("#app-shell").hidden = false;
}

function notify(message, type = "success", details = []) {
  const toast = node("div", `toast${type === "error" ? " error" : ""}`);
  toast.setAttribute("role", type === "error" ? "alert" : "status");
  const title = node("strong", "", type === "error" ? "Não foi possível concluir" : "Concluído");
  toast.append(title, node("span", "", message || "Operação realizada."));
  if (Array.isArray(details) && details.length) {
    const list = node("ul");
    details.forEach(item => {
      const field = item?.field ? `${item.field}: ` : "";
      list.append(node("li", "", `${field}${item?.message || "Valor inválido."}`));
    });
    toast.append(list);
  }
  const region = $("#toast-region");
  region.replaceChildren(toast);
  window.setTimeout(() => toast.remove(), 6500);
}

function showLoading(container, label = "Carregando dados da API…") {
  container.replaceChildren();
  const loading = node("div", "loading-state");
  loading.append(node("span", "spinner"), node("span", "", label));
  container.append(loading);
}

function showEmpty(container, message) {
  container.replaceChildren(node("p", "empty-state", message));
}

function showError(error, container) {
  if (error instanceof ApiFailure && error.status === 401) return;
  notify(error.message || statusMessage(error.status), "error", error.body?.fieldErrors);
  if (container) showEmpty(container, error.message || "Não foi possível carregar os dados.");
}

function asPage(body) {
  if (Array.isArray(body)) return { content: body, totalElements: body.length, totalPages: 1, number: 0 };
  return body || { content: [], totalElements: 0, totalPages: 0, number: 0 };
}

function fmtDate(value, includeTime = true) {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "—";
  return new Intl.DateTimeFormat("pt-BR", includeTime
    ? { dateStyle: "short", timeStyle: "short" }
    : { dateStyle: "short" }).format(date);
}

function toInstant(value) {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date.toISOString();
}

function statusBadge(status) {
  const labels = {
    SOLICITADA: "Solicitada", APROVADA: "Aprovada", EM_USO: "Em uso", CONCLUIDA: "Concluída",
    REJEITADA: "Rejeitada", CANCELADA: "Cancelada", NAO_COMPARECEU: "Não compareceu",
    ATIVO: "Ativo", MANUTENCAO: "Em manutenção", INATIVO: "Inativo"
  };
  return node("span", `status-badge status-${status || ""}`, labels[status] || status || "—");
}

function table(headers, rows, emptyMessage = "Nenhum registro encontrado.") {
  const wrap = node("div", "table-wrap");
  if (!rows.length) {
    wrap.append(node("p", "empty-state", emptyMessage));
    return wrap;
  }
  const tableNode = node("table", "data-table");
  const thead = node("thead");
  const headRow = node("tr");
  headers.forEach(label => headRow.append(node("th", "", label)));
  thead.append(headRow);
  const tbody = node("tbody");
  rows.forEach(row => {
    const tr = node("tr");
    row.forEach(cell => {
      const td = node("td");
      if (cell instanceof Node) td.append(cell);
      else td.textContent = cell === null || cell === undefined || cell === "" ? "—" : String(cell);
      tr.append(td);
    });
    tbody.append(tr);
  });
  tableNode.append(thead, tbody);
  wrap.append(tableNode);
  return wrap;
}

function setPagination(container, page, onChange) {
  container.replaceChildren();
  const totalPages = Number(page.totalPages || 0);
  if (totalPages <= 1) return;
  const previous = node("button", "", "← Anterior");
  previous.type = "button";
  previous.disabled = page.number <= 0;
  previous.addEventListener("click", () => onChange(page.number - 1));
  const next = node("button", "", "Próxima →");
  next.type = "button";
  next.disabled = page.number + 1 >= totalPages;
  next.addEventListener("click", () => onChange(page.number + 1));
  container.append(previous, node("span", "", `Página ${page.number + 1} de ${totalPages}`), next);
}

async function discoverCapabilities() {
  const audit = await api("/audit-events?page=0&size=1").catch(error => {
    if (error instanceof ApiFailure && error.status === 403) return null;
    throw error;
  });
  if (audit !== null) return "admin";
  const startAt = new Date(Date.now() - 60_000).toISOString();
  const endAt = new Date(Date.now() + 60_000).toISOString();
  const report = await api(`/reports/utilization?startAt=${encodeURIComponent(startAt)}&endAt=${encodeURIComponent(endAt)}`).catch(error => {
    if (error instanceof ApiFailure && error.status === 403) return null;
    throw error;
  });
  return report !== null ? "responsible" : "requester";
}

async function checkApiHealth() {
  const text = $("#api-status-text");
  const dot = $("#api-status-dot");
  try {
    const response = await fetch("/actuator/health", { headers: { Accept: "application/json" }, cache: "no-store" });
    const health = response.ok ? await response.json() : null;
    const online = response.ok && health?.status === "UP";
    text.textContent = online ? "API disponível" : "API indisponível";
    dot.style.backgroundColor = online ? "#2aaa7a" : "#b74444";
  } catch (_) {
    text.textContent = "API indisponível";
    dot.style.backgroundColor = "#b74444";
  }
}

function renderCapability() {
  const labels = { requester: "Solicitante", responsible: "Responsável", admin: "Administrador" };
  const role = labels[state.capability] || "Usuário autenticado";
  $("#profile-name").textContent = "Conta autenticada";
  $("#profile-role").textContent = role;
  $("#profile-avatar").textContent = role.charAt(0);
  $$(".capability-admin").forEach(el => { el.hidden = state.capability !== "admin"; });
  $$(".capability-operator").forEach(el => { el.hidden = state.capability === "requester"; });
  $("#operator-reservations").hidden = state.capability !== "admin";
}

const viewTitles = {
  dashboard: "Visão geral", resources: "Pesquisar recursos", availability: "Disponibilidade e reserva",
  reservations: "Minhas reservas", admin: "Gestão de recursos", approvals: "Acompanhamento"
};

function navigate(view) {
  if (view === "admin" && state.capability !== "admin") return;
  if (view === "approvals" && state.capability === "requester") return;
  state.currentView = view;
  $$(".view").forEach(section => section.classList.toggle("is-visible", section.id === `view-${view}`));
  $$(".nav-link[data-view]").forEach(button => button.classList.toggle("is-active", button.dataset.view === view));
  $("#topbar-title").textContent = viewTitles[view] || "Organização de Recursos";
  closeMobileNav();
  const loaders = {
    dashboard: loadDashboard,
    resources: () => loadResources(state.resourcePage),
    reservations: () => loadReservations(state.reservationPage),
    admin: loadAdminResources,
    approvals: state.capability === "admin" ? loadOperatorReservations : async () => {}
  };
  if (loaders[view]) loaders[view]();
}

async function initializeApp() {
  if (!state.token) { showLogin(); return; }
  if (state.expiresAt && Date.parse(state.expiresAt) <= Date.now()) { showLogin("Sua sessão expirou. Entre novamente."); return; }
  showApp();
  try {
    await checkApiHealth();
    state.capability = await discoverCapabilities();
    renderCapability();
    navigate(state.currentView);
  } catch (error) {
    showError(error);
    if (!(error instanceof ApiFailure && error.status === 401)) showLogin("Não foi possível validar seu acesso. Tente entrar novamente.");
  }
}

async function loadDashboard() {
  const metrics = $("#dashboard-metrics");
  const recent = $("#dashboard-reservations");
  showLoading(recent);
  try {
    const [resourceBody, reservationBody] = await Promise.all([
      api("/resources?page=0&size=1"),
      api("/reservations?page=0&size=5")
    ]);
    const reservationPage = asPage(reservationBody);
    const pending = reservationPage.content.filter(item => item.status === "SOLICITADA").length;
    const values = [
      ["Recursos cadastrados", resourceBody?.totalElements ?? "—", "Total informado pela API"],
      ["Reservas", reservationBody?.totalElements ?? reservationPage.content.length, state.capability === "admin" ? "Visão permitida ao administrador" : "Reservas da sua conta"],
      ["Solicitações pendentes", pending, "Na página recente consultada"]
    ];
    metrics.replaceChildren(...values.map(([label, value, caption]) => {
      const card = node("article", "metric-card");
      card.append(node("span", "metric-label", label), node("strong", "metric-value", value), node("span", "metric-caption", caption));
      return card;
    }));
    renderReservationTable(recent, reservationPage.content, false);
  } catch (error) {
    showError(error, recent);
  }
}

function resourceCard(resource, admin = false) {
  const card = node("article", "resource-card");
  const top = node("div", "resource-card-top");
  const type = { SALA: "Sala", PROFESSOR: "Professor", MATERIAL: "Material" }[resource.type] || resource.type;
  top.append(node("span", "resource-type", `▦ ${type}`), statusBadge(resource.status));
  const title = node("h3", "", resource.name);
  const location = node("p", "resource-location", resource.location || (resource.type === "PROFESSOR" ? "Docente" : "Local não informado"));
  const meta = node("div", "resource-meta");
  if (resource.capacity !== null && resource.capacity !== undefined) meta.append(node("span", "meta-pill", `Capacidade ${resource.capacity}`));
  if (resource.skills) meta.append(node("span", "meta-pill", resource.skills));
  if (resource.restricted) meta.append(node("span", "restricted-label", "Requer aprovação"));
  card.append(top, title, location, meta);
  if (admin) {
    const actions = node("div", "table-actions");
    const edit = node("button", "small-button", "Editar"); edit.type = "button";
    edit.addEventListener("click", () => beginResourceEdit(resource));
    const deactivate = node("button", "small-button danger", "Desativar"); deactivate.type = "button";
    deactivate.disabled = resource.status === "INATIVO";
    deactivate.addEventListener("click", () => deactivateResource(resource.id));
    actions.append(edit, deactivate); card.append(actions);
  }
  return card;
}

async function loadResources(pageNumber = 0, formData = null) {
  const results = $("#resource-results");
  const count = $("#resources-count");
  showLoading(results);
  const params = new URLSearchParams({ page: String(pageNumber), size: String(PAGE_SIZE) });
  params.set("status", "ATIVO");
  if (formData) {
    for (const key of ["location", "type", "minimumCapacity", "skill"]) {
      const value = String(formData.get(key) || "").trim();
      if (value) params.set(key, value);
    }
  }
  try {
    const page = asPage(await api(`/resources?${params.toString()}`));
    state.resourcePage = page.number || 0;
    state.resources = page.content;
    count.textContent = `${page.totalElements ?? page.content.length} recurso(s) encontrado(s)`;
    results.replaceChildren();
    if (!page.content.length) showEmpty(results, "Nenhum recurso corresponde aos filtros informados.");
    else page.content.forEach(resource => results.append(resourceCard(resource)));
    setPagination($("#resource-pagination"), page, next => loadResources(next, formData));
  } catch (error) { showError(error, results); count.textContent = "Falha ao carregar recursos"; }
}

async function checkAvailability() {
  const form = $("#reservation-form");
  const startInput = form.elements.startAt;
  const endInput = form.elements.endAt;
  const target = $("#available-resources");
  if (!startInput.value || !endInput.value || !form.reportValidity()) return;
  const startAt = toInstant(startInput.value);
  const endAt = toInstant(endInput.value);
  if (!startAt || !endAt || Date.parse(endAt) <= Date.parse(startAt)) {
    notify("O fim deve ser posterior ao início.", "error"); return;
  }
  showLoading(target, "Consultando disponibilidade real…");
  try {
    const resources = await api(`/resources/availability?startAt=${encodeURIComponent(startAt)}&endAt=${encodeURIComponent(endAt)}`);
    state.available = Array.isArray(resources) ? resources : [];
    target.replaceChildren();
    if (!state.available.length) { showEmpty(target, "Não há recursos disponíveis para esse período."); return; }
    state.available.forEach(resource => {
      const label = node("label", "resource-choice");
      const checkbox = document.createElement("input"); checkbox.type = "checkbox"; checkbox.name = "resourceIds"; checkbox.value = resource.id;
      const copy = node("span"); copy.append(node("strong", "", resource.name), node("small", "", `${resource.type}${resource.status ? ` · ${resource.status}` : ""}`));
      label.append(checkbox, copy); target.append(label);
    });
  } catch (error) { showError(error, target); }
}

async function createReservation(event) {
  event.preventDefault();
  const form = event.currentTarget;
  if (!form.reportValidity()) return;
  const data = new FormData(form);
  const startAt = toInstant(data.get("startAt"));
  const endAt = toInstant(data.get("endAt"));
  const resourceIds = $$("input[name='resourceIds']:checked", form).map(input => input.value);
  if (!resourceIds.length) { notify("Consulte a disponibilidade e selecione pelo menos um recurso.", "error"); return; }
  if (!startAt || !endAt || Date.parse(endAt) <= Date.parse(startAt)) { notify("O fim deve ser posterior ao início.", "error"); return; }
  const button = $("button[type='submit']", form); button.disabled = true; button.textContent = "Enviando…";
  try {
    const body = { startAt, endAt, resourceIds, justification: String(data.get("justification") || "").trim() || null };
    const reservation = await api("/reservations", { method: "POST", body: JSON.stringify(body) });
    notify(`Solicitação enviada. Status atual: ${reservation.status}.`);
    form.reset(); state.available = []; showEmpty($("#available-resources"), "Informe o período e consulte os recursos disponíveis.");
    navigate("reservations");
  } catch (error) { showError(error); }
  finally { button.disabled = false; button.textContent = "Enviar solicitação"; }
}

function renderReservationTable(container, reservations, actions = true, adminView = false) {
  if (!reservations?.length) { showEmpty(container, "Nenhuma reserva encontrada para esta consulta."); return; }
  const rows = reservations.map(reservation => {
    const resourceCount = Array.isArray(reservation.resourceIds) ? reservation.resourceIds.length : 0;
    const cells = [fmtDate(reservation.startAt), fmtDate(reservation.endAt), `${resourceCount} recurso(s)`, statusBadge(reservation.status)];
    if (adminView) cells.splice(0, 0, reservation.id);
    if (actions) {
      const actionCell = node("div", "table-actions");
      if (["SOLICITADA", "APROVADA"].includes(reservation.status)) {
        const cancel = node("button", "small-button danger", "Cancelar"); cancel.type = "button";
        cancel.addEventListener("click", () => cancelReservation(reservation.id)); actionCell.append(cancel);
      }
      if (adminView && ["SOLICITADA", "APROVADA", "EM_USO"].includes(reservation.status)) {
        const operate = node("button", "small-button", "Acompanhar"); operate.type = "button";
        operate.addEventListener("click", () => {
          $("#approval-form").elements.reservationId.value = reservation.id;
          const action = reservation.status === "SOLICITADA" ? "approve" : reservation.status === "APROVADA" ? "start" : "complete";
          $("#approval-form").elements.action.value = action;
          $("#approval-form").scrollIntoView({ behavior: "smooth", block: "center" });
        }); actionCell.append(operate);
      }
      cells.push(actionCell.childNodes.length ? actionCell : "—");
    }
    return cells;
  });
  const headers = adminView ? ["UUID", "Início", "Fim", "Recursos", "Status"] : ["Início", "Fim", "Recursos", "Status"];
  if (actions) headers.push("Ações");
  const result = table(headers, rows);
  container.replaceChildren(...result.childNodes);
}

async function loadReservations(pageNumber = 0) {
  const target = $("#reservation-results"); showLoading(target);
  try {
    const page = asPage(await api(`/reservations?page=${pageNumber}&size=${PAGE_SIZE}`));
    state.reservationPage = page.number || 0;
    const selected = $("#reservation-status-filter").value;
    const entries = selected ? page.content.filter(reservation => reservation.status === selected) : page.content;
    renderReservationTable(target, entries, true, state.capability === "admin");
    setPagination($("#reservation-pagination"), page, next => loadReservations(next));
  } catch (error) { showError(error, target); }
}

async function cancelReservation(id) {
  if (!window.confirm("Deseja cancelar esta reserva?")) return;
  try {
    const reservation = await api(`/reservations/${encodeURIComponent(id)}/cancel`, { method: "POST" });
    notify(`Reserva atualizada para ${reservation.status}.`); loadReservations(state.reservationPage);
  } catch (error) { showError(error); }
}

function beginResourceEdit(resource) {
  const form = $("#resource-form");
  form.elements.id.value = resource.id;
  form.elements.type.value = resource.type || "";
  form.elements.name.value = resource.name || "";
  form.elements.location.value = resource.location || "";
  form.elements.capacity.value = resource.capacity ?? "";
  form.elements.skills.value = resource.skills || "";
  form.elements.restricted.checked = Boolean(resource.restricted);
  form.elements.responsibleId.value = resource.responsibleId || "";
  $("#resource-form-title").textContent = "Atualizar recurso";
  $("#resource-submit").textContent = "Salvar alterações";
  $("#resource-cancel-edit").hidden = false;
  form.scrollIntoView({ behavior: "smooth", block: "center" });
}

function resetResourceForm() {
  const form = $("#resource-form"); form.reset(); form.elements.id.value = "";
  $("#resource-form-title").textContent = "Cadastrar recurso";
  $("#resource-submit").textContent = "Cadastrar recurso";
  $("#resource-cancel-edit").hidden = true;
}

function resourcePayload(data) {
  const rawCapacity = String(data.get("capacity") || "").trim();
  const responsibleId = String(data.get("responsibleId") || "").trim();
  return {
    type: String(data.get("type") || ""), name: String(data.get("name") || "").trim(),
    location: String(data.get("location") || "").trim() || null,
    capacity: rawCapacity ? Number(rawCapacity) : null,
    skills: String(data.get("skills") || "").trim() || null,
    restricted: data.get("restricted") === "on",
    responsibleId: responsibleId || null
  };
}

async function saveResource(event) {
  event.preventDefault();
  const form = event.currentTarget; if (!form.reportValidity()) return;
  const data = new FormData(form); const id = String(data.get("id") || "");
  const button = $("#resource-submit"); button.disabled = true;
  try {
    const resource = await api(id ? `/resources/${encodeURIComponent(id)}` : "/resources", {
      method: id ? "PATCH" : "POST", body: JSON.stringify(resourcePayload(data))
    });
    notify(id ? "Recurso atualizado." : `Recurso “${resource.name}” cadastrado.`);
    resetResourceForm(); loadAdminResources();
  } catch (error) { showError(error); }
  finally { button.disabled = false; }
}

async function deactivateResource(id) {
  if (!window.confirm("Desativar este recurso? Ele deixará de ser oferecido para novas reservas.")) return;
  try {
    await api(`/resources/${encodeURIComponent(id)}/deactivate`, { method: "POST" });
    notify("Recurso desativado."); loadAdminResources();
  } catch (error) { showError(error); }
}

async function loadAdminResources(pageNumber = 0) {
  const target = $("#admin-resource-results"); showLoading(target);
  try {
    const page = asPage(await api(`/resources?page=${pageNumber}&size=${PAGE_SIZE}`));
    state.adminResourcePage = page.number || 0;
    state.resources = page.content;
    const cards = node("div", "resource-grid");
    if (!page.content.length) cards.append(node("p", "empty-state", "Nenhum recurso cadastrado."));
    page.content.forEach(resource => cards.append(resourceCard(resource, true)));
    target.replaceChildren(cards);
    const pagination = node("div", "pagination");
    setPagination(pagination, page, next => loadAdminResources(next));
    target.append(pagination);
  } catch (error) { showError(error, target); }
}

function actionLabel(action) {
  return { approve: "aprovada", reject: "rejeitada", start: "iniciada", complete: "concluída", "no-show": "atualizada" }[action] || "atualizada";
}

async function submitApproval(event) {
  event.preventDefault();
  const form = event.currentTarget; if (!form.reportValidity()) return;
  const id = String(form.elements.reservationId.value).trim();
  const action = form.elements.action.value;
  const note = String(form.elements.note.value || "").trim();
  try {
    const result = await api(`/reservations/${encodeURIComponent(id)}/${action}`, {
      method: "POST", body: JSON.stringify(note ? { note } : {})
    });
    notify(`Reserva ${actionLabel(action)}. Status atual: ${result.status}.`);
    if (state.capability === "admin") loadOperatorReservations(state.operatorPage);
    form.elements.note.value = "";
  } catch (error) { showError(error); }
}

async function loadOperatorReservations(pageNumber = 0) {
  if (state.capability !== "admin") return;
  const target = $("#operator-reservation-results"); showLoading(target);
  try {
    const page = asPage(await api(`/reservations?page=${pageNumber}&size=${PAGE_SIZE}`));
    state.operatorPage = page.number || 0;
    renderReservationTable(target, page.content, false, true);
    // Add only transition actions valid for the currently displayed state.
    const tableBody = $("tbody", target);
    if (tableBody) {
      [...tableBody.rows].forEach((row, index) => {
        const reservation = page.content[index];
        const cell = node("td");
        const action = reservation.status === "SOLICITADA" ? "approve"
          : reservation.status === "APROVADA" ? "start"
            : reservation.status === "EM_USO" ? "complete" : null;
        if (action) {
          const button = node("button", "small-button", { approve: "Aprovar", start: "Registrar retirada", complete: "Concluir" }[action]);
          button.type = "button";
          button.addEventListener("click", () => {
            $("#approval-form").elements.reservationId.value = reservation.id;
            $("#approval-form").elements.action.value = action;
            $("#approval-form").scrollIntoView({ behavior: "smooth", block: "center" });
          });
          cell.append(button);
        } else cell.textContent = "—";
        row.append(cell);
      });
      const head = $("thead tr", target);
      if (head) head.append(node("th", "", "Ação rápida"));
    }
    setPagination($("#operator-reservation-pagination"), page, next => loadOperatorReservations(next));
  } catch (error) { showError(error, target); }
}

function openMobileNav() {
  $("#sidebar").classList.add("is-open");
  $("#mobile-scrim").hidden = false;
  $("#menu-toggle").setAttribute("aria-expanded", "true");
}

function closeMobileNav() {
  $("#sidebar").classList.remove("is-open");
  $("#mobile-scrim").hidden = true;
  $("#menu-toggle").setAttribute("aria-expanded", "false");
}

$("#login-form").addEventListener("submit", async event => {
  event.preventDefault();
  const form = event.currentTarget;
  const button = $("button[type='submit']", form);
  const email = String(form.elements.email.value).trim();
  const password = form.elements.password.value;
  $("#login-error").textContent = "";
  button.disabled = true; button.textContent = "Validando…";
  try {
    const result = await api("/auth/login", { method: "POST", body: JSON.stringify({ email, password }) });
    if (!result?.accessToken || !result?.expiresAt) throw new Error("A API não retornou os dados esperados para a sessão.");
    state.token = result.accessToken;
    state.expiresAt = result.expiresAt;
    sessionStorage.setItem(TOKEN_KEY, result.accessToken);
    sessionStorage.setItem(EXPIRY_KEY, result.expiresAt);
    form.elements.password.value = "";
    state.currentView = "dashboard";
    await initializeApp();
  } catch (error) {
    const details = error instanceof ApiFailure ? error.body?.fieldErrors : [];
    $("#login-error").textContent = details?.length ? `${error.message} ${details.map(item => item.message).filter(Boolean).join(" ")}` : (error.message || "Não foi possível entrar.");
  } finally {
    button.disabled = false;
    button.replaceChildren(document.createTextNode("Entrar "));
    const arrow = node("span", "", "→"); arrow.setAttribute("aria-hidden", "true");
    button.append(arrow);
  }
});

$("#logout-button").addEventListener("click", () => { clearSession(); showLogin("Você saiu do sistema."); });
$("#menu-toggle").addEventListener("click", () => $("#sidebar").classList.contains("is-open") ? closeMobileNav() : openMobileNav());
$("#mobile-scrim").addEventListener("click", closeMobileNav);
$$("[data-view]").forEach(button => button.addEventListener("click", () => navigate(button.dataset.view)));
$$("[data-go]").forEach(button => button.addEventListener("click", () => navigate(button.dataset.go)));
$("#resource-search-form").addEventListener("submit", event => { event.preventDefault(); loadResources(0, new FormData(event.currentTarget)); });
$("#clear-resource-filters").addEventListener("click", () => { $("#resource-search-form").reset(); loadResources(0); });
$("#check-availability").addEventListener("click", checkAvailability);
$("#reservation-form").addEventListener("submit", createReservation);
$("#reservation-status-filter").addEventListener("change", () => loadReservations(0));
$("#resource-form").addEventListener("submit", saveResource);
$("#resource-cancel-edit").addEventListener("click", resetResourceForm);
$("#approval-form").addEventListener("submit", submitApproval);

initializeApp();
