# Build de la aplicación con Node.js
FROM node:20-alpine AS build

WORKDIR /app

# Vite "hornea" las variables VITE_* dentro del bundle en tiempo de build,
# así que deben pasarse como build-arg (no basta con environment: en
# docker-compose, que solo aplica en runtime al servidor de dev de Vite).
ARG VITE_API_URL=http://localhost:8080
ENV VITE_API_URL=$VITE_API_URL

COPY package*.json ./

RUN npm install

COPY . .

# Genera la carpeta dist/ optimizada
RUN npm run build

# Servidor Web Nginx para producción
FROM nginx:alpine

# Copia los archivos estáticos generados por Vite
COPY --from=build /app/dist /usr/share/nginx/html

# Copia la configuración de Nginx para SPA (React Router)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]