export default {
  '*.{js,jsx,ts,tsx}': ['eslint --fix --max-warnings 0 --no-warn-ignored', 'prettier --write'],
  'src/**/*.{ts,tsx}': () => 'tsc -p tsconfig.app.json --noEmit',
  '*.{css,less}': ['prettier --write'],
};
