# Estágio de Build (Node 22)
FROM node:22-alpine as build-stage

WORKDIR /app

# Copia dependências e instala
COPY package*.json ./
RUN npm install

# Copia todo o código
COPY . .

# Argumento de build para injetar a URL da API
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL

# Executa o build do Vue
RUN npm run build

# Estágio de Produção (Nginx)
FROM nginx:stable-alpine as production-stage

# Copia os arquivos compilados do estágio anterior para a pasta padrão do Nginx
COPY --from=build-stage /app/dist /usr/share/nginx/html

# Copia nossa configuração personalizada do Nginx (o arquivo nginx.conf corrigido acima)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]