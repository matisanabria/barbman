# Barbman (BETA)

*Barbman* es una aplicación de escritorio pensada para la **gestión integral de barberías**.  
Fue diseñada en **Java** con **JavaFX** y utiliza una base de datos embebida (`SQLite`), lo que permite usarla **sin conexión** en cualquier computadora.  

El nombre *Barbman* proviene de *Barber Manager*

---

## Estado del proyecto
Actualmente en **fase BETA**.  
Puedes descargar la última versión desde la sección de [Releases](../../releases).

**Notas importantes**:  
- Los **datos por defecto de la base de datos** deben cargarse manualmente con una herramienta como [DB Browser for SQLite](https://sqlitebrowser.org/) u otra similar.  
  En próximas versiones se incluirá un **modo setup inicial** para simplificar este paso.  
- Es necesario **ejecutar el programa como administrador** para garantizar el correcto acceso a archivos y base de datos.  

---

## Características principales

- **Gestión de barberos**: autenticación rápida y cálculo automático de salarios.  
- **Registro de servicios realizados**: con producción diaria, semanal y mensual.  
- **Gestión de egresos**: control de gastos de la barbería.  
- **Reportes y estadísticas**: generación de informes de caja y pagos.  
- **Soporte de adelantos** en el módulo de sueldos.  
- **Base de datos embebida** (`SQLite`) → carpeta `data/`, sin instalación adicional.  
- **Sistema de logs**: todos los registros se guardan automáticamente en la carpeta `logs/`.  

---

## Tecnologías utilizadas

- **Java 17+**
- **JavaFX 17**
- **SQLite**
- **Log4j 2**
- **Maven**

---

## Licencia

Este proyecto está licenciado bajo los términos de la [Apache License 2.0](./LICENSE).  
Puede usarse, modificarse y distribuirse libremente, pero considere dar créditos al creador.

---

💈 *Barbman: Gestiona tu barbería.*
