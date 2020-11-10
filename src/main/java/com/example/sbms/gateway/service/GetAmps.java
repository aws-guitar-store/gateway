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

import com.example.sbms.gateway.model.Amp;
import com.example.sbms.gateway.model.Amps;
import com.example.sbms.gateway.model.Filter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@EnableKafka
public class GetAmps {
    private final ReplyingKafkaTemplate<String, Filter, Amps> kafkaTemplate;
    private final String eventAmpsRequestedTopic;
    private final long eventAmpsRequestedTimeout;

    public GetAmps(ReplyingKafkaTemplate<String, Filter, Amps> kafkaTemplate, @Value("${spring.kafka.producer.properties.event.amps-requested.topic}") String eventAmpsRequestedTopic, @Value("${spring.kafka.producer.properties.event.guitars-requested.timeout}") long eventAmpsRequestedTimeout) {
        this.kafkaTemplate = kafkaTemplate;
        this.eventAmpsRequestedTopic = eventAmpsRequestedTopic;
        this.eventAmpsRequestedTimeout = eventAmpsRequestedTimeout;
    }

    public List<Amp> all() {
        return this.sendRequest(Filter.forAll()).getAll();
    }

    public Optional<Amp> byId(String id) {
        return this.sendRequest(Filter.forId(id)).getOne();
    }

    private Amps sendRequest(Filter filter) {
        ProducerRecord<String, Filter> producerRecord = new ProducerRecord<>(this.eventAmpsRequestedTopic, filter);
        RequestReplyFuture<String, Filter, Amps> reply = kafkaTemplate.sendAndReceive(producerRecord);
        try {
            reply.getSendFuture().get(eventAmpsRequestedTimeout, TimeUnit.MILLISECONDS);
            ConsumerRecord<String, Amps> response = reply.get(this.eventAmpsRequestedTimeout, TimeUnit.MILLISECONDS);
            return response.value();
        }
        catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new ServiceException(e);
        }
    }
}
