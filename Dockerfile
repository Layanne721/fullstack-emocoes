# Estágio de Build
FROM node:18-alpine as build-stage

WORKDIR /app

# Copia dependências e instala
COPY package*.json ./
RUN npm install

# Copia todo o código e faz o build
COPY . .

# Argumento de build para injetar a URL da API
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL

RUN npm run build

# Estágio de Produção (Nginx)
FROM nginx:stable-alpine as production-stage

# Copia os arquivos compilados do estágio anterior
COPY --from=build-stage /app/dist /usr/share/nginx/html

# Copia nossa configuração personalizada do Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]