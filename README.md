# quarkus-camel-amqp-jdbc
 
Version 1.0.0
Kevin Boone, September 2022

## What is this?

`quarkus-camel-amqp-jdbc` is a simple application for the Apache Camel
extension for Quarkus, that consumes messages from a message broker using
the AMQP 1.0 protocol, and writes their message bodies to a database
table. As configured, it connects to an Oracle database but, thanks to the
magic of Quarkus, supporting a different database requires only trivial
changes.

All the configuration is in a single file: 
`src/main/resources/application.properties`. Run-time settings in this
file can be overridden using environment variables. 

The application using the `camel-amqp` component to subscribe to a 
specific queue on the message broker. As messages are received, 
the `camel-jdbc` component formulates and executes an `insert` query
to append the message to a database table.

The AMQP protocol does not support XA transactions, so there is no
automated way roll back the message consumption if the database cannot
be updated. Instead, an exception handler writes the message body
to a file in a specific directory using the `camel-file` component.

The Camel route is defined in file `camel-routes.xml`. The only other
program file, `CamelResource.java` demonstrates how the database may
be accessed from Java code (but nothing is actually implemented -- it's
just for illustration).

If GraalVM is installed, this application can be compiled ahead-of-time
to native code, which makes start-up essentially instantaneous.

## Prerequisites

- Java JDK 11 or later
- Maven version 3.2.8 or later
- A running relational database
- A message broker, e.g., Apache Artemis, Apache ActiveMQ.
- Some way to put text messages onto a specific queue on the message
  broker. Artemis provides a way to post messages using the administration
  console. Alternatively, see my `amqutil` utility: 
  https://github.com/kevinboone/amqutil 

To run the code without changes, the database will need to be created
with the following structure:

    create table messages (body varchar(255));

## Configuration

All configuration parameters are in `src/resources/application.properties`.
These include the database URL and credentials, AMQP broker URL and
credentials, queue name, and table name. 

All the setting in `application.properties` can be over-ridden at run time
using environment variables. For example, to set `table.name` use the
environment variable `TABLE_NAME`. This works for the native compilation
and the Java JAR.

To configure for a database other than Oracle, all that should be necessary is
to change the settings `quarkus.datasource.camel-ds.db-kind` and
`quarkus.datasource.camel-ds.jdbc.url`. However, you'll also have to
add the necessary support into the build, by adding the associated
`quarkus-jdbc-XXX` dependency to pom.xml. 


## Running

To run the self-contained JAR (assuming you have configured it to be built):

   java -jar target/quarkus-camel-ampq-jdbc-1.0.0-runner.jar

On Linux, you can set configuration values at run time using environment
variables, like this:

    TABLE_NAME=foo QUEUE_NAME=bar java -jar target/...

To run in development mode, using

    mvn quarkus:dev

A useful feature of development mode, apart from enabling remote debugging,
is that allows the log level to be changed using a keypress.

## Native compilation

If GraalVM, or an equivalent, is installed, this application can be compiled
to a native executable, and will run without a JVM. The Quarkus maintainers
now recommend Mandrel for compiling Quarkus to native code:
https://github.com/graalvm/mandrel/releases

To build the native executable it should only be necessary to use the
`-Pnative` profile with Maven:

	GRAALVM_HOME=/path/to/graalvm mvn clean package -Pnative

This compilation process takes a long time for such a small program: 
minutes to tens of minutes. 

The size of the native binary is about 80 Mb. This is much larger than
the stand-alone Java JAR size (about 30 Mb) but, of course, the native
executable does not need a JVM. The start-up of the native executable is, 
compared to the Java JAR version, essentially instantaneous.

## Notes

1. _Database drivers_. It is not necessary to add a database driver 
to the application for any of the supported databases -- the database
extension has the necessary dependencies. This is true -- surprisingly
for me -- for Oracle as well. As of September 2022, the Oracle driver
that Quarkus selects does support native-executable compilation (that was
a sticking point in the past). Although it's possible to override
Quarkus' choice of driver, there's no guarantee that a non-standard
version will work, particularly with native compilation.

2. _Native compilation_. This radically reduces start-up time, but we cannot 
expect the same level of serviceability as a Java JVM provides. Even
a thread dump is not particularly comprehensible. 

3. _Camel support_. At the time of writing, Quarkus does not support 
the full set
of Camel endpoints. Those that are supported, have specific 
Quarkus dependencies. For example, rather than including a dependency
for `camel-amqp` like this: 

    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-amqp</artifactId>
    </dependency>

we need the specific Quarkus version:

    <dependency>
      <groupId>org.apache.camel.quarkus</groupId>
      <artifactId>camel-quarkus-amqp</artifactId>
    </dependency>

If there is no `camel-quarkus-XXX` dependency for a particular
Camel endpoint, it's probably safe to assume that it's not supported
by Quarkus.


