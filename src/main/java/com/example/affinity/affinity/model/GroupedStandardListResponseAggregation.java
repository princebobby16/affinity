package com.example.affinity.affinity.model;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import java.util.ArrayList;
import java.util.List;

public class GroupedStandardListResponseAggregation implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (newExchange == null) {
            return oldExchange;
        }

        // Retrieve new list of invoices
        List<Invoice> newInvoices = newExchange.getIn().getBody(List.class);
        if (newInvoices == null || newInvoices.isEmpty()) {
            return oldExchange != null ? oldExchange : newExchange;
        }

        if (oldExchange == null) {
            // Start a new list of lists
            List<List<Invoice>> groupedInvoices = new ArrayList<>();
            groupedInvoices.add(newInvoices);
            newExchange.getIn().setBody(groupedInvoices);
            return newExchange;
        } else {
            // Retrieve existing aggregated lists
            List<List<Invoice>> groupedInvoices = oldExchange.getIn().getBody(List.class);
            if (groupedInvoices == null) {
                groupedInvoices = new ArrayList<>();
                oldExchange.getIn().setBody(groupedInvoices);
            }

            groupedInvoices.add(newInvoices);
            return oldExchange;
        }
    }

}
