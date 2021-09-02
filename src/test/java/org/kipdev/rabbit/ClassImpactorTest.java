package org.kipdev.rabbit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kipdev.rabbit.impact.ClassImpactor;
import org.kipdev.rabbit.runner.SeparateClassloaderTestRunner;
import org.kipdev.rabbit.types.IndirectRabbitExchange;
import org.kipdev.rabbit.types.MockMessageController;
import org.kipdev.rabbit.types.SimpleRabbitExchange;

import java.util.UUID;

@RunWith(SeparateClassloaderTestRunner.class)
public class ClassImpactorTest {

    @Before
    public void registerMessageController() {
        RabbitMessageController.INSTANCE = new MockMessageController();
    }

    @Test
    public void testSimpleImpact() {
        ClassImpactor.register("org.kipdev.rabbit.types.SimpleRabbitReceiver");

        RabbitMessageController.INSTANCE.registerExchange(SimpleRabbitExchange.INSTANCE, "simple");

        SimpleRabbitExchange.INSTANCE.synchronizePlayer(UUID.randomUUID(), 5);
    }

    @Test
    public void testIndirectImpact() {
        RabbitMessageController.INSTANCE.initialize(new RabbitCredentials("host", "username", "password", "vhost"), "org.kipdev");

        RabbitMessageController.INSTANCE.registerExchange(IndirectRabbitExchange.INSTANCE, "indirect");

        IndirectRabbitExchange.INSTANCE.renamePlayer("Justin", "Justin42069");
    }
}
