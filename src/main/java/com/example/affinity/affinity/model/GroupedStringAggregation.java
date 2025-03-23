package com.example.affinity.affinity.model;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

import java.util.ArrayList;
import java.util.List;

public class GroupedStringAggregation implements AggregationStrategy {
    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null) {
            List<String> newList = new ArrayList<>();
            String body = newExchange.getIn().getBody(String.class);
            if (body != null) {
                newList.add(body);
            }
            newExchange.getIn().setBody(newList);
            return newExchange;
        } else {
            List<String> list = oldExchange.getIn().getBody(List.class);
            String body = newExchange.getIn().getBody(String.class);
            if (body != null) {
                list.add(body);
            }
            return oldExchange;
        }
    }
}
