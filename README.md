# ms-factura y Spring Cloud Config

Este servicio obtiene la conexion a MySQL, RabbitMQ, los datos del emisor y API SUNAT desde el Config Server. `spring-cloud-starter-config` es la dependencia que interpreta `spring.config.import`; no se necesita `bootstrap.properties` ni `@EnableConfigClient`.

## Requisitos

- JDK 25 y `JAVA_HOME` apuntando a ese JDK. Se usa el Maven Wrapper incluido.
- Config Server iniciado en `http://localhost:8888`.
- MySQL disponible en `localhost:3311`, con la base `factura` y las credenciales del repositorio de configuracion.
- RabbitMQ disponible en `localhost:5672`.
- Eureka en `http://localhost:8761/eureka/` para registrar el servicio y resolver los clientes Feign de cliente y producto.

## Correspondencia de nombres

| Configuracion | Valor por defecto |
| --- | --- |
| `spring.application.name` | `ms-factura` |
| Archivo en la raiz del repositorio Git | `ms-factura.properties` |
| Rama Git (`spring.cloud.config.label`) | `main` |
| Perfil, si no se activa ninguno | `default` |
| Endpoint del Config Server | `http://localhost:8888/ms-factura/default/main` |

El nombre coincide tambien con la ruta `lb://ms-factura` del API Gateway. No se debe cambiar solo para que coincida con el nombre de la carpeta o el artifactId de Maven.

## Arranque en PowerShell

1. Iniciar primero `Config-server-facturador-electronico` con `.\mvnw.cmd spring-boot:run` desde su carpeta.
2. Comprobar la respuesta del Config Server:

   ```powershell
   $config = Invoke-RestMethod http://localhost:8888/ms-factura/default/main
   $config.propertySources | Select-Object name
   ```

   Debe aparecer `ms-factura.properties`. Revisar solo los nombres evita mostrar contrasenas o tokens incluidos en la respuesta.

3. Iniciar MySQL, RabbitMQ y Eureka. Para MySQL, el `docker-compose.yml` de la carpeta superior incluye el servicio `factura`: `docker compose up -d factura`.
4. Desde esta carpeta:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   Factura escucha en `8099`. El registro de arranque debe incluir `Located environment: name=ms-factura` y despues `Started MsFacturaFacturadorElectronicoApplication`.

Para otro servidor o rama, establecer las variables antes de arrancar:

```powershell
$env:CONFIG_SERVER_URL = 'http://localhost:8888'
$env:CONFIG_GIT_LABEL = 'main'
.\mvnw.cmd spring-boot:run
```

Si se cambia `CONFIG_SERVER_PORT` en el servidor, actualizar tambien `CONFIG_SERVER_URL` aqui. Dentro de Docker, `localhost` se refiere al propio contenedor: usar el nombre DNS del servicio correspondiente y adaptar tambien los hosts de MySQL, RabbitMQ y Eureka.

La importacion es obligatoria: si el Config Server no responde o la configuracion solicitada no existe, factura falla al iniciar. Las propiedades de infraestructura y API SUNAT permanecen en Git; los ajustes locales actuales de Eureka, listeners RabbitMQ y tiempos de espera Feign siguen aplicandose salvo que el repositorio remoto los sobrescriba.

## Cambios y diagnostico

- Un cambio en la copia local de `Config-repo-facturador-electronico` requiere commit y push para que el servidor configurado con GitHub lo lea. Para probar esa copia sin publicar, usar `CONFIG_GIT_URI` en el servidor con su URI `file:///D:/...`; Git lee los cambios confirmados.
- Despues de cambiar la configuracion remota, reiniciar factura para cargarla. Este proyecto no configura actualizacion automatica mediante Spring Cloud Bus ni un endpoint de refresh.
- `Unable to load config data from 'configserver:...'`: recargar Maven en el IDE y comprobar que se resolvio `spring-cloud-starter-config`.
- Error HTTP al cargar configuracion: iniciar primero Config Server y revisar URL, rama y archivo.
- Error de conexion JDBC despues de `Located environment`: revisar MySQL en `3311`, base y credenciales.
- Error de conexion RabbitMQ: revisar el broker en `5672`.
- `VerifyError: Constructor must call super() or this() before return`: se encontro bytecode previo invalido en `target`. Regenerarlo con `.\mvnw.cmd clean test` usando JDK 25; despues recargar el proyecto Maven en el IDE.

## Pruebas

```powershell
.\mvnw.cmd test
```

Las pruebas arrancan factura con una respuesta HTTP de configuracion controlada y H2, sin requerir GitHub, MySQL, RabbitMQ, Eureka ni llamadas a API SUNAT. Comprueban que se carguen propiedades remotas y que el arranque falle si falta la configuracion obligatoria. H2 solo se incluye para pruebas; la aplicacion sigue usando MySQL.

Referencia: [Spring Cloud Config Client](https://docs.spring.io/spring-cloud-config/reference/client.html).
