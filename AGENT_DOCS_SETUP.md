# Agent prompt — Documentación Docusaurus para Teckel

> Usa este documento como contexto completo para replicar en **Teckel** el mismo
> proceso de mejora que se hizo en **Criteria4s**. Léelo entero antes de actuar.

---

## Contexto del proyecto

**Teckel** es un framework Scala 2.13 para crear pipelines ETL sobre Apache Spark
usando ficheros YAML declarativos. El repo vive en:

- Local: `/home/p3rkins/git/eff3ct/teckel`
- GitHub: `https://github.com/eff3ct0/teckel`

Documentación en Markdown ya existente: `docs/` (sin Docusaurus todavía).

**Referencia de cómo quedó Criteria4s** (proyecto hermano ya completado):

- Local: `/mnt/data/git/eff3ct/criteria4s`
- Web: `https://criteria4s.rafaelfernandez.dev`
- Docusaurus en `website/` con tema personalizado de `rafaelfernandez.dev`

---

## Objetivo

Replicar para Teckel exactamente lo mismo que se hizo en Criteria4s:

1. Crear un sitio Docusaurus en `website/`
2. Migrar la documentación existente de `docs/`
3. Aplicar el tema visual de `rafaelfernandez.dev`
4. Desplegar en Cloudflare Pages en `teckel.rafaelfernandez.dev`
5. Añadir Teckel al dropdown *Projects* de `rafaelfernandez.dev`

---

## Paso 1 — Inicializar Docusaurus

```bash
cd /home/p3rkins/git/eff3ct/teckel
npx create-docusaurus@latest website classic --typescript
cd website && npm install
```

Estructura esperada tras la instalación:

```
teckel/
├── docs/                  # docs Markdown originales
├── website/               # sitio Docusaurus (nuevo)
│   ├── docs/              # docs que usará Docusaurus
│   ├── src/css/custom.css
│   ├── static/img/
│   ├── docusaurus.config.ts
│   └── sidebars.ts
```

Copia o mueve el contenido de `docs/` a `website/docs/` adaptando la estructura.

---

## Paso 2 — Configurar `docusaurus.config.ts`

Toma como referencia exacta el fichero de Criteria4s:
`/mnt/data/git/eff3ct/criteria4s/website/docusaurus.config.ts`

Ajustes específicos para Teckel:

```ts
const config: Config = {
  title: 'Rafael Fernández',           // para la pestaña del navegador
  tagline: 'Declarative Spark ETL pipelines with YAML',
  favicon: 'img/r-favicon.png',

  url: 'https://teckel.rafaelfernandez.dev',
  baseUrl: '/',

  organizationName: 'eff3ct0',
  projectName: 'teckel',

  // navbar
  navbar: {
    title: 'Teckel',
    logo: {
      alt: 'Rafael Fernandez logo',
      src: 'img/r-logo.png',
      srcDark: 'img/r-logo-dark.png',
    },
    items: [
      { type: 'docSidebar', sidebarId: 'docsSidebar', position: 'left', label: 'Documentation' },
      { href: 'https://github.com/eff3ct0/teckel', label: 'GitHub', position: 'right' },
      { href: 'https://central.sonatype.com/namespace/com.eff3ct', label: 'Maven Central', position: 'right' },
    ],
  },

  footer: {
    copyright: `Copyright © 2024-2026 Rafael Fernandez. Built with Docusaurus.`,
  },
};
```

---

## Paso 3 — Aplicar el tema visual de `rafaelfernandez.dev`

### 3a. Copiar los logos

Copia desde Criteria4s (ya existen con las medidas correctas):

```bash
cp /mnt/data/git/eff3ct/criteria4s/website/static/img/r-logo.png      website/static/img/
cp /mnt/data/git/eff3ct/criteria4s/website/static/img/r-logo-dark.png  website/static/img/
cp /mnt/data/git/eff3ct/criteria4s/website/static/img/r-favicon.png    website/static/img/
```

### 3b. Copiar el CSS

Copia el CSS completo de Criteria4s y reemplaza el de Teckel:

```bash
cp /mnt/data/git/eff3ct/criteria4s/website/src/css/custom.css \
   website/src/css/custom.css
```

El CSS ya incluye:

- Paleta de colores de `rafaelfernandez.dev` (`data/theme.json`) para light y dark
- Font Inter
- Código con colores GitHub (light) / VS Dark (dark)
- Navbar con blur, sidebar, tablas, admonitions, TOC, scrollbar, etc.

### 3c. Añadir la fuente Inter en `docusaurus.config.ts`

```ts
stylesheets: [
  {
    href: 'https://fonts.googleapis.com/css2?family=Inter:wght@400;450;500;600;700&display=swap',
    type: 'text/css',
  },
],
```

### 3d. Temas Prism

```ts
prism: {
  theme: prismThemes.github,
  darkTheme: prismThemes.vsDark,
  additionalLanguages: ['java', 'bash', 'scala', 'yaml'],
},
```

---

## Paso 4 — Despliegue en Cloudflare Pages

### 4a. Crear CNAME para el dominio personalizado

Añade el fichero en `website/static/CNAME` (si usas GitHub Pages) **o** configura
el dominio directamente en el panel de Cloudflare Pages → Custom domains:
`teckel.rafaelfernandez.dev`.

### 4b. Configuración de build en Cloudflare Pages

| Campo | Valor |
|---|---|
| Framework preset | None |
| Build command | `cd website && npm install && npm run build` |
| Build output directory | `website/build` |
| Root directory | `/` (raíz del repo) |

> **Nota:** Teckel **no usa sbt-mdoc**, así que no hay paso de compilación Scala
> previo al build. El build es solo Docusaurus puro.
> Si en el futuro se quiere añadir ejemplos de código compilados, habría que añadir
> `sbt-mdoc` al proyecto primero (ver cómo lo hace Criteria4s en
> `/mnt/data/git/eff3ct/criteria4s/project/plugins.sbt`).

### 4c. Variables de entorno en Cloudflare (si hacen falta)

```
NODE_VERSION = 20
```

---

## Paso 5 — Añadir Teckel al menú de `rafaelfernandez.dev`

El repo de la web personal está en `/home/p3rkins/git/github/rafaelfernandez.dev`.
El fichero de menús es `config/_default/menus.en.toml`.

Añade debajo de Criteria4s:

```toml
[[main]]
name = "Teckel"
parent = "projects"
url = "https://teckel.rafaelfernandez.dev"
weight = 2
```

Después:

```bash
cd /home/p3rkins/git/github/rafaelfernandez.dev
git add config/_default/menus.en.toml
git commit -m "feat(nav): add Teckel to Projects dropdown"
git push origin master
```

---

## Paso 6 — Commit y push de Teckel

```bash
cd /home/p3rkins/git/eff3ct/teckel
git add website/
git commit -m "docs: add Docusaurus site with rafaelfernandez.dev theme"
git push origin master
```

---

## Checklist final

- [ ] `website/` inicializado con Docusaurus (TypeScript)
- [ ] Docs migrados de `docs/` a `website/docs/`
- [ ] `docusaurus.config.ts` configurado (url, baseUrl, title, navbar, footer)
- [ ] Logos copiados (`r-logo.png`, `r-logo-dark.png`, `r-favicon.png`)
- [ ] `custom.css` copiado de Criteria4s
- [ ] Build local funciona: `cd website && npm run build`
- [ ] Cloudflare Pages configurado con `teckel.rafaelfernandez.dev`
- [ ] Teckel añadido al dropdown Projects de `rafaelfernandez.dev`
- [ ] Todo commiteado y pusheado

---

## Paleta de colores de referencia (`rafaelfernandez.dev/data/theme.json`)

```json
{
  "colors": {
    "default": {
      "theme_color": {
        "primary": "#121212",
        "secondary": "#6b7280",
        "body": "#fff",
        "border": "#eaeaea",
        "theme_light": "#f6f6f6",
        "theme_dark": "#0a0a0a"
      },
      "text_color": {
        "default": "#444444",
        "dark": "#040404",
        "light": "#717171"
      }
    },
    "darkmode": {
      "theme_color": {
        "primary": "#fff",
        "secondary": "#9ca3af",
        "body": "#1c1c1c",
        "border": "#3E3E3E",
        "theme_light": "#222222",
        "theme_dark": "#0a0a0a"
      },
      "text_color": {
        "default": "#B4AFB6",
        "dark": "#fff",
        "light": "#B4AFB6"
      }
    }
  },
  "fonts": {
    "font_family": {
      "primary": "Inter:wght@400;600"
    }
  }
}
```

---

## Ficheros clave de referencia en Criteria4s

| Fichero | Propósito |
|---|---|
| `website/docusaurus.config.ts` | Config completa — úsala como plantilla |
| `website/src/css/custom.css` | Tema visual completo — cópialo tal cual |
| `website/sidebars.ts` | Estructura del sidebar — adáptalo a los docs de Teckel |
| `website/static/img/r-logo*.png` | Logos — cópialos directamente |
