# Build de la aplicación con Node.js
FROM node:20-alpine AS build

WORKDIR /app

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