Contributing · MD
Guía de contribución - UadeMerchAPI
Este documento define cómo trabajamos en equipo sobre este repositorio. El objetivo es evitar pisarnos el código y mantener main siempre estable.

Ramas
main: rama estable. Solo contiene código funcionando. Nunca se pushea directo acá.
desarrollo: rama de integración. Acá se juntan las features antes de pasar a main.
feature/nombre-tarea: una rama por tarea/funcionalidad. Ejemplos:
feature/endpoint-productos
feature/login-usuario
feature/conexion-db
Flujo de trabajo
Antes de empezar a trabajar, actualizá tu rama desarrollo local:
bash
   git checkout desarrollo
   git pull origin desarrollo
Creá tu rama de trabajo desde desarrollo:
bash
   git checkout -b feature/nombre_tarea
Trabajá y hacé commits chicos y descriptivos:
bash
   git add .
   git commit -m "Agrega endpoint GET /productos"
Subí tu rama:
bash
   git push -u origin feature/nombre_tarea
Andá a GitHub y abrí un Pull Request hacia desarrollo.
Esperá que al menos un compañero revise y apruebe el PR antes de mergear.
Una vez que desarrollo está estable y probado, se abre un PR de desarrollo hacia main.
Reglas generales
❌ Nunca hacer push --force sobre main o desarrollo.
❌ Nunca commitear archivos de configuración con contraseñas, tokens o credenciales.
✅ Hacer git pull antes de empezar a trabajar cada vez, para evitar conflictos.
✅ Commits con mensajes claros (qué se hizo, no "cambios" o "fix").
✅ Un Pull Request por tarea, lo más chico y enfocado posible.
✅ Revisar el PR de un compañero antes de aprobarlo (leer el código, no solo aprobar por aprobar).
Convención de commits (sugerida)
tipo: descripción corta

Ejemplos:
feature: agrega endpoint de creación de productos
fix: corrige validación de stock negativo


Si tenés un conflicto de merge o rompiste algo sin querer, avisá al grupo antes de intentar "arreglarlo" con --force o borrando ramas. La mayoría de los problemas de Git se resuelven fácil si se charlan a tiempo.