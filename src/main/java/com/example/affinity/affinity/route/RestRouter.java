package com.example.affinity.affinity.route;

import com.example.affinity.affinity.response.Data;
import com.example.affinity.affinity.response.Meta;
import com.example.affinity.affinity.response.StandardResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.apache.camel.Exchange;
import java.sql.Timestamp;

@Component
public class RestRouter extends RouteBuilder {

    @Override
    public void configure() {

        onException(Exception.class)
                .log("${exception.message}")
                .log("${exception.stacktrace}")
                .handled(true)
                .process(exchange -> {
                    StandardResponse response = StandardResponse.builder()
                            .data(Data.builder()
                                    .id(0L)
                                    .message("an error occurred. please contact administrator")
                                    .build()
                            )
                            .meta(Meta.builder()
                                    .status("ERROR")
                                    .timestamp(new Timestamp(System.currentTimeMillis()))
                                    .traceId("")
                                    .build()
                            )
                            .build();

                    ObjectMapper objectMapper = new ObjectMapper();
                    exchange.getIn().setBody(objectMapper.writeValueAsString(response));

                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE));


        restConfiguration()
                .component("servlet")
                .enableCORS(true)
                .corsHeaderProperty("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers")
                .bindingMode(RestBindingMode.off);

        rest()
                // health check endpoint
                .get("/")
                .to("direct:health")

                // billable file upload endpoint
                .post("/submit-billable-hours")
                .consumes(MediaType.MULTIPART_FORM_DATA_VALUE)
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .to("direct:save-billable-hours")

                // employees
                .post("/employees")
                .consumes(MediaType.APPLICATION_JSON_VALUE)
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .to("direct:save-employee")

                .get("/employees")
                .to("direct:get-employees")
                .produces(MediaType.APPLICATION_JSON_VALUE)

                .get("/employees/{id}")
                .to("direct:get-employee")
                .produces(MediaType.APPLICATION_JSON_VALUE)

                // company
                .post("/companies")
                .consumes(MediaType.APPLICATION_JSON_VALUE)
                .to("direct:save-client")
                .produces(MediaType.APPLICATION_JSON_VALUE)

                .get("/companies")
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .to("direct:get-clients")

                .get("/companies/{id}")
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .to("direct:get-client")

                // invoice
                .get("/invoices/company/{companyId}")
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .to("direct:get-company-invoices")

                .get("/invoices/employee/{employeeId}")
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .to("direct:get-employee-data")

        ;

    }
}
