# LogFileEventRecorder

[![Build Status](https://travis-ci.com/cscodingallan/LogFileEventRecorder.svg?branch=master)](https://travis-ci.com/cscodingallan/LogFileEventRecorder)

## Requirements

* Java 8
* Use of any open-source library is allowed
* Your program must use a gradle build system to resolve dependencies, build and test

## Summary of task
Our custom-build server logs different events to a file. Every event has 2 entries in a log - one entry when the event was started and another when the event was finished. The entries in a log file have no specific order (it can occur that a specific event is logged before the event starts)
Every line in the file is a JSON object containing event data:
* id - the unique event identifier
* state - whether the event was started or finished (can have values "STARTED" or "FINISHED" timestamp - the timestamp of the event in milliseconds
* Application Server logs also have the additional attributes: type - type of log
host - hostname

Sample JSON: 

    {"id":"scsmbstgra", "state":"STARTED", "type":"APPLICATION_LOG",
    "host":"12345", "timestamp":1491377495212}  
    {"id":"scsmbstgrb", "state":"STARTED", "timestamp":1491377495213}
    {"id":"scsmbstgrc", "state":"FINISHED", "timestamp":1491377495218}
    {"id":"scsmbstgra", "state":"FINISHED", "type":"APPLICATION_LOG",
    "host":"12345", "timestamp":1491377495217}
    {"id":"scsmbstgrc", "state":"STARTED", "timestamp":1491377495210}
    {"id":"scsmbstgrb", "state":"FINISHED", "timestamp":1491377495216}
    ...
    
## To Run

Pre-requisites:

* JDK v1.8 or more recent must be pre-installed 
* Tested only on Oracle JDK
* Gradle 4.9

Steps:

1. Clone git repository to local file system
2. cd into cloned repository root directory
3. `./gradlew run --args <path to json logfile>` e.g (from repository root directory): `./gradlew run --args src/test/resources/SampleLog1.json`

NOTE: this has only been tested on MacOSX so it might not work out-of-the-box on other platforms like MS Windows.

## The Database

The database configured as a HSQLDB filesystem database that by default is located in a folder in the current working 
directory and named `database`.

Edit `src/main/resources/application.properties` to change that or any other database configuration.

A new database will be created by the application overwriting if a previously existing database is in the same location.

The created database can be inspected with some external tool e.g SquirrelDB, RazorSQL.

## Logging

Logging configuration is defined in `src/main/resources/application.properties`. When set to DEBUG level the app will
log all persisted event details. Logging is configured to log to stdout/stderr - i.e. default configuration.

## Assumptions

* The only permitted database columns are eventId, event duration, type, host, alert
* Only 1 table is permitted in the database
* There is no requirement to support a continuously updating file - i.e. any new events added to the file while it is being processed or after it has been processed will not be captured
* There is enough ram available to hold all partial events in memory
* The “type” property of application log events may be any string - i.e. there is no defined set of types
* The input json log file is always valid and well formed (if it is not, the app will throw an exception, log it and fail to complete)
* The only logging output channel required to be configured is to stdout/stderr
* Type and host values of log events with the same id have the same values

## Approach

Uses Spring Boot because this was a simple and fast way to make an application with Spring for dependency injection along
with built-in logging and JPA support. The downsides of this approach are a) that testing end-to-end either seems to require
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
than persisting them i.e. to the extent that too many persistence threads would be generated (this theory has not been tested).
Regulating the number of threads available for persistence is however simple should the need arise.

### Error Handling

It is assumed that the JSON input file is valid and well-formed. The app will throw and log execeptions if
either of those assumptions are incorrect.

### Testing

Unit tests are in place that cover the business logic including from JSON InputStream to persisted EventDetails
(i.e. almost a complete end-to-end test). Manual testing with an included sample log file has also been conducted. 

## Todo

* Provide a distributed, runnable jar via gradle plugins
* Improve EntityManagedEventDetailsPersisentenceService to use a criteria or named query for its `allEventDetails`
implementation
* Improve the LogFileRecorderTest that it can autowire without having to use `@SpringBootApplication`
* Testing with large input log files along with performance monitoring
* Test on other platforms like MS Windows and Linux
* Consider options for fault tolerance - i.e. for unexpected JSON log events
* Consider automating testing with generated input files




