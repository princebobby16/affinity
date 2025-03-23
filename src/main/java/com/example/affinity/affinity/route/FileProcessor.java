package com.example.affinity.affinity.route;

import com.example.affinity.affinity.model.GroupedStandardListResponseAggregation;
import com.example.affinity.affinity.model.GroupedStringAggregation;
import com.example.affinity.affinity.model.Invoice;
import com.example.affinity.affinity.service.InvoiceService;
import com.example.affinity.affinity.utils.Helpers;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.minio.MinioConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

@Component
public class FileProcessor extends RouteBuilder {

    public static String currentFileName = "";

    @Override
    public void configure() {

        from("spring-rabbitmq:{{env:AFFINITY_RABBITMQ_EXCHANGE}}?queues={{env:AFFINITY_RABBITMQ_EXCHANGE}}&routingKey=file&disableReplyTo=true")
                .log("received message")
                .log("${body}")
                .unmarshal().json(JsonLibrary.Jackson, HashMap.class)
                .log("${body[file-name]}")
                .setVariable("file-name", simple("${body[file-name]}"))
                .setHeader("CamelMinioObjectName", variable("file-name"))
                .process(exchange -> currentFileName = exchange.getIn().getHeader(MinioConstants.OBJECT_NAME, String.class))
                .log("file name received");

        from("minio://{{env:AFFINITY_MINIO_BILLABLE_HOURS_BUCKET}}?accessKey={{env:AFFINITY_MINIO_USERNAME}}&secretKey={{env:AFFINITY_MINIO_PASSWORD}}&endpoint={{env:AFFINITY_MINIO_HOST}}:{{env:AFFINITY_MINIO_PORT}}&deleteAfterRead=true&objectName="+ currentFileName)
                .process(exchange -> currentFileName = "")
                .log("${body}")
                .setBody().method(Helpers.class, "parseCSV(${body})")
                .split(body()).aggregationStrategy(new GroupedStringAggregation())
                    .to("direct:process-and-store-billable-hours")
                .end()
                .log("+++++++++++++++++++++ ${body}")
                .process(exchange -> {
                    List<String> companyNames = exchange.getIn().getBody(List.class);
                    List<String> uniqueCompanyNames = new ArrayList<>(new LinkedHashSet<>(companyNames)); // Remove duplicates
                    exchange.getIn().setBody(uniqueCompanyNames);
                })
                .log("${body}")
                .choice()
                .when(simple("${body.size()} > 0"))
                    .to("direct:find-and-process-invoice")
                .otherwise()
                    .log("nothing to be done as this file has already been process before ${body}")
                .endChoice()
                .end();


        from("direct:find-and-process-invoice")
                .split(body()).aggregationStrategy(new GroupedStandardListResponseAggregation())
                    .bean(InvoiceService.class, "findCompanyInvoiceList(${body})")
                .end()
                .log("creating invoices for companies")
                .split(body()).process(exchange -> {
                    // Create Excel workbook and sheet
                    try (Workbook workbook = new XSSFWorkbook()) {
                        Sheet sheet = workbook.createSheet("Invoice");

                        // Sample Data (Can be fetched dynamically)
                        List<Invoice> data = exchange.getIn().getBody(List.class);

                        // Create header row
                        Row header = sheet.createRow(0);
                        header.createCell(0).setCellValue("Company: ");
                        header.createCell(1).setCellValue(data.get(0).getCompanyName());

                        Row header1 = sheet.createRow(2);
                        header1.createCell(0).setCellValue("Employee ID");
                        header1.createCell(1).setCellValue("Number of Hours");
                        header1.createCell(2).setCellValue("Unit Price");
                        header1.createCell(3).setCellValue("Cost");

                        int rowNum = 3;
                        Float totalCost = 0f;
                        for (Invoice invoice : data) {
                            Row row = sheet.createRow(rowNum++);
                            row.createCell(0).setCellValue(invoice.getEmployeeId().toString());
                            row.createCell(1).setCellValue(invoice.getNoOfHours().toString());
                            row.createCell(2).setCellValue(invoice.getUnitPrice().toString());
                            row.createCell(3).setCellValue(invoice.getCost().toString());
                            totalCost += invoice.getCost();
                        }

                        Row header2 = sheet.createRow(rowNum);
                        header2.createCell(2).setCellValue("Total");
                        header2.createCell(3).setCellValue(totalCost.toString());

                        int year = LocalDateTime.now().getYear();
                        int month = LocalDateTime.now().getMonthValue();

                        // Write to ByteArrayOutputStream
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        workbook.write(outputStream);

                        // Set the output as the file content
                        exchange.getIn().setBody(outputStream.toByteArray());
                        exchange.getIn().setHeader(Exchange.FILE_NAME, "invoice-" + data.get(0).getCompanyName() + "-" + year + "-" + month + ".xlsx");
                        exchange.getIn().setHeader(MinioConstants.OBJECT_NAME, "invoice-" + data.get(0).getCompanyName() + "-" + year + "-" + month + ".xlsx");

                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                })
                    .to("file:{{env:AFFINITY_HOME_DIR}}/files")
                    .log("file successfully stored in directory")
                .end();

        from("direct:process-and-store-billable-hours")
                .log("${body}")
                .setHeader("company-name", variable("file-name"))
                .bean(InvoiceService.class, "storeInvoice(${body})")
                .log("invoice stored successfully ${body}");

        from("file:{{env:AFFINITY_HOME_DIR}}/files")
                .log("processing file: ${header.CamelFileNameOnly}")
                .setHeader(MinioConstants.OBJECT_NAME , simple("${header.CamelFileNameOnly}"))
                .to("minio://{{env:AFFINITY_MINIO_INVOICE_BUCKET}}?accessKey={{env:AFFINITY_MINIO_USERNAME}}&secretKey={{env:AFFINITY_MINIO_PASSWORD}}&endpoint={{env:AFFINITY_MINIO_HOST}}:{{env:AFFINITY_MINIO_PORT}}")
                .log("file processing done");
    }
}
