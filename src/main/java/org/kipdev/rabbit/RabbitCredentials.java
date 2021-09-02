package org.kipdev.rabbit;

import lombok.Data;

@Data
public class RabbitCredentials {
    private final String host, username, password, virtualHost;
}
