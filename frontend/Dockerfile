FROM node:26-alpine

# Явная установка yarn
RUN npm install -g yarn

WORKDIR /app
COPY package.json yarn.lock ./

# Установка зависимостей
RUN yarn install
COPY . .
RUN yarn run build

EXPOSE 4173

# Запускаем встроенный сервер
CMD ["yarn", "run", "preview", "--host"]