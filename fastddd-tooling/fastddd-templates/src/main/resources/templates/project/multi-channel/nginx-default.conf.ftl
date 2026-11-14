<#-- Nginx reverse proxy config for multi-channel project -->
<#list subApps as app>
upstream ${app.moduleName?replace("-","_")} { server ${app.moduleName}:${app.port}; }
</#list>

server {
    listen 80;

<#list subApps as app>
    # ${app.label} API
    location ${app.pathPrefix}/ {
        proxy_pass http://${app.moduleName?replace("-","_")}/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

</#list>
    # Health check
    location /health {
        return 200 'OK';
        add_header Content-Type text/plain;
    }
}
