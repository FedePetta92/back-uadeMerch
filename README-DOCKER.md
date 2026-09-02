# Docker – Base de datos MySQL (UadeMerchAPI)

Esta guía explica cómo levantar la base de datos MySQL con Docker para correr el proyecto en forma local. Sigue el mismo enfoque visto en clase (Clase 03).

## Requisitos previos

- Docker Desktop instalado y corriendo

## 1. Levantar el contenedor de MySQL (primera vez)

Desde una terminal (PowerShell, cmd o la terminal de VS Code), ejecutá:

```bash
docker run --name mysql-open -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=ecommerce_db3 -p 3306:3306 -d mysql:8.0
```

Desglosado, esto hace:

```bash
docker run --name mysql-open \
  -e MYSQL_ALLOW_EMPTY_PASSWORD=yes \
  -e MYSQL_DATABASE=ecommerce_db3 \
  -p 3306:3306 \
  -d mysql:8.0
```

- `--name mysql-open`: nombre del contenedor, para poder referenciarlo después.
- `-e MYSQL_ALLOW_EMPTY_PASSWORD=yes`: el usuario `root` queda sin contraseña (solo para desarrollo local, nunca en producción).
- `-e MYSQL_DATABASE=ecommerce_db3`: crea automáticamente la base de datos `ecommerce_db3`.
- `-p 3306:3306`: mapea el puerto 3306 del contenedor al puerto 3306 de tu máquina.
- `-d mysql:8.0`: lo corre en segundo plano (detached), usando la imagen `mysql:8.0`.

La primera vez, Docker va a descargar la imagen (`mysql:8.0`), así que puede tardar un par de minutos.

## 2. Levantar el contenedor (las siguientes veces)

Una vez creado, no hace falta volver a correr `docker run`. Alcanza con:

```bash
docker start mysql-open
```

## 3. Verificar que esté corriendo

```bash
docker ps
```

Deberías ver `mysql-open` en la lista, con estado `Up`.

Si no aparece, revisá los logs:

```bash
docker logs mysql-open
```

## 4. Configurar `application.properties`

En `src/main/resources/application.properties` (ya viene configurado así en este repo):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db3?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

No hace falta tocar nada más: apenas el contenedor esté corriendo, la app se conecta sola.

## 5. Conectarse desde MySQL Workbench (o la extensión de VS Code)

- **Host:** `127.0.0.1` (o `localhost`)
- **Port:** `3306`
- **Username:** `root`
- **Password:** (dejar vacío)
- **Database:** `ecommerce_db3`

## 6. Levantar el proyecto

```bash
./mvnw spring-boot:run
```

(o correrlo directamente desde el IDE)

## 7. Comandos útiles

| Acción | Comando |
|---|---|
| Crear el contenedor (solo la 1ra vez) | `docker run --name mysql-open -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=ecommerce_db3 -p 3306:3306 -d mysql:8.0` |
| Levantar el contenedor ya creado | `docker start mysql-open` |
| Ver contenedores corriendo | `docker ps` |
| Ver logs | `docker logs mysql-open` |
| Parar el contenedor (sin borrar datos) | `docker stop mysql-open` |
| Entrar a la consola de MySQL dentro del contenedor | `docker exec -it mysql-open mysql -u root` |
| Eliminar el contenedor (⚠️ borra los datos) | `docker rm -f mysql-open` |

## 8. Reiniciar desde cero (borrar todos los datos)

Si necesitás resetear la base de datos completamente:

```bash
docker rm -f mysql-open
docker run --name mysql-open -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=ecommerce_db3 -p 3306:3306 -d mysql:8.0
```

## 9. Troubleshooting: "port is not available" / conflicto de puerto 3306

Si al correr `docker start mysql-open` (o `docker run`) te aparece un error como:

```
Error response from daemon: ports are not available: exposing port TCP 0.0.0.0:3306 -> ...
bind: Only one usage of each socket address (protocol/network address/port) is normally permitted.
```

Significa que ya tenés algo en tu máquina (fuera de Docker) usando el puerto 3306 — típicamente un MySQL instalado localmente (ej. desde el instalador de MySQL, no Docker).

**Paso 1: identificar qué está usando el puerto**

En PowerShell:

```powershell
netstat -ano | findstr :3306
```

Esto devuelve un PID (último número de la línea). Para ver a qué proceso corresponde:

```powershell
Get-Process -Id <PID>
```

**Paso 2: confirmar si es un servicio de MySQL de Windows**

```powershell
Get-Service -Name "*mysql*"
```

Si aparece algo como `MySQL80` con estado `Running`, ese es el conflicto.

**Paso 3: pararlo (requiere PowerShell como administrador)**

Abrí PowerShell con **"Ejecutar como administrador"** y corré:

```powershell
Stop-Service -Name "MySQL80"
```

⚠️ Esto no borra tus datos, solo los deja inaccesibles mientras el servicio esté detenido.

Después, volvé a intentar:

```powershell
docker start mysql-open
docker ps
```

Deberías ver el contenedor con estado `Up` y el puerto `0.0.0.0:3306->3306/tcp`.

**Opcional: evitar que se vuelva a levantar solo al reiniciar la PC**

Los servicios de Windows arrancan automáticamente por defecto. Si vas a usar Docker para MySQL de forma habitual, conviene cambiar el tipo de inicio a manual (también requiere PowerShell como administrador):

```powershell
Set-Service -Name "MySQL80" -StartupType Manual
```

Así, `MySQL80` no compite por el puerto 3306 en cada arranque de Windows, y el contenedor de Docker lo puede usar sin conflictos.

## Nota sobre buenas prácticas

Este esquema (usuario `root` sin contraseña) es el que se usó en clase y sirve perfecto para desarrollo local del TP. Si en algún momento el equipo quiere endurecerlo (usuario propio, contraseña, variables de entorno), se puede migrar después sin mucho esfuerzo — pero no es necesario para la entrega.
