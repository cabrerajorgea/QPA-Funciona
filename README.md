# QPA — Qué Pasa Acá?

QPA (abreviatura de "¿Qué Pasa Acá?") es una aplicación social de chats geolocalizados. Permite a los usuarios crear o unirse a salas de conversación ubicadas en el espacio físico real, como restaurantes, plazas o eventos. Si estás cerca, podés chatear con otros en el lugar y compartir opiniones, experiencias o comentarios sobre lo que está ocurriendo.

Esta es la versión funcional estable (v1.0.0), conservada como punto de referencia para desarrollo futuro.

---

## 🚀 Características principales

- 🌍 Mapa interactivo con visualización de salas activas (modo explorador).
- 📍 Creación de salas basada en la ubicación real del usuario.
- 🔐 Privacidad: los usuarios pueden usar apodos y elegir mostrar o no su información.
- 🔔 Notificaciones cuando se crean salas cercanas o ingresan usuarios nuevos.
- ✏️ Chat en tiempo real con burbujas de mensajes diferenciadas.
- 🛡️ Restricción de acceso según la ubicación si así lo define el creador de la sala.

---

## 🔧 Configuración del entorno

### 1. Firebase

Este proyecto requiere un archivo `google-services.json` para conectarse con Firebase:

1. Iniciá un proyecto en [Firebase Console](https://console.firebase.google.com/).
2. Agregá una app Android y descargá el archivo `google-services.json`.
3. Colocá el archivo en la carpeta `app/`.

> ⚠️ Este archivo está ignorado por Git por seguridad, por lo que debe añadirse manualmente.

### 2. Google Maps API Key

Necesitás una clave de API de Google Maps: AIzaSyAieXEcrI68hemWqD7_8KlWSqjtj6XPfno

- Creá un archivo `local.properties` en la raíz del proyecto con:

El sistema la inyecta automáticamente durante el proceso de compilación.

---

## 📁 Estructura del proyecto (resumen)

app/

screens/

components/

utils/

viewmodel/

MainActivity.kt

FirebaseUtils.kt

MapHelpers.kt


---

## 📦 Estado del proyecto

✔️ **Estable** — Esta versión funciona correctamente y se usa como base de restauración segura.  
🧪 Para pruebas y nuevas funciones, usar la rama `dev` (si existe) o crear una nueva.

---

## 📄 Licencia

MIT © Jorge Cabrera

