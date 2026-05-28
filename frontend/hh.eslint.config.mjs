import { generateEslintConfig, PROJECT_TYPES } from '@hh.ru/eslint-config';

export default [
    {
        ignores: [
            'build/**',
            'dist/**',
            'tools/**',
            '**/*.cjs',
            'package-lock.json',
            'eslint.config.js',
            'vite.config.js',
            'tsconfig.json',
            'tsconfig.*.json',
        ],
    },
    ...generateEslintConfig(PROJECT_TYPES.SERVICE),
    {
        files: ['**/*.{ts,tsx,js,jsx}'],
        rules: {
            'no-console': 'warn',
        },
    },
];
