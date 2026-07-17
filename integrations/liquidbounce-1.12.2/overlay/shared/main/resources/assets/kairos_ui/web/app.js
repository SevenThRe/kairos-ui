(function () {
  "use strict";

  var model = null;
  var selectedCategory = "all";
  var selectedModule = null;
  var searchQuery = "";
  var stateRevision = -1;
  var pendingPatches = [];
  var themes = [
    { id: "violet", value: "ObsidianViolet" },
    { id: "cyan", value: "NeonCyan" },
    { id: "rose", value: "RoseQuartz" }
  ];
  var glyphs = {
    all: "K", combat: "⚔", player: "◉", movement: "↗", render: "◇",
    world: "◎", misc: "✦", exploit: "⌁", fun: "☺"
  };

  var categoriesNode = document.getElementById("categories");
  var modulesNode = document.getElementById("modules");
  var inspectorEmpty = document.getElementById("inspector-empty");
  var inspectorContent = document.getElementById("inspector-content");
  var searchNode = document.getElementById("search");
  var errorNode = document.getElementById("error");
  var errorMessage = document.getElementById("error-message");

  function query(request, onSuccess) {
    if (typeof window.mcefQuery !== "function") {
      showError("MCEF did not expose window.mcefQuery. Kairos has no native fallback.");
      return;
    }
    window.mcefQuery({
      request: JSON.stringify(request),
      persistent: false,
      onSuccess: function (response) {
        try {
          onSuccess(JSON.parse(response));
        } catch (error) {
          showError("Invalid runtime response: " + error.message);
        }
      },
      onFailure: function (code, message) {
        showError("Bridge " + code + ": " + message);
      }
    });
  }

  function showError(message) {
    errorMessage.textContent = message;
    errorNode.hidden = false;
  }

  function receive(next) {
    model = next;
    document.getElementById("app").setAttribute("aria-busy", "false");
    document.getElementById("version").textContent = next.version;
    document.getElementById("runtime-label").textContent = next.player + " · " + next.minecraft + " · " + next.prefix + "kairos gui";
    document.documentElement.setAttribute("data-theme", next.theme || "violet");
    document.documentElement.setAttribute("data-animations", next.animations === false ? "off" : "on");
    errorNode.hidden = true;
    if (selectedModule && !findModule(selectedModule)) selectedModule = null;
    if (pendingPatches.length) {
      var queued = pendingPatches;
      pendingPatches = [];
      queued.forEach(applyPatch);
    }
    render();
  }

  function applyPatch(patch) {
    if (!patch || typeof patch.revision !== "number" || patch.revision <= stateRevision) return;
    if (!model) {
      pendingPatches.push(patch);
      return;
    }
    stateRevision = patch.revision;
    (patch.modules || []).forEach(function (change) {
      var module = findModule(change.id);
      if (module) module.enabled = !!change.enabled;
    });
    render();
  }

  function requestRefresh() {
    query({ type: "refresh" }, receive);
  }

  function findModule(id) {
    if (!model) return null;
    for (var i = 0; i < model.modules.length; i++) {
      if (model.modules[i].id === id) return model.modules[i];
    }
    return null;
  }

  function filteredModules() {
    if (!model) return [];
    var text = searchQuery.toLowerCase();
    return model.modules.filter(function (module) {
      var categoryMatch = selectedCategory === "all" || module.category === selectedCategory;
      var queryMatch = !text || module.name.toLowerCase().indexOf(text) >= 0 ||
        module.description.toLowerCase().indexOf(text) >= 0 ||
        module.keybind.toLowerCase().indexOf(text) >= 0;
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
      button.innerHTML = '<span class="category-icon"></span><span class="category-name"></span><span class="category-count"></span>';
      button.querySelector(".category-icon").textContent = glyphs[category.id] || "•";
      button.querySelector(".category-name").textContent = category.name;
      button.querySelector(".category-count").textContent = count;
      button.onclick = function () {
        selectedCategory = category.id;
        render();
      };
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
    document.getElementById("enabled-count").textContent = String(model.modules.filter(function (item) { return item.enabled; }).length);
    document.getElementById("empty").hidden = list.length !== 0;

    list.forEach(function (module, index) {
      var card = document.createElement("article");
      card.className = "module-card" + (module.enabled ? " enabled" : "") +
        (selectedModule === module.id ? " selected" : "") +
        (module.toggleable ? "" : " host-module");
      card.style.animationDelay = Math.min(index * 12, 96) + "ms";
      card.innerHTML = '<span class="module-symbol"></span>' +
        '<span class="module-copy"><span class="module-name-line"><i></i><strong></strong></span><small></small></span>' +
        '<span class="module-meta"><kbd class="module-key"></kbd>' +
        '<button class="state-button" aria-label="Toggle module"><svg viewBox="0 0 24 24"><path d="m6 12 4 4 8-9"></path></svg></button>' +
        '<button class="more-button" aria-label="Module settings">•••</button></span>';
      card.querySelector(".module-symbol").textContent = initials(module.name);
      card.querySelector("strong").textContent = module.name;
      card.querySelector("small").textContent = module.description;
      var key = card.querySelector(".module-key");
      key.textContent = module.keybind;
      key.hidden = module.keybind === "None";

      card.onclick = function (event) {
        if (event.target.closest("button")) return;
        if (module.toggleable) toggle(module.id);
        else select(module.id);
      };
      card.oncontextmenu = function (event) {
        event.preventDefault();
        select(module.id);
      };
      card.querySelector(".state-button").onclick = function (event) {
        event.stopPropagation();
        if (module.toggleable) toggle(module.id);
        else select(module.id);
      };
      card.querySelector(".more-button").onclick = function (event) {
        event.stopPropagation();
        select(module.id);
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
    head.innerHTML = '<div class="inspector-title-row"><div><span class="inspector-category"></span><h2></h2></div><button class="large-state"></button></div>' +
      '<p></p><div class="inspector-meta"><span>Keybind <kbd></kbd></span><span class="value-count"></span></div>';
    head.querySelector(".inspector-category").textContent = module.category;
    head.querySelector("h2").textContent = module.name;
    head.querySelector("p").textContent = module.description;
    head.querySelector("kbd").textContent = module.keybind;
    head.querySelector(".value-count").textContent = module.settings.length + " values";
    var stateButton = head.querySelector(".large-state");
    stateButton.className += module.enabled ? " enabled" : "";
    stateButton.textContent = module.toggleable ? (module.enabled ? "ENABLED" : "DISABLED") : "UI HOST";
    stateButton.onclick = function () {
      if (module.toggleable) toggle(module.id);
    };
    inspectorContent.appendChild(head);

    var list = document.createElement("div");
    list.className = "settings-list";
    if (!module.settings.length) {
      list.innerHTML = '<div class="no-settings">This module has no configurable values.<br>Its enabled state is still connected to LiquidBounce.</div>';
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
      row.innerHTML = '<span class="setting-copy"><strong></strong><small>Boolean value</small></span>' +
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
        '<input class="number-value" type="number"></div><input class="range" type="range">';
      row.querySelector("strong").textContent = setting.name;
      row.querySelector("small").textContent = formatNumber(setting.min) + " — " + formatNumber(setting.max);
      var numberInput = row.querySelector(".number-value");
      var rangeInput = row.querySelector(".range");
      [numberInput, rangeInput].forEach(function (input) {
        input.min = setting.min;
        input.max = setting.max;
        input.step = setting.step;
        input.value = setting.value;
      });
      rangeInput.oninput = function () { numberInput.value = rangeInput.value; };
      rangeInput.onchange = function () { setSetting(module.id, setting.id, Number(rangeInput.value)); };
      numberInput.onchange = function () { setSetting(module.id, setting.id, Number(numberInput.value)); };
      return row;
    }

    if (setting.type === "list") {
      row.className = "setting";
      row.innerHTML = '<span class="setting-copy"><strong></strong><small>Mode</small></span><label class="select-wrap"><select></select><i>⌄</i></label>';
      row.querySelector("strong").textContent = setting.name;
      var select = row.querySelector("select");
      setting.options.forEach(function (option) {
        var node = document.createElement("option");
        node.value = option;
        node.textContent = option;
        node.selected = option === setting.value;
        select.appendChild(node);
      });
      select.onchange = function () { setSetting(module.id, setting.id, select.value); };
      return row;
    }

    if (setting.type === "text") {
      row.className = "setting text-setting";
      row.innerHTML = '<span class="setting-copy"><strong></strong><small>Text value</small></span><input class="text-value" type="text">';
      row.querySelector("strong").textContent = setting.name;
      var textInput = row.querySelector(".text-value");
      textInput.value = setting.value;
      textInput.onchange = function () { setSetting(module.id, setting.id, textInput.value); };
      return row;
    }

    row.className = "setting readonly-setting";
    row.innerHTML = '<span class="setting-copy"><strong></strong><small>Read only</small></span><span class="readonly-value"></span>';
    row.querySelector("strong").textContent = setting.name;
    row.querySelector(".readonly-value").textContent = setting.value;
    return row;
  }

  function select(id) {
    selectedModule = id;
    render();
  }

  function toggle(id) {
    query({ type: "toggle", module: id }, receive);
  }

  function setSetting(module, setting, value) {
    query({ type: "setting", module: module, setting: setting, value: value }, receive);
  }

  function initials(name) {
    var capitals = name.replace(/[^A-Z0-9]/g, "");
    return (capitals || name).slice(0, 2).toUpperCase();
  }

  function formatNumber(value) {
    return String(Math.round(Number(value) * 100) / 100);
  }

  searchNode.oninput = function () {
    searchQuery = searchNode.value.trim();
    render();
  };

  document.addEventListener("keydown", function (event) {
    if (event.ctrlKey && (event.key === "k" || event.key === "K")) {
      event.preventDefault();
      searchNode.focus();
      searchNode.select();
    }
  });

  document.getElementById("close-button").onclick = function () {
    query({ type: "close" }, function () {});
  };

  document.getElementById("theme-button").onclick = function () {
    if (!model) return;
    var currentIndex = 0;
    for (var i = 0; i < themes.length; i++) {
      if (themes[i].id === model.theme) currentIndex = i;
    }
    var next = themes[(currentIndex + 1) % themes.length];
    setSetting("ClickGUI", "Theme", next.value);
  };

  window.KairosRuntime = { applyPatch: applyPatch, requestRefresh: requestRefresh };
  window.Kairos = { receiveState: receive, refresh: requestRefresh };
  query({ type: "bootstrap" }, receive);
}());
