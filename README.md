# Introduction
In short, this project helps with <b>log aggregation</b>.

To accomplish this, this project provides functionalities to propagate log messages to an ELK stack via ZMQ.
Following message can be captured from within a Java application and forwarded to the ZMQ queue / ELK stack:
* java.util.logging
* log4j
* log4j2
* logback
* slf4j *(under development)*
* stdout / stderr
* Apache Tomcat
* proprietary log files *(under development)*

# Architecture
Log messages are captured e.g. by custom logging appenders and sent to a ZMQ queue. A ZMQ queue is very lightweight, very fast and supported out-of-the box by an ELK stack.
The ELK stack consists of three components:
* Logstash for receiving log messages, e.g. delivered by a ZMQ queue, and extracting appropriate log data.
* ElasticSearch for indexing the log data.
* Kibana for providing a Web based user interface with capabilities like fulltext search, filtering and dashboards.

# Global Configurations

Set following environment variables or Java system properties respectivly:

Environment Variable | Java system property | Remarks
---------------------|----------------------|--------
LOGKEEPER_HOST | logkeeper.host | Host name or IP address of ZMQ / Logstack server (mandatory).
LOGKEEPER_PORT | logkeeper.port | Port of ZMQ / Logstash server (default: 2120).
CONTAINER_LABEL | - | Label of this container (optional).
CONTAINER_CATEGORY | - | Category of this container (optional).
CONTAINER_COMPOUND_ID | - | Compound ID of this container (optional).

The idea of all environment variables starting with CONTAINER is to get a better overview and filtering capabilities inside the ELK analysis.

Examples:
* Container 1:
  * CONTAINER_CATEGORY = research_application
  * CONTAINER_COMPOUND_ID = dev_web_research
  * CONTAINER_LABEL = tomcat
* Container 2:
    * CONTAINER_CATEGORY = research_application
    * CONTAINER_COMPOUND_ID = dev_web_research
    * CONTAINER_LABEL = mongo_db


# Configuring java.util.logging
1) Add to your Java project a maven/gradle dependency to
*TODO*

2) Add following lines to your ```logging.properties```:

```
handlers = earth.cube.tools.logkeeper.loggers.java_logging.ForwardHandler
.level = WARNING

# ZMQ Logging
earth.cube.tools.logkeeper.loggers.java_logging.ForwardHandler.application = the_name_of_your_application
earth.cube.tools.logkeeper.loggers.java_logging.ForwardHandler.source = the_name_of_your_source

# Logging refinements
com.foo.handlers = earth.cube.tools.logkeeper.loggers.java_logging.ForwardHandler
com.foo.level = INFO
```

# Configuring log4j
1) Add to your Java project a maven/gradle dependency to
*TODO*

2) Add following lines to your ```log4j.properties```:

```
log4j.rootLogger=WARN, forward

log4j.appender.forward=earth.cube.tools.logkeeper.loggers.log4j.ForwardAppender
log4j.appender.forward.application=the_name_of_your_application
log4j.appender.forward.source=the_name_of_your_source

com.foo=INFO, forward
```

# Configuring log4j2
1) Add to your Java project a maven/gradle dependency to
*TODO*

2) Add following lines to your ```log4j2.yaml```:

```
Configuration:
  status: warn
  packages: earth.cube.tools.logkeeper.loggers.log4j2

  Appenders:
    Forward_Appender:
      name: Forward
      application: the_name_of_your_application
      source: the_name_of_your_source


  Loggers:
    Root:
      level: warn
      AppenderRef:
        ref: Forward
    Logger:
        - name: com.foo
          level: info
          AppenderRef:
            - ref: Forward
              level: info
```

# Configuring logback
1) Add to your Java project a maven/gradle dependency to
*TODO*

2) Add following lines to your ```logback.xml```:

```
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

	<appender name="FORWARDER" class="earth.cube.tools.logkeeper.loggers.logback.ForwardAppender">
		<application>the_name_of_your_application</application>
		<source>the_name_of_your_source</source>
	</appender>

	<root level="WARN">
		<appender-ref ref="FORWARDER" />
	</root>

	<logger name="com.foo" level="INFO" additivity="false">
		<appender-ref ref="FORWARDER" />
	</logger>

</configuration>
```

# Configuring stdout / stderr

1) Add to your Java project a maven/gradle dependency to
*TODO*

2) Set
* either an environment variable ```LOGKEEPER_APPLICATION``` with
```
export LOGKEEPER_APPLICATION=the_name_of_your_application
```
or
* a Java property ```logkeeper.application``` with
```
-Dlogkeeper.application=the_name_of_your_application
```

3) Execute at a very early stage of your application following line (corresponding class resides in package ```earth.cube.tools.logkeeper.core.streams```):
```
StreamRedirector.set();
```

# Configuring Apache Tomcat

1) Add following JAR files into the Catalina home:
*TODO*

2) Add following lines to your ```conf/logging.properties```:
```
handlers = 1catalina.earth.cube.tools.logkeeper.loggers.java_logging.ForwardHandler, 2localhost.earth.cube.tools.logkeeper.loggers.java_logging.ForwardHandler

.handlers = 1catalina.earth.cube.tools.logkeeper.loggers.java_logging.ForwardHandler

############################################################
# Handler specific properties.
# Describes specific configuration info for Handlers.
############################################################

1catalina.earth.cube.tools.logkeeper.loggers.java_logging.ForwardHandler.level = FINE
1catalina.earth.cube.tools.logkeeper.loggers.java_logging.ForwardHandler.application = Tomcat
1catalina.earth.cube.tools.logkeeper.loggers.java_logging.ForwardHandler.source = catalina

2localhost.earth.cube.tools.logkeeper.loggers.java_logging.ForwardHandler.level = FINE
2localhost.earth.cube.tools.logkeeper.loggers.java_logging.ForwardHandler.application = Tomcat
2localhost.earth.cube.tools.logkeeper.loggers.java_logging.ForwardHandler.source = localhost


############################################################
# Facility specific properties.
# Provides extra control for each logger.
############################################################

org.apache.catalina.core.ContainerBase.[Catalina].[localhost].level = INFO
org.apache.catalina.core.ContainerBase.[Catalina].[localhost].handlers = 2localhost.earth.cube.tools.logkeeper.loggers.java_logging.ForwardHandler
```
