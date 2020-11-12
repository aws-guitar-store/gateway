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
package com.example.sbms.gateway.integration;

import com.example.sbms.gateway.integration.model.Filter;
import com.example.sbms.gateway.domain.model.Guitar;
import com.example.sbms.gateway.domain.model.Guitars;
import com.example.sbms.gateway.domain.service.data.GuitarRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@EnableKafka
@Component
public class EventDrivenGuitarRepository implements GuitarRepository {
    private final ReplyingKafkaTemplate<String, Filter, Guitars> kafkaTemplate;
    private final String eventGuitarsRequestedTopic;
    private final long eventGuitarsRequestedTimeout;

    public EventDrivenGuitarRepository(ReplyingKafkaTemplate<String, Filter, Guitars> kafkaTemplate, @Value("${spring.kafka.producer.properties.event.guitars-requested.topic}") String eventGuitarsRequestedTopic, @Value("${spring.kafka.producer.properties.event.guitars-requested.timeout}") long eventGuitarsRequestedTimeout) {
        this.kafkaTemplate = kafkaTemplate;
        this.eventGuitarsRequestedTopic = eventGuitarsRequestedTopic;
        this.eventGuitarsRequestedTimeout = eventGuitarsRequestedTimeout;
    }

    public List<Guitar> all() {
        return this.sendRequest(Filter.forAll()).getAll();
    }

    public Optional<Guitar> byId(String id) {
        return this.sendRequest(Filter.forId(id)).getOne();
    }

    private Guitars sendRequest(Filter filter) {
        ProducerRecord<String, Filter> producerRecord = new ProducerRecord<>(this.eventGuitarsRequestedTopic, filter);
        RequestReplyFuture<String, Filter, Guitars> reply = kafkaTemplate.sendAndReceive(producerRecord);
        try {
            reply.getSendFuture().get(eventGuitarsRequestedTimeout, TimeUnit.MILLISECONDS);
            ConsumerRecord<String, Guitars> response = reply.get(this.eventGuitarsRequestedTimeout, TimeUnit.MILLISECONDS);
            return response.value();
        }
        catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new IntegrationException(e);
        }
    }
}
