# ---- Stage 1: build ----
FROM node:26-alpine AS builder

# Явная установка yarn
RUN npm install -g yarn

WORKDIR /app



# Сначала копируем только package.json и yarn.lock для кэширования слоя с зависимостями
COPY package.json yarn.lock ./
RUN yarn install --frozen-lockfile

# Копируем остальной код и собираем
COPY . .
RUN yarn build

# ---- Stage 2: nginx ----
FROM nginx:1.31.2-alpine

# Убираем дефолтный конфиг nginx
RUN rm -rf /usr/share/nginx/html/*

# Копируем собранный dist из первого этапа
COPY --from=builder /app/dist /usr/share/nginx/html

# Свой конфиг nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
