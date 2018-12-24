## To Run

* JDK v1.8 or more recent must be pre-installed 
* Tested only on Oracle JDK
* Gradle 4.9

`./gradlew run --args <path to json logfile>`

e.g (from project root directory):

`./gradlew run --args src/test/resources/SampleLog1.json`

## The Database

The database configured as a HSQLDB filesystem database that by default is located in a folder in the current working 
directory and named `database`.

Edit `src/main/resources/application.properties` to change that or any other database configuration.

A new database will be created by the application overwriting if a previously existing database is in the same location.

## Logging

Logging configuration is defined in `src/main/resources/application.properties`. When set to DEBUG level the app will
log all persisted event details. Logging is configured to log to stdout/stderr - i.e. default configuration.


## Approach

Uses Spring Boot because this was a simple and fast way to make an application with Spring for dependency injection along
with built in logging and JPA support. The downsides of this approach are a) that testing end-to-end either seems to require
an xml Spring configuration or that the `CommandLineRunner` is executed as part of the test i.e. in order to 
get autowiring to work (there's probably some elegant way to resolve that) and b) slow startup time.

Uses RxJava to take some of the responsibility for concurrency along with boiler plate code that would go with
a DIY threading approach.

The input logfile is considered to be composed of an array of JSON LogEvent objects. A streamer is provided that 
streams each object sequentially rather than loading the entire JSON document into memory. This should 
enable files of unlimited size to be used as an input source provided there is enough ram to store all
log events that have no siblings and there is enough space for the database. (Caveat: the app has not been
tested with large input files.)

### Flow Of Execution

The flow of execution is as follows:

1. Command line app is invoked with the input JSON logfile as the only command line arg
2. A `JacksonJsonArraySimpleObjectStreamer` is used within an RxJava Observable to read each JSON LogEvent
3. Where a sibling of LogEvent has already been retrieved (where sibling is another LogEvent with the same id
but different state) then build an EventDetail using the 2 LogEvent siblings removing the previously encountered LogEvent
from temporary ram storage. Emit the built EventDetail
4. Persist emitted event details to the configured JPA storage

A single thread reads the JSON file and emits observed EventDetail instances. Other threads are used to persist
emitted EventDetails. An RxJava Schedulers.io pool provides the persistence threads. That pool is unlimited but
that is not expected to be of concern because the generation of EventDetails should not be signficantly faster
than persisting them i.e. to the extent that too many persistence threads would be generated (this has not been tested).
Regulating the number of threads available for persistence is however simple should the need arise.

### Error Handling

It is assumed that the JSON input file is valid and well-formed. The app will throw and log execeptions if
either of assumptions are incorrect.

### Testing

Unit tests are in place that cover the business logic including from JSON InputStream to persisted EventDetails
(i.e. almost a complete end-to-end test). Manual testing with an included sample log file has also been conducted. 

## Todo

* Provide a distributed, runnable jar via gradle plugins
* Improve EntityManagedEventDetailsPersisentenceService to use a criteria or named query for its `allEventDetails`
implementation
* Improve the LogFileRecorderTest that it can autowire without having to use `@SpringBootApplication`
* Testing with large input log files along with performance monitoring




