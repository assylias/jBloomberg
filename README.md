##Welcome to jBloomberg

jBloomberg is a high-level API that wraps the [low level Bloomberg Desktop Java API](http://www.openbloomberg.com/open-api/).
Although most features of the underlying Bloomberg API are available, some options might not be reachable through the jBloomberg API.

You can browse the [javadoc](http://assylias.github.com/jBloomberg/apidocs/index.html) for more information, including example usages.

###Requirements

**As of v3, Java 8 is required.**

###Description

The main advantages of this library vs. the Bloomberg API are:

- Less string based configuration: whenever possible enums are used to remove the typos issues
- Less verbose: retrieving historical data literally takes 5 lines of code, whereas when using the Bloomberg API,
the code gets quickly cluttered with parsing, error handling and so on
- Fluent design: most queries to Bloomberg are prepared with builders using the fluent interface pattern
- The library takes thread safety seriously (so does the Bloomberg API): all actions / objects are thread safe
and can be used in a multi threaded application (unless indicated otherwise, for example the builders)
- Uses the standard java.util.concurrent package objects, so the syntax / way of doing things should look familiar
to Java developers. For example, a historical data request returns a `Future<HistoricalData>`

It should however be noted that using jBloomberg does increase memory consumption and GC vs. using BLPAPI directly although for most
applications that should not be noticeable.

###Stability

Note that the API is not stable yet and its design could be subject to changes in the future.

###License

Apache License v2.0

###Dependencies

####Source dependencies

- guava (Apache License v2.0)
- slf4j (MIT License)
- Bloomberg BLPAPI (tested with 3.8.7.2) (MIT-like License)
- Big Blue Utils (Apache License v2.0)

####Test dependencies

- jmockit (MIT License)
- testNG (Apache License v2.0)

##Issues

If you find a bug or want a new feature you can ask in the [Issues section](https://github.com/assylias/jBloomberg/issues) on github. For general questions or if you are unable to get something to work
you can ask on stackoverflow using the [jbloomberg tag](http://stackoverflow.com/tags/jbloomberg/info) in your question.