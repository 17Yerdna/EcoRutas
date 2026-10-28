# Entrega Unidad 2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to perform this plan after EcoAPI and EcoRutas pass their quality gates. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Entregar dos repositorios verificables, colección API, evidencias reales, informe HTML/PDF con el estilo de sesión 11 y preparación para defensa/cambio en vivo.

**Source of truth:** `../specs/2026-10-28-entrega-unidad2-design.md`

**Master tracker:** `../../../../tasks/todo.md`; always read `../../../../tasks/handoff.md` before resuming.

**Visual reference:** `C:/Users/andre/OneDrive/Desktop/Archivos extra de PharmaMobil/Docs Sesion 11/S11_ActividadAutonoma_Mestanza_Bazan.html`

## Non-negotiable rules

- No capturas, resultados, fechas, issues, PRs ni commits inventados.
- Los repositorios nacieron vacíos el 28-10-2026: la trazabilidad comienza desde esta implementación; no se retrofechan commits.
- La identidad Git se verifica antes del primer commit: `Andrey Fidel Mestanza Bazan <andreymestanzabazan@gmail.com>`; GitHub `17Yerdna`.
- Cada evidencia se captura después de ejecutar su caso y muestra contexto suficiente para verificarlo.
- El PDF conserva A4, margen 18 mm, Times New Roman 12 pt, interlineado 1.5, azul `#1b3a5c`, dorado `#d4a017`, logo UPeU y tablas azules.
- El informe no duplica el README: resume decisiones, pruebas y evidencias evaluables.

## Review Focus

- Los enlaces apuntan a ambos tags `v1.0-unidad2`, no solo a ramas móviles: Task 7.
- Hay al menos tres PR cerrados por repositorio, con issue/tarea y merge `--no-ff`: Tasks 1 y 2.
- Colección demuestra 401, 403 y 400 y queda exportada dentro de EcoAPI: Task 3.
- PDF contiene login, 403, offline/reconexión, grafos Git y salida `commonTest`: Tasks 4–6.
- La rama `sc-<letra>-mestanza` tiene tres commits reales y separados: Task 7.

---

### Task 1: Trazabilidad real de EcoAPI

**Repository:** `EcoAPI`

**Produces:** issues/tasks, branches, at least three reviewed PRs into `develop`, non-fast-forward merges.

- [ ] **Step 1: Create `develop` from the initial base and protect `main` from feature work**

- [ ] **Step 2: Create one issue/task for each API slice**

Minimum slices: base/security, weighing/business rules, route/error contract/docs.

- [ ] **Step 3: Implement each slice from `feature/<issue>-<slug>` using its plan tasks**

Keep one coherent commit per red/green slice; do not split trivial formatting into fake commits.

- [ ] **Step 4: Push each branch and open a PR linked to its issue**

PR description includes requirement, tests run and evidence. Review the diff before merge.

- [ ] **Step 5: Merge each approved PR into `develop` with `--no-ff` and close its issue**

- [ ] **Step 6: Verify the graph**

Run: `git log --graph --decorate --oneline --all`
Expected: at least three visible feature branches merged to `develop`; no feature commit directly on `main`.

---

### Task 2: Trazabilidad real de EcoRutas

**Repository:** `EcoRutas`

**Produces:** issues/tasks, branches, at least three reviewed PRs into `develop`, non-fast-forward merges.

- [ ] **Step 1: Create issues for base/session, local/offline/sync, and screens/integration**

- [ ] **Step 2: Execute the app plan in `feature/<issue>-<slug>` branches**

- [ ] **Step 3: Open one linked PR per coherent slice with test output**

- [ ] **Step 4: Review then merge into `develop` using `--no-ff`**

- [ ] **Step 5: Verify at least three closed PRs and a clean graph**

Run: `git log --graph --decorate --oneline --all`
Expected: feature branches and merge commits are distinguishable.

---

### Task 3: Colección ejecutable de EcoAPI

**Files:**
- Create: `EcoAPI/docs/api/EcoAPI.postman_collection.json`
- Create: `EcoAPI/docs/api/EcoAPI.local.postman_environment.json`
- Create: `EcoAPI/docs/api/README.md`

**Produces:** repeatable requests and assertions for success plus 401/403/400.

- [ ] **Step 1: Add login requests for operario1, supervisor1 and admin**

Capture tokens into role-specific environment variables; never commit a real secret.

- [ ] **Step 2: Add route, point, create, correct, annul, omit and close requests**

- [ ] **Step 3: Add explicit negative cases**

401: missing/expired token. 403: operario annuls, admin creates weighing, and operario tries `PUNTO_GESTIONAR`. 400: bruto ≤ tara with field detail. Add 404 and 409 as useful supporting evidence.

- [ ] **Step 4: Run the collection against a fresh Docker database**

Expected: all assertions pass and no request depends on manually copied database IDs beyond variables populated by earlier requests.

- [ ] **Step 5: Export collection/environment and commit them to EcoAPI**

```bash
git add docs/api
git commit -m "test(api): agregar colección reproducible de evaluación"
```

---

### Task 4: Evidencias técnicas reales

**Directories:**
- Create: `EcoRutas/docs/entrega/evidencias/`
- Create: `EcoRutas/docs/entrega/logs/`

**Produces:** screenshots and plain-text outputs referenced by the report.

- [ ] **Step 1: Reset and start the full stack**

Run: `docker compose down -v` in EcoAPI.

Run: `docker compose up --build -d` in EcoAPI.

Expected: exact seed data and healthy API.

- [ ] **Step 2: Capture login and role/session evidence**

Show successful login and profile/privileges without exposing the full token.

- [ ] **Step 3: Capture a real 403 journey**

Use an OPERARIO action outside privilege; show the clear message and retained session.

- [ ] **Step 4: Capture offline and reconnection journey**

Disable network, register a weighing, show negative ID/pending badge, reconnect, synchronize, show sent/received summary and positive server ID.

- [ ] **Step 5: Save backend/app test outputs**

Run: `.\mvnw.cmd test` in EcoAPI.

Run: `.\gradlew.bat :shared:allTests --console=plain` in EcoRutas.

Save unedited outputs under `docs/entrega/logs/` and record the exact commands in the report.

- [ ] **Step 6: Capture Git graphs from both repositories**

Use a terminal width where branch and merge names remain legible.

---

### Task 5: Informe HTML con el estilo de sesión 11

**Files:**
- Create: `EcoRutas/docs/entrega/logo_upeu.png` by copying the existing approved asset
- Create: `EcoRutas/docs/entrega/Examen_Unidad2_Mestanza_Bazan.html`

**Produces:** self-contained printable report with links and evidence.

- [ ] **Step 1: Reuse the reference stylesheet and page structure**

Keep A4/18 mm, Times New Roman 12 pt/1.5, navy/gold identity, blue tables, green evidence badges and page-safe cards. Change content, not the established visual language.

- [ ] **Step 2: Add cover and evaluation summary**

Include university, course, exam version B, student Andrey Mestanza Bazán, GitHub `17Yerdna`, date, both repository URLs and tag URLs.

- [ ] **Step 3: Add architecture and rule coverage**

One compact diagram: EcoRutas → Ktor → EcoAPI → PostgreSQL, with SQLDelight as local source. Add a table mapping RN-01..RN-04 and HTTP 401/403/400/404/409 to implementation/test/evidence.

- [ ] **Step 4: Add evidence cards**

Each card uses: propósito, preparación, pasos, resultado observado and conclusión. Embed only the real files produced in Task 4.

- [ ] **Step 5: Add Git and quality sections**

Include issue/PR counts, both graphs, branch strategy, tag, collection result and `commonTest` output.

- [ ] **Step 6: Add limitations honestly**

State that the repositories began on 28-10-2026 and therefore cannot demonstrate earlier unit dates; do not obscure or falsify this fact.

---

### Task 6: Exportación y verificación visual del PDF

**Files:**
- Create: `EcoRutas/docs/entrega/Examen_Unidad2_Mestanza_Bazan.pdf`

**Produces:** final readable PDF matching the reference style.

- [ ] **Step 1: Open the HTML in a browser and inspect every evidence image**

- [ ] **Step 2: Print to PDF with backgrounds enabled, A4 and scale 100%**

- [ ] **Step 3: Render every PDF page to images for inspection**

Check no clipped text, orphan headings, broken tables, tiny terminal text, blank pages or missing logo.

- [ ] **Step 4: Fix HTML/CSS and re-export until all pages pass**

- [ ] **Step 5: Verify text extraction and links**

Expected: repository/tag URLs are selectable and the student's identity is correct.

---

### Task 7: Cambio en vivo, release y checklist final

**Produces:** selected SC branch, final merge/tag and complete submission package.

- [ ] **Step 1: When the professor assigns A/B/C/D, create `sc-<letra>-mestanza` from current `develop`**

- [ ] **Step 2: Implement only the selected scenario with three real commits**

Commit 1: failing test/contract. Commit 2: minimum implementation. Commit 3: verification/docs. Do not pre-create fake commits.

Use the matching runbook:

| Scenario | Repositories | Minimum vertical slice | Decisive check |
|---|---|---|---|
| SC-A | EcoRutas | Parameterized SQLDelight query/case use combines search text and `soloPendientes`; ViewModel exposes it; route UI adds control | Works in airplane mode and no filtering loop exists in the composable |
| SC-B | EcoAPI + EcoRutas | Migration/seed adds `AUDITOR` with `PUNTO_LEER` and `PESAJE_LEER`; login/session receives privileges; UI becomes read-only by privilege | Read calls return 200 and every write returns 403 without `if (rol == "AUDITOR")` |
| SC-C | EcoAPI + EcoRutas | Flyway/entity/request/response/seed, remote DTO/domain mapping, SQLDelight migration, form and detail add `humedadEstimada` | Value survives create, sync, download, app restart and correction |
| SC-D | EcoRutas | Observable SQLDelight count scoped to user; repository Flow; route badge | Count changes only after local register or successful sync, never through a manual ViewModel counter |

- [ ] **Step 3: Run scenario-specific and full tests, then open its PR to `develop`**

- [ ] **Step 4: Merge final `develop` into `main` in each repository**

Use a reviewed PR or `--no-ff` merge; verify both main branches build from a fresh clone.

- [ ] **Step 5: Create and push annotated tag `v1.0-unidad2` in both repositories**

- [ ] **Step 6: Verify submission links in an unauthenticated browser session**

Check both repositories, both tags, closed PR lists and collection/report files are reachable.

- [ ] **Step 7: Run the final checklist**

Backend one command; relational DB; exact seed; JWT/roles; RN-01..04; local Flow; offline IDs; ordered sync; required screens; four minimum tests; 401/403/400 collection; READMEs; PDF; SC branch; graphs; tag.

- [ ] **Step 8: Commit the final report assets to EcoRutas**

```bash
git add docs/entrega README.md
git commit -m "docs(entrega): incorporar informe y evidencias verificadas"
```

---

### Task 8: Guion y ensayo de defensa

**File:**
- Create: `EcoRutas/docs/entrega/Guion_Defensa.md`

**Produces:** short evidence-backed answer for every question bank topic.

- [ ] **Step 1: Map each of the 13 topics to one code location and one live action**

Cover: backend privilege enforcement; JWT contents/signature/expiry; secure Android token storage; central bearer header; distinct 401/403 behavior; end-to-end weighing registration; SQLDelight `Flow`; negative offline IDs; sync ordering; `commonTest` fakes; field validation error path; adding a new role; and Git branch/PR history.

- [ ] **Step 2: Write answers as 60–90 second demonstrations, not memorized definitions**

Each entry contains: file/function to open, command or screen to show, expected observation, and one sentence explaining the decision.

- [ ] **Step 3: Rehearse two random questions with the system running**

Expected: each answer points to own code and live behavior without reading the report.

- [ ] **Step 4: Rehearse one SC scenario rollback/retry**

Verify a failed test can be diagnosed without editing on `main` and that the working branch can be pushed before the session ends.
