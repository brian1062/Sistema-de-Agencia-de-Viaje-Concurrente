# Sistema de Agencia de Viajes con Redes de Petri

Este proyecto implementa un sistema de agencia de viajes utilizando Redes de Petri para modelar y controlar procesos concurrentes. El sistema gestiona el flujo de clientes, el manejo de reservas y la asignación de recursos de manera segura para múltiples hilos.

## Descripción General

El sistema modela una agencia de viajes con las siguientes características:
- Gestión de entrada y cola de clientes
- Manejo concurrente de reservas por múltiples agentes
- Administración y sincronización de recursos
- Políticas configurables para balance de carga
- Semántica temporal para temporización realista de procesos

## Arquitectura del Sistema

El modelo de Red de Petri incluye:
- 15 plazas que representan diferentes estados y recursos
- 12 transiciones que modelan varios eventos y acciones
- Múltiples hilos concurrentes que gestionan diferentes segmentos del sistema
- Implementación de monitor thread-safe
- Toma de decisiones basada en políticas para la asignación de recursos

## Autores

- [**Gerard Brian**](https://github.com/brian1062)
- [**Viotti Franco**](https://github.com/franco-viotti)
- [**Rodriguez Emanuel**](https://github.com/Ema-Rodriguez)

## [Enunciado](https://github.com/brian1062/tp_final_programacion_concurrente/blob/master/assignment.pdf)

## [Informe](https://github.com/brian1062/tp_final_programacion_concurrente/blob/master/report.pdf)

## Requisitos

- Java 21
- Gradle 8.5+

## Compilación del Proyecto

```bash
# Clonar el repositorio
git clone https://github.com/brian1062/Sistema-de-Agencia-de-Viaje-Concurrente.git

# Navegar al directorio del proyecto
cd Sistema-de-Agencia-de-Viaje-Concurrente

# Compilar el proyecto
./gradlew build
```

## Ejecución del Sistema

```bash
./gradlew run
```

Al ejecutar el sistema, puedes elegir entre tres políticas:
1. Política Balanceada (distribución 50/50)
2. Política Priorizada (distribuciones 75/25 y 80/20)
3. Política FCFS (First-Come-First-Served)

## Características

### Implementación de Políticas
- **Política Balanceada**: Asegura una distribución equitativa entre agentes y resultados de reservas (aceptadas/rechazadas).
- **Política Priorizada**: Implementa distribución ponderada (75/25 para agentes, 80/20 para resultados)
- **Política FCFS**: Implementación estándar de First-Come-First-Served

## Pruebas

```bash
# Ejecutar pruebas con informe de cobertura
./gradlew test

# Ver informe de cobertura
open build/reports/jacoco/test/html/index.html
```

## Herramientas de Análisis

El proyecto incluye un script Python (`regex.py`) para analizar secuencias de transiciones y verificar invariantes. Para ejecutar el análisis:

```bash
python3 regex.py
```

## Contribuir

1. Haz un fork del repositorio
2. Crea tu rama de característica (`git checkout -b feature/NuevaCaracteristica`)
3. Haz commit de tus cambios (`git commit -m 'Agrega alguna NuevaCaracteristica'`)
4. Haz push a la rama (`git push origin feature/NuevaCaracteristica`)
5. Abre un Pull Request

## Licencia

Este proyecto está licenciado bajo la Licencia MIT - ver el archivo LICENSE para más detalles.