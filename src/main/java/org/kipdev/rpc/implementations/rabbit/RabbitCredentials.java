package org.kipdev.rpc.implementations.rabbit;

import lombok.Data;

@Data
public class RabbitCredentials {
    private final String host, username, password, virtualHost;
}
