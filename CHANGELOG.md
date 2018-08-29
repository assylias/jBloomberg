## [v3.7]

### Added

- `BloombergSearchRequestBuilder` to query SRCH screens
- `InstrumentListRequestBuilder` to search instruments containing a given string
- `PortfolioRequestBuilder` to query PRTU portfolios

### Fixed

- Added an automatic module name for Java 9+ compatibility
- bbcomm.exe can also be located in a custom location, which can be configured using an environment variable `jbloomberg.bbcomm`

## [v3.6]

### Fixed

- bbcomm.exe can also be located in c:/blp/DAPI
- the price adjustments in `HistoricalRequestBuilder` and `IntradayBarRequestBuilder` were DPDF dependent

### Changed

- deprecate the `adjustDefault` method in `HistoricalRequestBuilder` and `IntradayBarRequestBuilder` as this is now the default behaviour
- add an `ignorePricingDefaults` method
- `BloombergException` is now unchecked

## [v3.5]

### Added

- Added a few methods to monitor errors encountered when subscribing to real time data
- Added new method `<T> T as(TypeReference<T> type)` on `TypedObject` to allow "casting" into a generic type

### Fixed

- When subscribing to an expired security twice an exception was thrown
- Deal with incorrect date/time type returned by the real time feed

### Changed

- removed dependency on external library Big Blue Utils
- package of TypedObject changed from com.assylias.bigblue.utils to com.assylias.jbloomberg

## [v3.4]

### Added

- Allow to use custom SessionOptions
- Add a facility to monitor the state of the session

### Fixed

- Conversion between Bloomberg dates/datetimes and java.time LocalDate/OffsetDateTime/OffsetTime more robust
- Various javadoc errors
- Don't start local bb_comm process if using remote hosts only

### Changed

- OffsetDateTime is now used instead of ZonedDateTime