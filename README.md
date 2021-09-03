#RPC

Client for creating RPCs on a variety of platforms. 
Rabbit implementation included by default but can support a variety of others.

----

How to use
--
In this example Rabbit will be used however any other framework can be used just as well.

First, initialize the RabbitMessageController with your RabbitCredentials and base package.
```java
RabbitCredentials credentials = new RabbitCredentials("host", "username", "password", "vhost");
RabbitMessageController.INSTANCE.initialize(credentials, "org.kipdev");
```
The package will be searched through to transform all contained Exchanges with an @RPC.

This will allow all RPCs in the `org.kipdev` package to be used properly. 
Any that are missing can be registered manually with the ClassImpactor.

Next, create an exchange with an RPC contained in it like the following.
```java
public enum ExampleExchange implements Exchange {
    INSTANCE;

    @RPC
    public void logChatMessage(String sender, String message) {
        System.out.printf("%s: %s\n", sender, message);
    }
}
```

Finally, register the ExampleExchange with the channel to send the messages over.
```java
RPCController.INSTANCE.registerExchange(ExampleExchange.INSTANCE, "example");
```
RabbitMessageController can also be used here, there is no difference.

Now the logChatMessage has been turned into a fully functioning RPC. When any source invokes the method is 
it will translate the parameters and method name into an array of bytes to broadcast
over Rabbit. Then when any listening servers receive the data it will call the original method
with the deserialized parameters automatically.

---

Implementation Details
--

By default, this library uses Gson to handle serialization of parameters. 
Gson allows for registration of new type adaptors to handle complex types which is supported through GsonDataParser#registerTypeAdapter.
However, if more fine grain control is required a new DataSerializer can be registered in RPCController.

The RPCController itself can also be overwritten to work with other technologies besides Rabbit. This is actually what
RabbitMessageController does under the hood.

If no class impacting is desired, developers are also able to interact with Rabbit more directly by calling 
Exchange#sendMessage. However, make sure to either overwrite Exchange#receiveMessage or suffix the method
with $receive. In this case make sure to also omit the @RPC annotation from the source.

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
