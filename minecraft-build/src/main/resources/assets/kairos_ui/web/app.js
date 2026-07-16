(function () {
  "use strict";

  var model = null;
  var selectedCategory = "all";
  var selectedModule = null;
  var searchQuery = "";
  var themes = ["violet", "cyan", "rose"];
  var glyphs = { all: "K", movement: "↗", player: "◉", render: "◇", misc: "··" };

  var categoriesNode = document.getElementById("categories");
  var modulesNode = document.getElementById("modules");
  var inspectorEmpty = document.getElementById("inspector-empty");
  var inspectorContent = document.getElementById("inspector-content");
  var searchNode = document.getElementById("search");
  var errorNode = document.getElementById("error");
  var errorMessage = document.getElementById("error-message");

  function query(request, onSuccess) {
    if (typeof window.mcefQuery !== "function") {
      showError("MCEF did not expose window.mcefQuery. Native fallback is disabled.");
      return;
    }
    window.mcefQuery({
      request: JSON.stringify(request),
      persistent: false,
      onSuccess: function (response) {
        try { onSuccess(JSON.parse(response)); }
        catch (error) { showError("Invalid runtime response: " + error.message); }
      },
      onFailure: function (code, message) { showError("Bridge " + code + ": " + message); }
    });
  }

  function showError(message) {
    errorMessage.textContent = message;
    errorNode.hidden = false;
  }

  function receive(next) {
    model = next;
    document.getElementById("app").setAttribute("aria-busy", "false");
    document.getElementById("version").textContent = "v" + next.version;
    document.getElementById("runtime-label").textContent = next.player + " · MC " + next.minecraft;
    errorNode.hidden = true;
    if (selectedModule && !findModule(selectedModule)) selectedModule = null;
    render();
  }

  function findModule(id) {
    if (!model) return null;
    for (var i = 0; i < model.modules.length; i++) if (model.modules[i].id === id) return model.modules[i];
    return null;
  }

  function filteredModules() {
    if (!model) return [];
    var queryText = searchQuery.toLowerCase();
    return model.modules.filter(function (module) {
      var categoryMatch = selectedCategory === "all" || module.category === selectedCategory;
      var queryMatch = !queryText || module.name.toLowerCase().indexOf(queryText) >= 0 ||
        module.description.toLowerCase().indexOf(queryText) >= 0;
      return categoryMatch && queryMatch;
    });
  }

  function render() {
    if (!model) return;
    renderCategories();
    renderModules();
    renderInspector();
  }

  function renderCategories() {
    categoriesNode.textContent = "";
    var list = [{ id: "all", name: "All modules", icon: "all" }].concat(model.categories);
    list.forEach(function (category) {
      var count = model.modules.filter(function (module) {
        return category.id === "all" || module.category === category.id;
      }).length;
      var button = document.createElement("button");
      button.className = "category" + (selectedCategory === category.id ? " active" : "");
      button.innerHTML = '<span class="category-icon">' + (glyphs[category.id] || "•") +
        '</span><span class="category-name"></span><span class="category-count">' + count + '</span>';
      button.querySelector(".category-name").textContent = category.name;
      button.onclick = function () { selectedCategory = category.id; render(); };
      categoriesNode.appendChild(button);
    });
  }

  function renderModules() {
    var list = filteredModules();
    modulesNode.textContent = "";
    var category = model.categories.filter(function (item) { return item.id === selectedCategory; })[0];
    document.getElementById("category-title").textContent = category ? category.name : "All modules";
    document.getElementById("category-kicker").textContent = searchQuery ? "Search results" : "Module library";
    document.getElementById("module-count").textContent = String(list.length);
    document.getElementById("enabled-count").textContent = String(list.filter(function (item) { return item.enabled; }).length);
    document.getElementById("empty").hidden = list.length !== 0;

    list.forEach(function (module, index) {
      var card = document.createElement("article");
      card.className = "module-card" + (module.enabled ? " enabled" : "") +
        (selectedModule === module.id ? " selected" : "");
      card.style.animationDelay = Math.min(index * 14, 100) + "ms";
      card.innerHTML = '<span class="module-symbol">' + initials(module.name) + '</span>' +
        '<span class="module-copy"><strong></strong><small></small></span>' +
        '<span class="module-actions"><button class="state-button"><i></i>' +
        (module.enabled ? "ON" : "OFF") + '</button><button class="more-button">•••</button></span>';
      card.querySelector("strong").textContent = module.name;
      card.querySelector("small").textContent = module.description;
      card.onclick = function (event) {
        if (event.target.className === "more-button") return;
        toggle(module.id);
      };
      card.oncontextmenu = function (event) {
        event.preventDefault();
        selectedModule = module.id;
        render();
      };
      card.querySelector(".more-button").onclick = function (event) {
        event.stopPropagation();
        selectedModule = module.id;
        render();
      };
      modulesNode.appendChild(card);
    });
  }

  function renderInspector() {
    var module = findModule(selectedModule);
    inspectorEmpty.hidden = !!module;
    inspectorContent.hidden = !module;
    if (!module) return;
    inspectorContent.textContent = "";

    var head = document.createElement("div");
    head.className = "inspector-head";
    head.innerHTML = '<div class="inspector-title-row"><h2></h2><button class="large-state"></button></div><p></p>';
    head.querySelector("h2").textContent = module.name;
    head.querySelector("p").textContent = module.description;
    var stateButton = head.querySelector(".large-state");
    stateButton.className += module.enabled ? " enabled" : "";
    stateButton.textContent = module.enabled ? "ENABLED" : "DISABLED";
    stateButton.onclick = function () { toggle(module.id); };
    inspectorContent.appendChild(head);

    var list = document.createElement("div");
    list.className = "settings-list";
    if (!module.settings.length) {
      list.innerHTML = '<div class="no-settings">This module has no configurable values yet.<br>Its enabled state is live and persisted by Kairos.</div>';
    } else {
      var lastGroup = null;
      module.settings.forEach(function (setting) {
        if (setting.group !== lastGroup) {
          var group = document.createElement("p");
          group.className = "settings-label";
          group.textContent = setting.group;
          list.appendChild(group);
          lastGroup = setting.group;
        }
        list.appendChild(settingControl(module, setting));
      });
    }
    inspectorContent.appendChild(list);
  }

  function settingControl(module, setting) {
    var row = document.createElement("div");
    if (setting.type === "boolean") {
      row.className = "setting";
      row.innerHTML = '<span class="setting-copy"><strong></strong><small>Toggle option</small></span>' +
        '<button class="check-button"><svg viewBox="0 0 24 24"><path d="m6 12 4 4 8-9"></path></svg></button>';
      row.querySelector("strong").textContent = setting.name;
      var button = row.querySelector("button");
      if (setting.value) button.className += " enabled";
      button.onclick = function () { setSetting(module.id, setting.id, !setting.value); };
      return row;
    }
    if (setting.type === "number") {
      row.className = "setting number-setting";
      row.innerHTML = '<div class="number-top"><span class="setting-copy"><strong></strong><small></small></span>' +
        '<span class="number-value"></span></div><input type="range">';
      row.querySelector("strong").textContent = setting.name;
      row.querySelector("small").textContent = setting.min + " — " + setting.max;
      row.querySelector(".number-value").textContent = formatNumber(setting.value);
      var input = row.querySelector("input");
      input.min = setting.min; input.max = setting.max; input.step = setting.step; input.value = setting.value;
      input.oninput = function () { row.querySelector(".number-value").textContent = formatNumber(Number(input.value)); };
      input.onchange = function () { setSetting(module.id, setting.id, Number(input.value)); };
      return row;
    }
    row.className = "setting";
    row.innerHTML = '<span class="setting-copy"><strong></strong><small></small></span>';
    row.querySelector("strong").textContent = setting.name;
    row.querySelector("small").textContent = setting.value;
    return row;
  }

  function toggle(id) { query({ type: "toggle", module: id }, receive); }
  function setSetting(module, setting, value) {
    query({ type: "setting", module: module, setting: setting, value: value }, receive);
  }
  function initials(name) {
    var capitals = name.replace(/[^A-Z0-9]/g, "");
    return (capitals || name).slice(0, 2).toUpperCase();
  }
  function formatNumber(value) { return Math.round(value * 100) / 100; }

  searchNode.oninput = function () { searchQuery = searchNode.value.trim(); render(); };
  document.addEventListener("keydown", function (event) {
    if (event.ctrlKey && (event.key === "k" || event.key === "K")) {
      event.preventDefault(); searchNode.focus(); searchNode.select();
    }
  });
  document.getElementById("close-button").onclick = function () { query({ type: "close" }, function () {}); };
  document.getElementById("theme-button").onclick = function () {
    var current = document.documentElement.getAttribute("data-theme") || "violet";
    var next = themes[(themes.indexOf(current) + 1) % themes.length];
    document.documentElement.setAttribute("data-theme", next);
    try { localStorage.setItem("kairos.theme", next); } catch (ignored) {}
  };
  try {
    var savedTheme = localStorage.getItem("kairos.theme");
    if (themes.indexOf(savedTheme) >= 0) document.documentElement.setAttribute("data-theme", savedTheme);
  } catch (ignored) {}

  window.Kairos = { receiveState: receive };
  query({ type: "bootstrap" }, receive);
}());
