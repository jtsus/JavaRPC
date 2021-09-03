package org.kipdev.rpc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kipdev.rpc.impact.ClassImpactor;
import org.kipdev.rpc.runner.SeparateClassloaderTestRunner;
import org.kipdev.rpc.types.CustomSendExchange;
import org.kipdev.rpc.types.IndirectExchange;
import org.kipdev.rpc.types.MockMessageController;
import org.kipdev.rpc.types.SimpleExchange;

import java.util.UUID;

@RunWith(SeparateClassloaderTestRunner.class)
public class ClassImpactorTest {

    @Test
    public void testSimpleImpact() {
        RPCController.INSTANCE = MockMessageController.INSTANCE;

        ClassImpactor.registerPackage("org.kipdev.rpc.types", getClass());

        RPCController.INSTANCE.registerExchange("simple", SimpleExchange.INSTANCE);

        SimpleExchange.INSTANCE.synchronizePlayer(UUID.randomUUID(), 5);
    }

    @Test
    public void testIndirectImpact() {
        MockMessageController.INSTANCE.initialize("org.kipdev");

        RPCController.INSTANCE.registerExchange("indirect", IndirectExchange.INSTANCE);
        RPCController.INSTANCE.registerExchange("custom", CustomSendExchange.INSTANCE);

        IndirectExchange.INSTANCE.renamePlayer("Justin", "Justin42069");
        CustomSendExchange.INSTANCE.ping("Mock");
    }
}
