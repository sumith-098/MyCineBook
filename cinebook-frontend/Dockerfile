# ==========================================
# Stage 1: Build the React Frontend with Vite
# ==========================================
FROM node:18-alpine AS build
WORKDIR /app

# Install dependencies first (caches layers if package.json doesn't change)
COPY package*.json ./
RUN npm install

# Copy source code and build production bundle
COPY . .
RUN npm run build

# ==========================================
# Stage 2: Serve using Nginx Web Server
# ==========================================
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Remove default nginx static assets
RUN rm -rf ./*

# Copy built files from Stage 1 into Nginx public directory
COPY --from=build /app/dist .

# Expose port 80 for web traffic
EXPOSE 80

# Start Nginx server
CMD ["nginx", "-g", "daemon off;"]