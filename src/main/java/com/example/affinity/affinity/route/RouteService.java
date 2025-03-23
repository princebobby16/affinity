package com.example.affinity.affinity.route;

import com.example.affinity.affinity.response.Data;
import com.example.affinity.affinity.response.HealthCheckResponse;
import com.example.affinity.affinity.response.Meta;
import com.example.affinity.affinity.response.StandardResponse;
import com.example.affinity.affinity.utils.Helpers;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.minio.MinioConstants;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;

@Component
public class RouteService extends RouteBuilder {


    @Override
    public void configure() {

    }
}
