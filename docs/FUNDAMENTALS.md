# Fundamentals
The Packet Protocols library allows you to use TCP to send packets between a simple server and client configuration.
For an example of this, look at the `src/main/test/chat` directory. It has a `packets` directory, containing the data
for the protocol and the available packets. Then the `targets` with the actual `ChatApp` class inside of it.

# Protocol Layers
The protocol is split into "layers". These all aim to achieve the same end goal, but are different steps:

## Protocol
Represented by the `dev.akarah.protocol.Protocol` class, this represents an entire system of packet
definitions. This is essentially the packet declaration layer. If you want a packet for action, the `Protocol` and
`Packet` classes help you do that.

### Defining a new packet
To define a new packet, create a class that extends `Packet`. The first type of `Packet` should be the Intermediate
Type (more on
that later), and the second `Self` type being the same class. **If `Self` is not the same class you will run into
problems.**

The most important part is the `PacketSpecification`. This allows you to specify the data a packet has. For example,
a chat message packet might have a `username` and a `message`, both of which are `String`s. You could use this code
to describe it:

```java
@Override
public PacketSpecification<Pair<String, String>> specification() {
    return PacketSpecification
        .ofId(1)
        .withArguments(
            Argument.string()
                .comment("Username of the message"),
            Argument.string()
                .comment("Actual content of the message")
        );
}
```

The `.comment("...")` part is optional but it allows you to attach extra documentation data to specific arguments.
It will be rendered when `PacketSpecification#generateDocumentation` is invoked.

This code snippet returns a new `PacketSpecification` that specifies the packet must be known by the ID of 1, and
has 2 string arguments in it. Each `PacketSpecification` has an associated intermediate type. You can find it
automatically by invoking `PacketSpecification#javaTypeSignature()` on the packet specification. It will return a `String`
containing the Java type signature of the intermediate type.

### Valid Argument Types
There are some `Argument`s you can choose from:
- `singleByte()` Represents 1 byte.
- `integer()` Represents a 32-bit integer.
- `varInt()` Represents a variably-sized 64-bit integer in LEB-128 format.
- `string()` Represents a String.
- `arrayOf(type)` Represents an array of a type. Encoded as a 32-bit integer length followed by the array's elements.
- `optionalOf(type)` Represents an optional value. Encoded as a 1-byte flag, possibly followed by the type's value.
- `terminalOptionalOf(type)` Represents an optional value where it is inferred based on if there is more in the buffer
or not. **Behavior is undefined if not placed at ending positions of a `PacketSpecification`'s arguments.
- `pair(a, b)` Represents a tuple of `a` and `b`.
- `triple(a, b, c)` Represents a tuple of `a`, `b`, and `c`.
You can also create your own arguments by implementing `PacketFormat` on your own records.

## Intermediate Types
Intermediate Types represent the actual values that a `PacketSpecification` accepts. It's a value of `String`, `int`,
`Pair`, etc. Note that `Pair` and similar types are powered by the `javatuples` library. This is intermediate as you
are able to convert from/to high-level packets and low-level decoded/encoded `ByteBuffer`s (note encoding also requires
a `PacketSpecification`, so the code knows *exactly* what your packet does).