# Checklist de Publicación en Google Play Store y Generación de APK/AAB (`PerúViajero`)

**Proyecto:** `PerúViajero` (`pe.edu.utp.perviajero`)  
**Versión objetivo:** `1.1.0` (`versionCode = 2`)

---

## 1. Artefactos y Firma Digital (Android Studio / Gradle)

1. **Generar Keystore de producción** (una sola vez, fuera de Git):
   ```powershell
   keytool -genkeypair -v -keystore peruviajero-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias peruviajero
   ```
2. **Configurar `keystore.properties` en la raíz del proyecto** (ignorado en `.gitignore`):
   ```properties
   storeFile=peruviajero-release.jks
   storePassword=TU_PASSWORD
   keyAlias=peruviajero
   keyPassword=TU_PASSWORD
   ```
3. **Generar los artefactos Release con R8 activado (`app/src/main/keepRules/rules.keep`):**
   * **APK instalable (para entrega/pruebas directas):**
     ```powershell
     .\gradlew.bat assembleRelease
     ```
     Salida: `app/build/outputs/apk/release/app-release.apk`
   * **Android App Bundle (obligatorio para Google Play Console):**
     ```powershell
     .\gradlew.bat bundleRelease
     ```
     Salida: `app/build/outputs/bundle/release/app-release.aab`

---

## 2. Requisitos de Ficha de Google Play Console

| Elemento | Especificación |
| :--- | :--- |
| **Nombre de la App** | PerúViajero — Turismo, Rutas y Comunidad |
| **Package ID** | `pe.edu.utp.perviajero` |
| **Categoría** | Viajes y guías locales (*Travel & Local*) |
| **Ícono de alta resolución** | PNG 512 × 512 px (sin transparencias no válidas) |
| **Gráfico de funciones** | 1024 × 500 px |
| **Capturas obligatorias** | Mínimo 2–4 capturas de teléfono (16:9 o 9:16): Catálogo, Comunidad, Mapa GPS, Brújula Offline |
| **Clasificación de contenido** | Cuestionario IARC (Apto para todo público / +13 por contenido generado por usuarios en Comunidad) |

---

## 3. Declaración de Permisos y Política de Privacidad

| Permiso (`AndroidManifest.xml`) | Justificación para Revisión de Tienda |
| :--- | :--- |
| `INTERNET` / `ACCESS_NETWORK_STATE` | Consumo de catálogo REST de destinos, carga de mapas/rutas viales y notificaciones push. |
| `CAMERA` | Captura voluntaria de fotografías de experiencias turísticas en Comunidad y foto de perfil. |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Trazado de ruta GPS en el mapa embebido y cálculo de distancia/rumbo hacia el destino turístico. |
| `POST_NOTIFICATIONS` | Confirmación de reservas, recordatorios programados de itinerario y alertas de viaje. |

### Puntos Clave de la Política de Privacidad
* Las fotografías y publicaciones creadas en el modo local permanecen en almacenamiento privado del dispositivo (`filesDir/Pictures` y `SharedPreferences`) salvo futura sincronización en la nube autorizada.
* La ubicación GPS se utiliza únicamente en primer plano cuando el turista abre el mapa de ruta o la brújula direccional; no se rastrea en segundo plano ni se vende a terceros.
* Los datos del manifiesto de pasajeros (DNI/Nombres) se utilizan exclusivamente para emitir el boleto/voucher turístico conforme a la normativa MTC/SUTRAN.
