package com.sistema.banco.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import com.sistema.banco.modelos.Transaccion;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import com.sistema.banco.config.MyServerRabbit;
import com.sistema.banco.servicios.BankApiService;

public class Consumidor {

	private static final String[] BANK_QUEUES = {"BANRURAL", "GYT", "BAC", "BI"};
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
    	
    	ConnectionFactory factory = MyServerRabbit.getFactory();

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.basicQos(1);

        for (String queue : BANK_QUEUES) {
            channel.queueDeclare(queue, true, false, false, null);
            
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String payload = new String(delivery.getBody(), StandardCharsets.UTF_8);
                try {
                    Transaccion tx = mapper.readValue(payload, Transaccion.class);
                    String codigoUnico = UUID.randomUUID().toString().substring(0, 8);
                    tx.idTransaccion = "TX-" + tx.idTransaccion + "-" + codigoUnico + "-LESTER";
                    tx.carnet = "0905-24-22750"; 
                    tx.nombre = "Lester David Payes Méndez"; 
                    tx.correo = "lpayesm@miumg.edu.gt";
                    
                    String jsonModificado = mapper.writeValueAsString(tx);
                    
                    if (BankApiService.guardarTransaccion(jsonModificado)) {
                        System.out.println("[OK] Transaccion guardada para Lester: " + tx.idTransaccion);
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    } else {
                        System.err.println("[FALLO] Reintentando en 3 segundos..."); 
                        
                        Thread.sleep(2000);

                        if (BankApiService.guardarTransaccion(jsonModificado)) {
                            System.out.println("[OK] Guardado en segundo intento.");
                            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        } else {
                            System.err.println("[ERROR] No se pudo procesar. El mensaje permanece en la cola.");
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Error procesando mensaje: " + ex.getMessage());
                }
            };

            channel.basicConsume(queue, false, deliverCallback, consumerTag -> {});
        }
        System.out.println("Consumidor esperando mensajes...");
    }

   
}
