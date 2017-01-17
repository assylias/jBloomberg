##[v3.4]

###Added

- Allow to use custom SessionOptions
- Add a facility to monitor the state of the session

###Fixed

- Conversion between Bloomberg dates/datetimes and java.time LocalDate/OffsetDateTime/OffsetTime more robust
- Various javadoc errors
- Don't start local bb_comm process if using remote hosts only

###Changed

- OffsetDateTime is now used instead of ZonedDateTime

##[v3.5]

###Added

- Added a few methods to monitor errors encountered when subscribing to real time data

###Fixed

- When subscribing to an expired security twice an exception was thrown
- Deal with incorrect date/time type returned by the real time feed