/*
 * Copyright 2015 Yann Le Tallec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Datetime;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.GregorianCalendar;
import static java.util.Objects.requireNonNull;

/**
 * A utility class to convert between Bloomberg dates and java.time objects.
 */
final class DateUtils {

  private DateUtils() {
  }

  /**
   * Returns a Bloomberg Datetime at the same instant with the same offset.
   *
   * @param odt an OffsetDateTime
   *
   * @return a Bloomberg Datetime at the same instant with the same offset.
   *
   * @throws NullPointerException if odt is null
   */
  public static Datetime toDatetime(OffsetDateTime odt) {
    requireNonNull(odt);
    Datetime dt = new Datetime(GregorianCalendar.from(odt.toZonedDateTime()));
    dt.setNanosecond(odt.getNano());
    return dt;
  }

  /**
   * Returns an OffsetDateTime at the same instant with the same offset. If the given Datetime has no time zone information, UTC is assumed (which is supposed
   * to be the default behaviour in Bloomberg.
   *
   * @param dt a Bloomberg Datetime
   *
   * @return an OffsetDateTime Datetime at the same instant with the same offset.
   *
   * @throws NullPointerException if dt is null
   */
  public static OffsetDateTime toOffsetDateTime(Datetime dt) {
    requireNonNull(dt);
    boolean hasTz = dt.hasParts(Datetime.TIME_ZONE_OFFSET);

    int offsetInSeconds = hasTz ? dt.timezoneOffsetMinutes() * 60 : 0;
    ZoneOffset offset = ZoneOffset.ofTotalSeconds(offsetInSeconds);

    return OffsetDateTime.of(dt.year(), dt.month(), dt.dayOfMonth(), dt.hour(), dt.minute(), dt.second(), dt.nanosecond(), offset);
  }

  /**
   * Returns a LocalDate corresponding to the given Datetime. The time information of the given Datetime, if any, is ignored.
   *
   * @param dt a Bloomberg Datetime
   *
   * @return a LocalDate corresponding to the given Datetime.
   *
   * @throws NullPointerException if dt is null
   */
  public static LocalDate toLocalDate(Datetime dt) {
    //not calling toOffsetDateTime here as the time part may not be set or may be set with incorrect values (e.g. hour = 24)
    requireNonNull(dt);
    return LocalDate.of(dt.year(), dt.month(), dt.dayOfMonth());
  }

  /**
   * Returns an OffsetDateTime at the same instant with the same offset. If the given Datetime has no time zone information, UTC is assumed (which is supposed
   * to be the default behaviour in Bloomberg.
   *
   * @param dt a Bloomberg Datetime
   *
   * @return an OffsetDateTime Datetime at the same instant with the same offset.
   *
   * @throws NullPointerException if dt is null
   */
  public static OffsetTime toOffsetTime(Datetime dt) {
    //not calling toOffsetDateTime here as the date part may not be set or may be set with incorrect values (e.g. month = -1)
    requireNonNull(dt);
    boolean hasTz = dt.hasParts(Datetime.TIME_ZONE_OFFSET);

    int offsetInSeconds = hasTz ? dt.timezoneOffsetMinutes() * 60 : 0;
    ZoneOffset offset = ZoneOffset.ofTotalSeconds(offsetInSeconds);

    return OffsetTime.of(dt.hour() % 24, dt.minute(), dt.second(), dt.nanosecond(), offset);
  }
}
