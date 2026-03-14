package com.sistema.banco.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import com.sistema.banco.modelos.Transaccion;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import com.sistema.banco.config.MyServerRabbit;
import com.sistema.banco.servicios.BankApiService;

public class Consumidor {

	private static final String[] BANK_QUEUES = {"BANRURAL", "GYT", "BAC", "BI", "cola_rechazados"};
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
                	if (queue.equals("cola_rechazados")) {
                        System.err.println("REGISTRO RECIBIDO: " + payload);
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    } 
                    else {
                        Transaccion tx = mapper.readValue(payload, Transaccion.class);
                        String codigoUnico = UUID.randomUUID().toString().substring(0, 8);
                        tx.setIdTransaccion("TX-" + tx.getIdTransaccion() + "-" + codigoUnico + "-LESTER");
                        tx.setCarnet("0905-24-22750");
                        tx.setNombre("Lester David Payes Méndez");
                        tx.setCorreo("lpayesm@miumg.edu.gt");
                        
                        String jsonModificado = mapper.writeValueAsString(tx);
                        boolean exito = false;

                        System.out.println("Procesando para API: " + tx.getIdTransaccion() + " de la cola " + queue);
                        
                        while (!exito) {
                            if (BankApiService.guardarTransaccion(jsonModificado)) {
                                String logAceptada = "ID: " + tx.getIdTransaccion() + " | MONTO: " + tx.getMonto() + " | ESTADO: ACEPTADA (POST)";
                                System.out.println(logAceptada);
                                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                                exito = true;
                            } else {
                                System.err.println("Falla conexion API. Reintentando ID: " + tx.getIdTransaccion());
                                Thread.sleep(3000); 
                            }
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
