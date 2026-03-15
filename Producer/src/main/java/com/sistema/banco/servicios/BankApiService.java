package com.sistema.banco.servicios;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.banco.modelos.LoteTransacciones;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BankApiService {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    
    private static final String URL_GET = "https://hly784ig9d.execute-api.us-east-1.amazonaws.com/default/transacciones";


    public static LoteTransacciones obtenerTransacciones() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_GET)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), LoteTransacciones.class);
        } else {
            throw new Exception("Error al obtener datos: " + response.statusCode());
        }
    }
}