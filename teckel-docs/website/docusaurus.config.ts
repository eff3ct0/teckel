import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

const config: Config = {
  title: 'Teckel',
  tagline: 'Declarative Apache Spark ETL pipelines in YAML',
  favicon: 'img/favicon.png',

  future: {
    v4: true,
  },

  url: 'https://teckel.eff3ct.com',
  baseUrl: '/',

  organizationName: 'eff3ct',
  projectName: 'teckel',

  onBrokenLinks: 'throw',

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  stylesheets: [
    {
      href: 'https://fonts.googleapis.com/css2?family=Inter:wght@400;450;500;600;700&display=swap',
      type: 'text/css',
    },
  ],

  presets: [
    [
      'classic',
      {
        docs: {
          routeBasePath: '/',
          sidebarPath: './sidebars.ts',
          editUrl:
            'https://github.com/eff3ct/teckel/edit/master/teckel-docs/website/',
        },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
    colorMode: {
      defaultMode: 'light',
      disableSwitch: false,
      respectPrefersColorScheme: true,
    },
    navbar: {
      title: 'Teckel',
      logo: {
        alt: 'Teckel logo',
        src: 'img/logo.svg',
        srcDark: 'img/logo-dark.svg',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'docsSidebar',
          position: 'left',
          label: 'Documentation',
        },
        {
          href: 'https://github.com/eff3ct/teckel',
          label: 'GitHub',
          position: 'right',
        },
        {
          href: 'https://central.sonatype.com/namespace/com.eff3ct',
          label: 'Maven Central',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'light',
      links: [
        {
          title: 'Resources',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/eff3ct/teckel',
            },
            {
              label: 'MIT License',
              href: 'https://github.com/eff3ct/teckel/blob/master/LICENSE',
            },
            {
              label: 'Maven Central',
              href: 'https://central.sonatype.com/namespace/com.eff3ct',
            },
          ],
        },
      ],
      copyright: `Copyright © 2024-2026 Rafael Fernandez. Built with Docusaurus.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.vsDark,
      additionalLanguages: ['java', 'bash', 'scala', 'yaml'],
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
