# Trabajo Practico Programacion Concurrente
---
## Integrantes:

- [**Gerard Brian**](https://github.com/brian1062)
- [**Rodríguez Emanuel**](https://github.com/Ema-Rodriguez)
- [**Schneider Jeremias**](https://github.com/JereSch8)
- [**Viotti Franco**](https://github.com/franco-viotti)

---
[Enunciado Trabajo Practico](https://github.com/brian1062/tp_final_programacion_concurrente/blob/master/Enunciado_TP_Final_Concurrente_2024.pdf)

---
## Introducción

El presente informe detalla el análisis, modelado, y simulación de una red de Petri aplicada a un sistema de agencia de viajes, utilizando herramientas de análisis como Petrinator y implementaciones en Java para la simulación y control de concurrencia. Este trabajo se enmarca dentro del contexto de la asignatura Programación Concurrente y tiene como objetivo principal estudiar y demostrar el comportamiento de sistemas concurrentes mediante el uso de redes de Petri, un modelo gráfico y matemático ampliamente utilizado en la modelización de sistemas distribuidos.

---
## Descripción de la Red de Petri
![Red de petri](/images/red_de_petri.png)
La red de Petri utilizada en este trabajo modela el flujo de clientes y la gestión de reservas en una agencia de viajes. Cada lugar y transición dentro de la red representa diferentes estados y eventos en el sistema. A continuación, se describen las principales plazas y transiciones:

* **P0 (Idle)**: Representa el buffer de entrada de clientes a la agencia.
* **P1, P4, P6, P7, P10 (Recursos Compartidos)**: Plazas que modelan recursos compartidos en el sistema, como agentes de reservas y áreas de gestión.
* **P2 (Ingreso a la Agencia)**: Representa el ingreso de un cliente a la agencia de viajes.
* **P3 (Sala de Espera)**: Lugar donde los clientes esperan antes de ser atendidos por un agente.
* **P5, P8 (Gestión de Reservas)**: Estados del sistema en los cuales se gestionan las reservas de los clientes.
* **P9 (Espera para Aprobación/Rechazo de Reservas)**: Modela la espera de los clientes mientras su reserva es procesada por un agente.
* **P11 (Confirmación de Reserva)**: Plazas que representan la confirmación de una reserva por parte del agente.
* **P12 (Cancelación de Reserva)**: Lugar donde se modela la cancelación de una reserva por parte del cliente o del agente.
* **P13 (Pago de la Reserva)**: Representa el momento en que el cliente realiza el pago de la reserva confirmada.
* **P14 (Salida del Cliente)**: Instancia previa a que el cliente se retire de la agencia.

La estructura de esta red de Petri permite capturar de manera efectiva las interacciones y dependencias entre los diferentes estados y procesos del sistema de la agencia de viajes.

## Análisis de propiedades de la red

Se hizo uso de la herramienta “Petrinator” para análisis de la red la cual nos dio como resultado las siguientes propiedades:

<p align="center">
  <img src="/images/petri_property.png">
</p>

La red de Petri es **simple y extendida**, lo que significa que las transiciones reciben entradas de una sola plaza, o bien, en aquellos casos donde reciben más de una, el conjunto de una de estas últimas está contenido o es igual al de las otras. Esta característica asegura una estructura regular en la red, lo que facilita su análisis y garantiza la coherencia en el flujo de tokens a través del sistema.
### Limitación
La red de Petri es **limitada**, lo que significa que la cantidad de tokens en cada lugar nunca excederá un valor máximo predefinido. Esta propiedad es fundamental para evitar un crecimiento ilimitado en cualquier parte del sistema, lo que podría llevar a estados de desbordamiento o comportamiento incontrolado. La limitación asegura que el sistema se mantenga dentro de los parámetros operativos previstos, previniendo la acumulación excesiva de tokens en cualquier lugar.
### Seguridad
Sin embargo, a pesar de ser limitada, la red **no es completamente segura**. Esto significa que, en algunos estados alcanzables desde la marca inicial, uno o más lugares pueden contener más de un token. Esta condición podría representar la ocurrencia simultánea de eventos que deberían ser mutuamente excluyentes, lo que podría tener implicaciones en la concurrencia y sincronización de eventos dentro del sistema. En el contexto de la agencia de viajes, esto puede reflejar situaciones en las que múltiples clientes o reservas están siendo gestionados simultáneamente, lo cual es aceptable pero requiere supervisión para evitar sobrecargas.
### Ausencia de Deadlock
La red de Petri **no presenta deadlock**, lo que es una propiedad crucial en sistemas concurrentes. Un deadlock es una situación en la que ninguna transición del sistema puede ser disparada, provocando que el sistema se quede atascado en un estado inactivo. La ausencia de deadlocks asegura que desde cualquier estado alcanzable siempre existe al menos una transición que puede ser disparada, garantizando que el sistema nunca se quedará atrapado sin posibilidad de avanzar. Esta propiedad es esencial para mantener la continuidad operativa del sistema, especialmente en un entorno de agencia de viajes, donde la paralización de procesos podría tener consecuencias significativas.

### Vivacidad
El hecho que una red de Petri sea viva significa que todas las transiciones tienen la posibilidad de ser disparadas en algún momento del ciclo de vida. Esto garantiza que ninguna parte quedará bloqueada indefinidamente y que todos los procesos podrán completarse eventualmente. En el caso analizado la red de Petri **es viva**, esta es una propiedad clave para asegurar que este sistema pueda gestionar de manera eficiente todas las reservas, cancelaciones y otros eventos sin interrupciones.

## Invariantes de la Red.

El análisis de invariantes es crucial para comprender la conservación y repetitividad dentro de la red de Petri. Para llevar a cabo este análisis, se ha utilizado la matriz de incidencia, que juega un papel fundamental en la identificación tanto de los invariantes de plaza como de los invariantes de transición.

## Matriz de Incidencia
La matriz de incidencia de la red de Petri es una representación matemática que describe cómo las transiciones afectan los lugares en la red. Esta matriz se define como la diferencia entre la matriz de incidencia de salida $I^+$ y la matriz de incidencia de entrada $I^-$.

$$ C = I^+ - I^- $$
* $I^+=$ Indica cómo las transiciones añaden tokens a los lugares.
* $I^-=$ Indica cómo las transiciones remueven tokens de los lugares.

Las matrices $I^+$ e $I^-$ son también conocidas como post y pre, respectivamente. Para el caso de la matriz post se tiene que cada elemento $I^+ (P_I,T_j)$ contiene el peso asociado al arco que va desde $T_j$ hasta $P_i$, es decir, la cantidad de tokens que se generan en la plaza Pi cuando la transición $T_j$ es disparada. El caso contrario ocurre para los elementos de $I^-(P_i, T_j)$, que indica los tokens que se retiran en la plaza $P_i$ cuando la transición $T_j$ es disparada.

Las filas de las matrices representan las plazas mientras que las columnas las transiciones.

## Invariantes de Plaza
Una invariante de plaza o **p-invariante** es un conjunto de plazas cuya suma de tokens no se modifica con una secuencia de disparos arbitrarios. Estos invariantes son esenciales para analizar la **conservación de recursos** dentro del sistema, asegurando que ciertas cantidades o recursos se mantengan estables a lo largo del tiempo.
Para su cálculo se hace uso de la ecuación:

$$I * x = 0$$

siendo $I$ la matriz de incidencia y $x$ un vector característico constituído por las plazas que forman parte de la invariante.

## Invariantes de Transición
Una invariante de transición o **t-invariante** es el conjunto de transiciones que deben dispararse para que la red retorne a su estado inicial. Estos invariantes son clave para entender los **ciclos operacionales** en la red, asegurando que el sistema puede regresar a un estado de referencia después de completar un ciclo de operaciones.
Para el cálculo de los vectores que forman parte de las t-invariantes, la ecuación asociada es:

$$I^T * x = 0$$

donde $I^T$ es la transpuesta de la matriz de incidencia y $x$ un vector característico constituído por las transiciones necesarias para que el sistema vuelva a su estado inicial.

---

## Análisis de matriz de incidencia
#### Obtención de las matrices post, pre e incidencia utilizando Petrinator:


![forward_backward_matrix](/images/forward_backward_matrix.png)

---

<p align="center">
  <img src="/images/combined_incidence_matrix.png">
</p>


### Análisis de invariantes:
#### Invariantes de Plaza
![combined_incidence_matrix](/images/invariantes_plazas.png)

Un uno en una posición indica que esa plaza es parte de la invariante y un cero indica lo contrario.

###### Ecuaciones de las invariantes de plaza

$$M(P1)+M(P2)=1$$$$M(P5)+M(P6)=1$$$$M(P7)+M(P8)=1$$$$M(P2)+M(P3)+M(P4)=5$$$$M(P10)+M(P11)+M(P12)+M(P13)=1$$$$M(P0)+M(P2)+M(P3)+M(P5)+M(P8)+M(P9)+M(P11)+M(P12)+M(P13)+M(P14)=5 $$

#### Invariantes de Transición

<p align="center">
  <img src="/images/invariantes_transicion.png">
</p>

Un uno en una posición indica que esa transición es parte de la invariante y un cero indica lo contrario.


## Análisis de invariantes de transicion
Se busco la expresion regular que se adapte a nuestra red de petri la cual fue la siguiente:

```
(T0)(.*?)(T1)(.*?)((T2)(.*?)(T5)|(T3)(.*?)(T4))(.*?)((T6)(.*?)(T9)(.*?)(T10)|(T7)(.*?)(T8))(.*?)(T11)(.*?)
```

Por medio de la herramienta para analizar expresiones regulares [Debuggex](https://www.debuggex.com/) se obtuvo la siguiene diagrama:


<p align="center">
  <img src="/images/debuggex.png">
</p>
