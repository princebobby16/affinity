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

        from("direct:health")
                .log("Health check started")
                .setBody().method(HealthCheckResponse.class, "returnHealthStatus()")
                .process(exchange -> {
                    HashMap<String, Object> data = HealthCheckResponse.returnHealthStatus();
                    ObjectMapper objectMapper = new ObjectMapper();
                    exchange.getIn().setBody(objectMapper.writeValueAsString(data));
                })
                .setHeader("Content-Type", constant(MediaType.APPLICATION_JSON_VALUE));

        from("direct:save-billable-hours")
                .log("Received CSV file upload request")
                .log("body ${body}")
                .convertBodyTo(byte[].class)
                .process(exchange -> {
                    // Generate a unique file ID (you can use UUID)
                    String filename = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
                    System.out.println(filename);
                    if (filename == null) {
                        filename = "timesheet-" + LocalDateTime.now() + ".csv";
                    }
                    System.out.println(filename);
                    // Set Minio required headers
                    exchange.getIn().setHeader(MinioConstants.OBJECT_NAME, filename);
                })
                .to("minio://{{env:AFFINITY_MINIO_BILLABLE_HOURS_BUCKET}}?accessKey={{env:AFFINITY_MINIO_USERNAME}}&secretKey={{env:AFFINITY_MINIO_PASSWORD}}&endpoint={{env:AFFINITY_MINIO_HOST}}:{{env:AFFINITY_MINIO_PORT}}")
                .log("file successfully stored in minio")
                .log("sending file to rabbitmq")
                .process(exchange -> {
                    String fileName = exchange.getIn().getHeader("CamelMinioObjectName", String.class);
                    ObjectMapper objectMapper = new ObjectMapper();
                    exchange.getIn().setBody(objectMapper.writeValueAsString(Helpers.setFileName(fileName)));
                })
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
                .log("JSON payload to send: ${body}")
                .to("spring-rabbitmq:{{env:AFFINITY_RABBITMQ_EXCHANGE}}?queues={{env:AFFINITY_RABBITMQ_QUEUE}}&routingKey=file&disableReplyTo=true")
                .log("file successfully stored in rabbitmq")
                .process(exchange -> {
                    StandardResponse response = StandardResponse.builder()
                            .data(Data.builder()
                                    .id(0L)
                                    .message("file uploaded successfully")
                                    .build()
                            )
                            .meta(Meta.builder()
                                    .status("SUCCESS")
                                    .timestamp(new Timestamp(System.currentTimeMillis()))
                                    .traceId("")
                                    .build()
                            )
                            .build();

                    ObjectMapper objectMapper = new ObjectMapper();
                    exchange.getIn().setBody(objectMapper.writeValueAsString(response));
                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE));
    }
}
