package ru.cubesolutions.evam.way4pusheraction;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import ru.cubesolutions.rabbitmq.EndPoint;
import ru.cubesolutions.rabbitmq.RabbitConfig;

import java.io.IOException;

/**
 * Created by Garya on 09.02.2018.
 */
public class EndpointWrapper extends EndPoint {

    private RabbitConfig rabbitConfig;

    public EndpointWrapper(RabbitConfig rabbitConfig) throws IOException {
        super(rabbitConfig);
        this.rabbitConfig = rabbitConfig;
    }

    public Channel getChannel() {
        return channel;
    }

    public Connection getConnection() {
        return connection;
    }


}
