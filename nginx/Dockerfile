FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY maintenance.html /usr/share/nginx/html/maintenance.html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
