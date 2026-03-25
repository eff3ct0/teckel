import type {SidebarsConfig} from '@docusaurus/plugin-content-docs';

const sidebars: SidebarsConfig = {
  docsSidebar: [
    'index',
    'getting-started',
    {
      type: 'category',
      label: 'Reference',
      items: [
        'transformations',
        'cli',
        'api',
        'plugins',
      ],
    },
    'examples',
  ],
};

export default sidebars;
