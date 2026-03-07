# 多架构支持：node、nginx 官方镜像均支持 linux/amd64、linux/arm64
# 验证：docker pull --platform linux/arm64 node:20-alpine
FROM node:20-alpine AS build
WORKDIR /app
RUN npm config set registry https://registry.npmmirror.com
COPY package.json .npmrc ./
RUN npm install
COPY . .
RUN npm run build

# 验证：docker pull --platform linux/arm64 nginx:alpine
FROM nginx:alpine
WORKDIR /usr/share/nginx/html
RUN rm -rf /usr/share/nginx/html/*
COPY --from=build /app/dist .
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
