# Proyecto: Colas con rabbit

Este es mi proyecto de mensajería asíncrona. La idea principal fue separar todo en dos proyectos independientes: uno que se encarga de obtener las transacciones y otro que se encarga de procesarlas y guardarlas. Así, si algo falla, no se pierde nada.

---

## Proyecto 1: Producer (El que obtiene las transacciones)
Este proyecto solo se encarga de ir a traer las transacciones de la API y repartirlas en las colas de RabbitMQ.

### Estructura de paquetes:
* **com.sistema.banco.config**: Aquí tengo MyServerRabbit.java con mi usuario y contraseña para conectarme al server de Rabbit local.
* **com.sistema.banco.modelos**: Tengo LoteTransacciones.java y Transaccion.java. Sirven para que Jackson convierta el JSON que viene de la API en objetos que Java entienda.
* **com.sistema.banco.producer**: Aquí está Productor.java. Este código hace el GET, recorre cada transacción y la mete en la cola del banco que le toca (BI, GYT, etc.).
* **com.sistema.banco.servicios**: Tengo BankApiService.java, pero solo con el método GET. No tiene nada más porque el productor solo necesita jalar info, no guardar.

---

## Proyecto 2: Consumer (El que procesa)
Este proyecto es el que está escuchando las colas y hace todo el trabajo de modificación y guardado final.

### Estructura de paquetes:
* **com.sistema.banco.consumer**: Aquí está Consumidor.java. Este saca los mensajes de las colas, les pega mi nombre (Lester Payes), mi carnet (0905-24-22750) y les inventa un ID único con UUID.
* **com.sistema.banco.modelos**: También tengo Transaccion.java para poder armar el objeto de nuevo, modificarlo y volverlo a pasar a JSON.
* **com.sistema.banco.servicios**: Aquí el BankApiService.java solo tiene el método POST. Es el que se encarga de mandar la transacción ya personalizada a AWS. Si la API falla, el código tiene un reintento de 3 segundos hasta que por fin lo guarda.

---

## Cómo funciona todo el flujo
1. El Producer hace el GET a la API y llena las colas en RabbitMQ.
2. El Consumer agarra esos mensajes, les mete mis datos personales y genera el código único.
3. El Consumer hace el POST a la API de guardado. Cuando ya está guardado, le avisa a RabbitMQ (ACK) para que ya borre el mensaje de la cola.

## Dependencias del Proyecto (Maven)
Ambos proyectos utilizan estas librerias en el pom.xml para funcionar:

```xml
<dependencies>
    <dependency>
        <groupId>com.rabbitmq</groupId>
        <artifactId>amqp-client</artifactId>
        <version>5.21.0</version>
    </dependency>

    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.17.0</version>
    </dependency>

    <dependency>
        <groupId>org.apache.httpcomponents.client5</groupId>
        <artifactId>httpclient5</artifactId>
        <version>5.3.1</version>
    </dependency>
    
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>1.7.36</version>
    </dependency>
</dependencies>

#Enlace del video: https://drive.google.com/file/d/1or5XD91pgQDfv9CvuplkmsMlm9wMt7BM/view?usp=sharing

**Desarrollado por:** Lester David Payes Méndez  
**Carnet:** 0905-24-22750