/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nesscomputing.amqp;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import com.nesscomputing.amqp.PublisherCallback.PublisherData;

/**
 * A general runnable that will keep the connection to an exchange alive and enqueue messages
 * on the exchange.
 */
public final class ExchangePublisher<T> extends AbstractPublisher<T>
{
    private final String routingKey;

    public ExchangePublisher(@Nonnull final ConnectionFactory connectionFactory,
                             @Nonnull final AmqpConfig amqpConfig,
                             @Nonnull final String name,
                             @Nonnull final PublisherCallback<? super T> publisherCallback)

    {
        super(connectionFactory, amqpConfig, name, publisherCallback);

        this.routingKey = amqpConfig.getRoutingKey();
    }

    @Override
    protected String getServiceType()
    {
        return "exchange-publisher";
    }

    @Override
    protected void connectCallback(@Nonnull final Channel channel) throws IOException
    {
        if (getConfig().isDeclaring()) {
            channel.exchangeDeclare(getName(),
                                    getConfig().getExchangeType(),
                                    getConfig().isDurable(),
                                    getConfig().isAutoDelete(), null);
        }
    }

    @Override
    protected void publish(final PublisherData publisherData) throws IOException
    {
        final Channel channel = channelConnect();
        // An exchange has its own name and the default routing key...
        channel.basicPublish(getName(), routingKey, publisherData.getProperties(), publisherData.getData());
    }
}
