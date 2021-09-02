#RabbitRPC

Simple Rabbit client for creating RPCs.

----

How to use
--
Initialize the RabbitMessageController with your RabbitCredentials and base package.
The package will be used to transform all RabbitExchanges with a @RabbitRPC.
```java
RabbitCredentials credentials = new RabbitCredentials("host", "username", "password", "vhost");
RabbitMessageController.INSTANCE.initialize(credentials, "org.kipdev");
```
This will allow all RabbitRPCs in the `org.kipdev` package to be used properly. 
Any that are missing can be manually registered with the ClassImpactor.

Next, create an exchange with an RPC contained in it like the following.
```java
public enum ExampleExchange implements RabbitExchange {
    INSTANCE;

    @RabbitRPC
    public void logChatMessage(String sender, String message) {
        System.out.printf("%s: %s\n", sender, message);
    }
}
```

Finally, register the ExampleExchange with the rabbit channel to send the messages over.
```java
RabbitMessageController.INSTANCE.registerExchange(ExampleExchange.INSTANCE, "example");
```

Now the logChatMessage has been turned into a fully functioning RPC. When any source invokes the method is 
it will translate the parameters and method name into an array of bytes to broadcast
over Rabbit. Then when any listening servers receive the data it will call the original method
with the deserialized parameters.

---

Implementation Details
--

By default, this library uses Gson to handle serialization of parameters. 
Gson allows for registration of new type adaptors to handle complex types which is supported through GsonDataParser#registerTypeAdapter.
However, if more fine grain control is required a new DataSerializer can be registered in RabbitMessageController.

The RabbitMessageController itself can also be overwritten to work with other technologies besides Rabbit.

If no class impacting is desired, developers are also able to interact with Rabbit more directly by calling 
RabbitExchange#sendMessage. However, make sure to either overwrite RabbitExchange#receiveMessage or suffix the method
with $receive. In this case make sure to also omit the @RabbitRPC annotation from the source.

As an example:
```java
public enum ExampleExchange implements RabbitExchange {
    INSTANCE;
    
    public void logChatMessage(String sender, String message) {
        sendMessage("logChatMessage", sender, message);
    }

    public void logChatMessage$receive(String sender, String message) {
        System.out.printf("%s: %s\n", sender, message);
    }
}
```

This is actually what the class source looks like after being impacted normally as well.
