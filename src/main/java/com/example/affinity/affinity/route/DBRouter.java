package com.example.affinity.affinity.route;

import com.example.affinity.affinity.request.CompanyDto;
import com.example.affinity.affinity.request.EmployeeDto;
import com.example.affinity.affinity.service.CompanyService;
import com.example.affinity.affinity.service.EmployeeService;
import com.example.affinity.affinity.service.InvoiceService;
import com.example.affinity.affinity.utils.Helpers;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.minio.MinioConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class DBRouter extends RouteBuilder {
    @Override
    public void configure() {

        // employees
        from("direct:save-employee")
                .log("Received Body ${body}")
                .unmarshal().json(JsonLibrary.Jackson, EmployeeDto.class)
                .bean(EmployeeService.class, "save(${body})")
                .process(exchange -> {
                    Object obj = exchange.getIn().getBody();
                    ObjectMapper objectMapper = new ObjectMapper();
                    exchange.getIn().setBody(objectMapper.writeValueAsString(obj));
                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE));

        from("direct:get-employee")
                .log("Received id ${header.id}")
                .bean(EmployeeService.class, "findOneById(${header.id})")
                .process(exchange -> {
                    Object obj = exchange.getIn().getBody();
                    ObjectMapper objectMapper = new ObjectMapper();
                    exchange.getIn().setBody(objectMapper.writeValueAsString(obj));
                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE));

        from("direct:get-employees")
                .log("Received Body ${body}")
                .bean(EmployeeService.class, "findAll(${body})")
                .process(exchange -> {
                    Object obj = exchange.getIn().getBody();
                    ObjectMapper objectMapper = new ObjectMapper();
                    exchange.getIn().setBody(objectMapper.writeValueAsString(obj));
                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE));

        // client
        from("direct:save-client")
                .log("Received Body ${body}")
                .unmarshal().json(JsonLibrary.Jackson, CompanyDto.class)
                .bean(CompanyService.class, "save(${body})")
                .process(exchange -> {
                    Object obj = exchange.getIn().getBody();
                    ObjectMapper objectMapper = new ObjectMapper();
                    exchange.getIn().setBody(objectMapper.writeValueAsString(obj));
                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE));

        from("direct:get-client")
                .log("Received id ${header.id}")
                .bean(CompanyService.class, "findOneById(${header.id})")
                .process(exchange -> {
                    Object obj = exchange.getIn().getBody();
                    ObjectMapper objectMapper = new ObjectMapper();
                    exchange.getIn().setBody(objectMapper.writeValueAsString(obj));
                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE));

        from("direct:get-clients")
                .log("Received Body ${body}")
                .bean(CompanyService.class, "findAll(${body})")
                .process(exchange -> {
                    Object obj = exchange.getIn().getBody();
                    ObjectMapper objectMapper = new ObjectMapper();
                    exchange.getIn().setBody(objectMapper.writeValueAsString(obj));
                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE));

        from("direct:get-company-invoices")
                .log("Received Body ${body}")
                .bean(InvoiceService.class, "findCompanyInvoice(${header.companyId})")
                .process(exchange -> {
                    Object obj = exchange.getIn().getBody();
                    ObjectMapper objectMapper = new ObjectMapper();
                    exchange.getIn().setBody(objectMapper.writeValueAsString(obj));
                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE));

        from("direct:get-company-invoice-file")
                .log("Received Body ${body}")
                .log("${headers}")
                .bean(Helpers.class, "getCompanyInvoiceFileName(${header.month}, ${header.year}, ${header.companyId})")
                .setHeader(MinioConstants.OBJECT_NAME, simple("${body}"))
                .log("body ${body}")
                .log("${header.CamelMinioObjectName}")
//                .setHeader(MinioConstants.OBJECT_NAME, simple("camelKey"))
                .to("minio://{{env:AFFINITY_MINIO_BILLABLE_HOURS_BUCKET}}?accessKey={{env:AFFINITY_MINIO_USERNAME}}&secretKey={{env:AFFINITY_MINIO_PASSWORD}}&endpoint={{env:AFFINITY_MINIO_HOST}}:{{env:AFFINITY_MINIO_PORT}}&operation=getObject")
//                .process(exchange -> {
//                    Object obj = exchange.getIn().getBody();
//                    ObjectMapper objectMapper = new ObjectMapper();
//                    exchange.getIn().setBody(objectMapper.writeValueAsString(obj));
//                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_OCTET_STREAM_VALUE));

        from("direct:get-employee-data")
                .log("Received Body ${body}")
                .bean(InvoiceService.class, "findEmployeeWorkData(${header.employeeId})")
                .process(exchange -> {
                    Object obj = exchange.getIn().getBody();
                    ObjectMapper objectMapper = new ObjectMapper();
                    exchange.getIn().setBody(objectMapper.writeValueAsString(obj));
                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE));
    }
}
