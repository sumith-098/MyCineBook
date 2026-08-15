FROM node:20-alpine AS build

WORKDIR /app

# Copy dependency manifests first
COPY package*.json ./

# Force npm to install native bindings cleanly inside Linux
RUN npm ci

# Copy remaining source files
COPY . .

# Run the Vite build
RUN npm run build

# Serve with Nginx
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]