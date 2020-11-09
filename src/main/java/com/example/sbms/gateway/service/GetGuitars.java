/*
 * MIT License
 *
 * Copyright (c) 2020 Rarysoft Enterprises
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.example.sbms.gateway.service;

import com.example.sbms.gateway.itemcache.Cache;
import com.example.sbms.gateway.itemcache.PollingTimeout;
import com.example.sbms.gateway.model.Filter;
import com.example.sbms.gateway.model.Guitar;
import com.example.sbms.gateway.model.Guitars;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@EnableKafka
public class GetGuitars {
    private final KafkaTemplate<String, Filter> kafkaTemplate;
    private final String requestGuitarsTopic;
    private final long requestGuitarsTimeout;
    private final Cache<Guitar> guitars;

    public GetGuitars(KafkaTemplate<String, Filter> kafkaTemplate, @Value("${spring.kafka.producer.properties.event.guitars-requested.topic}") String requestGuitarsTopic, @Value("${spring.kafka.producer.properties.event.guitars-requested.timeout}") long requestGuitarsTimeout) {
        this.kafkaTemplate = kafkaTemplate;
        this.requestGuitarsTopic = requestGuitarsTopic;
        this.requestGuitarsTimeout = requestGuitarsTimeout;
        this.guitars = new Cache<>(Guitar::getId);
    }

    public List<Guitar> all() {
        if (this.guitars.isFullyPopulated()) {
            return new ArrayList<>(this.guitars.all());
        }
        this.requestAll();
        try {
            return new ArrayList<>(this.guitars.all(this.requestGuitarsTimeout));
        }
        catch (PollingTimeout e) {
            // TODO: handle timeout?
        }
        return new ArrayList<>();
    }

    public Optional<Guitar> byId(String id) {
        if (this.guitars.contains(id)) {
            return this.guitars.get(id);
        }
        this.requestById(id);
        return this.guitars.get(id, this.requestGuitarsTimeout);
    }

    @KafkaListener(topics = "${spring.kafka.consumer.properties.event.guitars-provided.topic}", containerFactory = "guitarsContainerFactory", groupId = "${spring.kafka.consumer.properties.unique-group-id}")
    public void receiveGuitars(@Payload Guitars guitars) {
        if (guitars.isCompleteCollection()) {
            this.guitars.setAll(guitars.getAll());
        }
        else {
            guitars.getAll().forEach(this.guitars::add);
        }
    }

    private void requestAll() {
        this.sendRequest(Filter.forAll());
    }

    private void requestById(String id) {
        this.sendRequest(Filter.forId(id));
    }

    private void sendRequest(Filter filter) {
        Message<Filter> message = MessageBuilder
                .withPayload(filter)
                .setHeader(KafkaHeaders.TOPIC, requestGuitarsTopic)
                .build();
        // TODO: error handling
        kafkaTemplate.send(message);
    }
}
