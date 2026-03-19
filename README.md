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

Configurar credenciales en application.properties

Ejecutar el proyecto:

./mvnw spring-boot:run
🔒 Seguridad

Autenticación basada en JWT

No se almacenan credenciales en el repositorio

Uso de archivos de configuración ignorados por Git

👨‍💻 Autor

Desarrollado por Gustavo De La Rosa
Backend Developer enfocado en APIs seguras y escalables
