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

import com.example.sbms.gateway.integration.config.GatewayChannels;
import com.example.sbms.gateway.integration.config.StreamGateway;
import com.example.sbms.gateway.integration.model.Filter;
import com.example.sbms.gateway.domain.model.Guitar;
import com.example.sbms.gateway.domain.model.Guitars;
import com.example.sbms.gateway.domain.service.data.GuitarRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@EnableBinding({ Processor.class, GatewayChannels.class, StreamGateway.class })
@Component
public class EventDrivenGuitarRepository implements GuitarRepository {
    private final StreamGateway streamGateway;
    private final String eventGuitarsRequestedTopic;
    private final long eventGuitarsRequestedTimeout;

    public EventDrivenGuitarRepository(StreamGateway streamGateway, @Value("${spring.kafka.producer.properties.event.guitars-requested.topic}") String eventGuitarsRequestedTopic, @Value("${spring.kafka.producer.properties.event.guitars-requested.timeout}") long eventGuitarsRequestedTimeout) {
        this.streamGateway = streamGateway;
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
        return streamGateway.process(filter);
    }
}
