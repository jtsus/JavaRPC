package org.kipdev.rpc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kipdev.rpc.impact.ClassImpactor;
import org.kipdev.rpc.runner.SeparateClassloaderTestRunner;
import org.kipdev.rpc.types.IndirectExchange;
import org.kipdev.rpc.types.MockMessageController;
import org.kipdev.rpc.types.SimpleExchange;

import java.util.UUID;

@RunWith(SeparateClassloaderTestRunner.class)
public class ClassImpactorTest {

    @Test
    public void testSimpleImpact() {
        RPCController.INSTANCE = MockMessageController.INSTANCE;

        ClassImpactor.register("org.kipdev.rpc.types.SimpleExchange");

        RPCController.INSTANCE.registerExchange("simple", SimpleExchange.INSTANCE);

        SimpleExchange.INSTANCE.synchronizePlayer(UUID.randomUUID(), 5);
    }

    @Test
    public void testIndirectImpact() {
        MockMessageController.INSTANCE.initialize("org.kipdev");

        RPCController.INSTANCE.registerExchange("indirect", IndirectExchange.INSTANCE);

        IndirectExchange.INSTANCE.renamePlayer("Justin", "Justin42069");
    }
}
