# Trabajo Práctico N° 2 - Paradigmas de Programación - UTN 2026

## Descripción del Proyecto
Este desarrollo corresponde a la implementación práctica de la Unidad 2, enfocada en la organización avanzada, reutilización de código y tolerancia a fallos bajo el paradigma de Programación Orientada a Objetos en Java.

El sistema modela una plataforma de gestión de eventos universitarios con manejo dinámico de actividades (charlas, talleres y cursos), automatizando procesos de inscripción, control de asistencia y emisión de comprobantes de acceso.

## Estructura del Código y Soluciones Implementadas

* Manejo de Excepciones y Persistencia (Ejercicio 1):** Se diseñó la excepción chequeada `CupoExcedidoException` para controlar de manera estricta el límite de vacantes en las actividades. Se implementó una lógica de persistencia mediante la serialización del objeto `EventoUniversitario` en un archivo local (`evento.dat`), gestionando los flujos de entrada/salida de datos con bloques `try-catch-finally` para asegurar el cierre de recursos y la estabilidad del programa.
* Uso de Interfaces (Ejercicio 2):** Se incorporó el comportamiento `Certificable` mediante una interfaz. Esto permite desacoplar los tipos de actividades aptas para emitir comprobantes (como Talleres y Cursos) de aquellas que no lo son (Charlas), sin sobrecargar la jerarquía de herencia.
* Genéricos y Wildcards (Ejercicio 3):** Se añadieron métodos parametrizados en la clase principal del modelo utilizando límites acotados (`Actividad>`) para garantizar el retorno de colecciones fuertemente tipadas en los filtros. Asimismo, se aplicaron comodines (`Actividad>`) para flexibilizar la reutilización del método encargado del cálculo de costos de materiales.
* Clases Anidadas e Hilos (Ejercicio 4):** Se definió `TicketDeAcceso` como una clase interna miembro de `Inscripcion`, restringiendo su ciclo de vida al contexto de una postulación confirmada. El proceso de despacho masivo se delegó en la clase concurrente `EnvioTicketsThread` para simular la latencia de red en un hilo secundario, manteniendo el hilo principal libre para renderizar logs de la aplicación.
