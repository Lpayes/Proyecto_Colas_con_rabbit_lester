package com.sistema.banco.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import com.sistema.banco.modelos.Transaccion;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class Consumidor {

	private static final String[] BANK_QUEUES = {"BANRURAL", "GYT", "BAC", "BI"};
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setUsername("Lester");
        factory.setPassword("124computadora123");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.basicQos(1);

        for (String queue : BANK_QUEUES) {
            channel.queueDeclare(queue, true, false, false, null);
            
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String payload = new String(delivery.getBody(), StandardCharsets.UTF_8);
                try {
                    Transaccion tx = mapper.readValue(payload, Transaccion.class);
                    
                    if (postToApi(payload)) {
                        System.out.println("[OK] Transacción " + tx.idTransaccion + " guardada.");
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    } else {
                        System.err.println("[FALLO] API no disponible. Mensaje sigue en cola.");
                    }
                } catch (Exception ex) {
                    System.err.println("Error: " + ex.getMessage());
                }
            };

            channel.basicConsume(queue, false, deliverCallback, consumerTag -> {});
        }
        System.out.println("Consumidor esperando mensajes...");
    }

    private static boolean postToApi(String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://7e0d9ogwzd.execute-api.us-east-1.amazonaws.com/default/guardarTransacciones"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
    
}
