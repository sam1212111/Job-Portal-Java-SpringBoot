package com.us.server.event;


import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.us.server.config.RabbitMqConfig;
import com.us.server.dto.UserEventDto;

@Service
public class UserEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishUserRegistered(UserEventDto event) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE,
                RabbitMqConfig.USER_REGISTERED_KEY,
                event
        );
        System.out.println("User registered event published: "
            + event.getEmail());
    }
}