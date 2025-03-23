package com.example.affinity.affinity.response;

import com.example.affinity.affinity.utils.Helpers;

import java.util.HashMap;

public class HealthCheckResponse {

    public static HashMap<String, Object> returnHealthStatus() {

        String env = Helpers.getEnv("ENVIRONMENT", "DEV");

        HashMap<String, Object> response = new HashMap<>();
        response.put("email", "eng@affinity.com");
        response.put("system", "Affinity Billing API");
        response.put("version", "1.0.0");
        response.put("environment", env);
        response.put("status", "OK");
        return response;
    }

}
