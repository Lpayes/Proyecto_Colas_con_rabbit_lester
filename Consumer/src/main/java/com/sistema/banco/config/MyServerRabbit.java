package com.sistema.banco.config;

import com.rabbitmq.client.ConnectionFactory;

public class MyServerRabbit {
	private static final String HOST = "127.0.0.1";
    private static final String USER = "Lester";
    private static final String PASS = "124computadora123";

    public static ConnectionFactory getFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setUsername(USER);
        factory.setPassword(PASS);
        return factory;
    }
}
