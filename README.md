🚀 Spring Boot API - JWT Authentication
📌 Descripción

API REST desarrollada con Spring Boot que implementa autenticación y autorización basada en JSON Web Tokens (JWT).
Permite el registro de usuarios, login seguro y control de acceso mediante roles.

🧰 Tecnologías utilizadas

Java

Spring Boot

Spring Security

JWT (JSON Web Tokens)

Maven

🔐 Características principales

✅ Registro de usuarios

✅ Autenticación con JWT

✅ Protección de endpoints

✅ Control de acceso por roles

✅ Arquitectura en capas (Controller, Service, Repository)

📦 Endpoints principales
🔑 Autenticación

POST /auth/login → Iniciar sesión

POST /auth/register → Registrar usuario

👤 Usuarios

GET /users → Obtener usuarios (protegido)

⚙️ Configuración del proyecto

Clonar el repositorio:

git clone https://github.com/TU_USUARIO/spring-api-jwt.git

Navegar al proyecto:

cd spring-api-jwt

Crear archivo de configuración:

cp src/main/resources/application-example.properties src/main/resources/application.properties

Configurar credenciales de la base de datos en application.properties (reemplazar los placeholders)

Definir la clave de firma JWT en la variable de entorno JWT_SECRET (mínimo 32 bytes). Generar una:

openssl rand -base64 48

En PowerShell (alternativa sin openssl):

$b = New-Object byte[] 48; [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)

(No usar Get-Random: no es criptográficamente seguro.)

Definirla en Linux/macOS (sesión actual):

export JWT_SECRET="valor_generado"

Definirla en Windows (PowerShell, sesión actual):

$env:JWT_SECRET = "valor_generado"

Definirla en Windows de forma persistente (abrir una nueva terminal después):

setx JWT_SECRET "valor_generado"

(setx guarda el valor en texto plano en el registro del usuario: válido para desarrollo; en producción usar el gestor de secretos o las variables del entorno de despliegue.)

Opcional: jwt.expiration-ms en application.properties define la duración del token en milisegundos (por defecto 3600000 = 1 hora).
La aplicación no arranca si JWT_SECRET falta o tiene menos de 32 bytes.

Ejecutar el proyecto:

./mvnw spring-boot:run
🔒 Seguridad

Autenticación basada en JWT

No se almacenan credenciales en el repositorio

Uso de archivos de configuración ignorados por Git

👨‍💻 Autor

Desarrollado por Gustavo De La Rosa
Backend Developer enfocado en APIs seguras y escalables
