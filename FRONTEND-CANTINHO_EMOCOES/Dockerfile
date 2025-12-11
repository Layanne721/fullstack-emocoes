# Estágio de Construção (Build)
FROM node:18-alpine as build-stage

WORKDIR /app

# Instala dependências
COPY package*.json ./
RUN npm install

# Copia o código e constrói o site
COPY . .

# Recebe a URL do backend via argumento
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL

RUN npm run build

# Estágio de Produção (Servidor Nginx)
FROM nginx:stable-alpine as production-stage

# Copia os arquivos construídos para o servidor
COPY --from=build-stage /app/dist /usr/share/nginx/html

# Copia a configuração do Nginx (arquivo abaixo)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]