package com.sistema.banco.servicios;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BankApiService {
    private static final HttpClient client = HttpClient.newHttpClient();
    
    private static final String URL_POST = "https://7e0d9ogwzd.execute-api.us-east-1.amazonaws.com/default/guardarTransacciones";

    public static boolean guardarTransaccion(String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_POST))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Respuesta API Guardar: " + response.statusCode());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}