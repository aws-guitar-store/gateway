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

import com.example.sbms.gateway.model.Amps;
import com.example.sbms.gateway.model.Filter;
import com.example.sbms.gateway.model.Guitars;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

@Configuration
public class KafkaRequestReplyConfiguration {
    @Value("${spring.kafka.consumer.properties.event.guitars-provided.topic}")
    private String eventGuitarsProvidedTopic;

    @Value("${spring.kafka.consumer.properties.event.amps-provided.topic}")
    private String eventAmpsProvidedTopic;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConcurrentMessageListenerContainer<String, Guitars> filteredGuitarsContainer(ConcurrentKafkaListenerContainerFactory<String, Guitars> containerFactory) {
        ConcurrentMessageListenerContainer<String, Guitars> repliesContainer = containerFactory.createContainer(this.eventGuitarsProvidedTopic);
        repliesContainer.getContainerProperties().setGroupId(this.groupId);
        repliesContainer.setAutoStartup(false);
        return repliesContainer;
    }

    @Bean
    public ReplyingKafkaTemplate<String, Filter, Guitars> filteredGuitarsKafkaTemplate(ProducerFactory<String, Filter> producerFactory, ConcurrentMessageListenerContainer<String, Guitars> repliesContainer) {
        return new ReplyingKafkaTemplate<>(producerFactory, repliesContainer);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, Amps> filteredAmpsContainer(ConcurrentKafkaListenerContainerFactory<String, Amps> containerFactory) {
        ConcurrentMessageListenerContainer<String, Amps> repliesContainer = containerFactory.createContainer(this.eventAmpsProvidedTopic);
        repliesContainer.getContainerProperties().setGroupId(this.groupId);
        repliesContainer.setAutoStartup(false);
        return repliesContainer;
    }

    @Bean
    public ReplyingKafkaTemplate<String, Filter, Amps> filteredAmpsKafkaTemplate(ProducerFactory<String, Filter> producerFactory, ConcurrentMessageListenerContainer<String, Amps> repliesContainer) {
        return new ReplyingKafkaTemplate<>(producerFactory, repliesContainer);
    }
}
