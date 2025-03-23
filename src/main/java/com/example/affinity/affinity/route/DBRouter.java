package com.example.affinity.affinity.route;

import com.example.affinity.affinity.request.CompanyDto;
import com.example.affinity.affinity.request.EmployeeDto;
import com.example.affinity.affinity.response.Data;
import com.example.affinity.affinity.response.HealthCheckResponse;
import com.example.affinity.affinity.response.Meta;
import com.example.affinity.affinity.response.StandardResponse;
import com.example.affinity.affinity.service.CompanyService;
import com.example.affinity.affinity.service.EmployeeService;
import com.example.affinity.affinity.service.InvoiceService;
import com.example.affinity.affinity.utils.Helpers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.minio.MinioConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
public class DBRouter extends RouteBuilder {
    @Override
    public void configure() {

        // health
        from("direct:health")
                .log("Health check started")
                .setBody().method(HealthCheckResponse.class, "returnHealthStatus()")
                .process(DBRouter::encodeBodyToJson)
                .setHeader("Content-Type", constant(MediaType.APPLICATION_JSON_VALUE));

        // billable hours
        from("direct:save-billable-hours")
                .log("Received CSV file upload request")
                .log("${headers}")
                .choice()
                .when(header(Exchange.CONTENT_TYPE).isEqualTo("text/csv"))
                    .process(exchange -> {
                        String contentLength = exchange.getIn().getHeader("Content-Length", String.class);
                        if (contentLength != null && Integer.parseInt(contentLength) > 10 * 1024 * 1024) {
                            throw new RuntimeException("File too large!");
                        }
                    })
                    .log("file received")
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
                    .log("file name successfully stored in rabbitmq")
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
                    .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
                .otherwise()
                    .process(exchange -> {
                        StandardResponse response = StandardResponse.builder()
                                .data(Data.builder()
                                        .id(0L)
                                        .message("invalid file type")
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
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                    .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
        ;

        // employees
        from("direct:save-employee")
                .log("Received Body ${body}")
                .unmarshal().json(JsonLibrary.Jackson, EmployeeDto.class)
                .bean(EmployeeService.class, "save(${body})")
                .process(DBRouter::encodeBodyToJson)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE));

        // get one employee by their id
        from("direct:get-employee")
                .log("Received id ${header.id}")
                .bean(EmployeeService.class, "findOneById(${header.id})")// call the service to fetch employee
                .process(DBRouter::encodeBodyToJson)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE));

        // get all employees
        from("direct:get-employees")
                .log("fetching all employees")
                .bean(EmployeeService.class, "findAll()") // call the service to fetch employees
                .process(DBRouter::encodeBodyToJson)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200)) // set status code
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE)); // set content type

        // client
        from("direct:save-client")
                .log("Received Body ${body}")
                .unmarshal().json(JsonLibrary.Jackson, CompanyDto.class)
                .bean(CompanyService.class, "save(${body})")
                .process(DBRouter::encodeBodyToJson)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE));

        from("direct:get-client")
                .log("Received id ${header.id}")
                .bean(CompanyService.class, "findOneById(${header.id})")
                .process(DBRouter::encodeBodyToJson)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE));

        from("direct:get-clients")
                .log("Received Body ${body}")
                .bean(CompanyService.class, "findAll(${body})")
                .process(DBRouter::encodeBodyToJson)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE));

        from("direct:get-company-invoices")
                .log("Received Body ${body}")
                .bean(InvoiceService.class, "findCompanyInvoice(${header.companyId})")
                .process(DBRouter::encodeBodyToJson)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE));

        from("direct:get-company-invoice-file")
                .log("Received Body ${body}")
                .log("${headers}")
                .bean(Helpers.class, "getCompanyInvoiceFileName(${header.month}, ${header.year}, ${header.companyId})")
                .setHeader(MinioConstants.OBJECT_NAME, simple("${body}"))
                .log("body ${body}")
                .log("${header.CamelMinioObjectName}")
                .to("minio://{{env:AFFINITY_MINIO_BILLABLE_HOURS_BUCKET}}?accessKey={{env:AFFINITY_MINIO_USERNAME}}&secretKey={{env:AFFINITY_MINIO_PASSWORD}}&endpoint={{env:AFFINITY_MINIO_HOST}}:{{env:AFFINITY_MINIO_PORT}}&operation=getObject")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_OCTET_STREAM_VALUE));

        from("direct:get-employee-data")
                .log("Received Body ${body}")
                .bean(InvoiceService.class, "findEmployeeWorkData(${header.employeeId})")
                .process(DBRouter::encodeBodyToJson)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE));
    }

    private static void encodeBodyToJson(Exchange exchange) throws JsonProcessingException {
        Object obj = exchange.getIn().getBody(); // get the return value from the findAll() function
        ObjectMapper objectMapper = new ObjectMapper();
        exchange.getIn().setBody(objectMapper.writeValueAsString(obj));// encode to json and set it to the body
    }
}
