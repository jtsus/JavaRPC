package org.kipdev.rabbit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kipdev.rabbit.impact.ClassImpactor;
import org.kipdev.rabbit.runner.SeparateClassloaderTestRunner;
import org.kipdev.rabbit.types.IndirectRabbitReceiver;
import org.kipdev.rabbit.types.MockMessageController;
import org.kipdev.rabbit.types.SimpleRabbitReceiver;

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

        RabbitMessageController.INSTANCE.registerExchange(SimpleRabbitReceiver.INSTANCE, "simple");

        SimpleRabbitReceiver.INSTANCE.synchronizePlayer(UUID.randomUUID(), 5);
    }

    @Test
    public void testIndirectImpact() {
        RabbitMessageController.INSTANCE.initialize(null, "org.kipdev");

        RabbitMessageController.INSTANCE.registerExchange(IndirectRabbitReceiver.INSTANCE, "indirect");

        IndirectRabbitReceiver.INSTANCE.renamePlayer("Justin", "Justin42069");
    }
}
