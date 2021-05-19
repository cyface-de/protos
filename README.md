# Cyface Protocol Buffer Messages

This repository defines the Cyface Models as schemas such as Protocol Buffer Schemas.
This allows to de-/serialize the Cyface Binary Data from in different programming languages.

For more details on Protocol Buffers (short: Protobuf) check out it's link:https://developers.google.com/protocol-buffers[documentation].

## Message Format Definitions

This is a collection of `.proto` files describe the data structure of the Cyface Models.
- This allows Protobuf to automatically de-/encode the the data from/to binary files.
- Supports later extentions of the format with backward-compatibility

At the time of writing the latest release is link:https://developers.google.com/protocol-buffers/docs/proto3[Protocol Buffer Version 3].

## Compiling the Message Definitions

Generates serializer, deserializer, etc. in a choosen language, e.g. `.java` files for Java.
See https://developers.google.com/protocol-buffers/docs/javatutorial#compiling-your-protocol-buffers

Java classes are pre-compiled (Protocol Buffers v3.17.0).

The serializers encode the data in an efficient way.

For more details check out the currently internal documentation link:https://cyface.atlassian.net/wiki/spaces/IM/pages/1535148033/Datenformat+bertragungsprotokoll+2021[here].


## Read and Write Messages

TBC