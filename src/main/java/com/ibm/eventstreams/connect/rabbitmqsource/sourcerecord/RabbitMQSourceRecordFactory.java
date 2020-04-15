package com.ibm.eventstreams.connect.rabbitmqsource.sourcerecord;

import com.ibm.eventstreams.connect.rabbitmqsource.config.RabbitMQSourceConnectorConfig;
import com.ibm.eventstreams.connect.rabbitmqsource.schema.EnvelopeSchema;
import com.ibm.eventstreams.connect.rabbitmqsource.schema.KeySchema;
import com.ibm.eventstreams.connect.rabbitmqsource.schema.ValueSchema;

import com.google.common.collect.ImmutableMap;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import org.apache.kafka.common.utils.SystemTime;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class RabbitMQSourceRecordFactory {

    private final RabbitMQSourceConnectorConfig config;
    private final Time time = new SystemTime();

    public RabbitMQSourceRecordFactory(RabbitMQSourceConnectorConfig config) {
        this.config = config;
    }

    public SourceRecord makeSourceRecord(String consumerTag, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] bytes) {
        final Map<String, ?> sourcePartition = ImmutableMap.of(EnvelopeSchema.FIELD_ROUTINGKEY, envelope.getRoutingKey());
        final Map<String, ?> sourceOffset = ImmutableMap.of(EnvelopeSchema.FIELD_DELIVERYTAG, envelope.getDeliveryTag());

        final Struct key = KeySchema.toStruct(basicProperties);
        final Struct value = ValueSchema.toStruct(consumerTag, envelope, basicProperties, bytes);

        final String topic = this.config.kafkaTopic;

        long timestamp = Optional.ofNullable(basicProperties.getTimestamp()).map(Date::getTime).orElse(this.time.milliseconds());

        return new SourceRecord(
                sourcePartition,
                sourceOffset,
                topic,
                null,
                key.schema(),
                key,
                value.schema(),
                value,
                timestamp
        );
    }
}