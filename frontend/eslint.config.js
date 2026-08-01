import js from '@eslint/js';
import globals from 'globals';
import tseslint from 'typescript-eslint';

export default tseslint.config(
    // Включает рекомендуемые настройки для javascript
    js.configs.recommended,
    // Включает рекомендуемые настройки для typescript
    ...tseslint.configs.recommended,
    {
        languageOptions: {
            globals: {
                ...globals.browser,
            },
        },
        rules: {
            // Запрещает переопределение констант
            'no-const-assign': 'error',
            // Запрещает использование неинициализированных переменных
            'no-use-before-define': 'error',
            // Запрещает использование неиспользуемых переменных
            'no-unused-vars': ['warn', { argsIgnorePattern: '^_', varsIgnorePattern: '^_' }],
            // Выдаём предупреждение если есть console.log в коде
            'no-console': 'warn',
            // Запрещает использование return внутри функции, которая передаётся в конструктор Promise
            'no-promise-executor-return': 'error',
        },
        files: ['**/*.{ts,tsx,js,jsx}'],
    },
    {
        ignores: [
            'build/**',
            'dist/**',
            'tools/**',
            '**/*.cjs',
            'package-lock.json',
            'eslint.config.js',
            'tsconfig.json',
            'tsconfig.*.json',
        ],
    },
);
