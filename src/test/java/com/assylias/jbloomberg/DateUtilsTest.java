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
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

@Test(groups = "unit")
public class DateUtilsTest {

  @Test(expectedExceptions = NullPointerException.class) public void getDateTime_null() {
    DateUtils.toDatetime(null);
  }

  @Test public void toDateTime_ok() {
    OffsetDateTime odt = OffsetDateTime.now().plusNanos(7).withOffsetSameInstant(ZoneOffset.ofHoursMinutes(2, 7));
    Datetime dt = DateUtils.toDatetime(odt);

    assertDatesEquals(dt, odt);
    assertEquals(dt.timezoneOffsetMinutes(), 2 * 60 + 7);
  }

  @Test(expectedExceptions = NullPointerException.class) public void toOffsetDateTime_null() {
    DateUtils.toOffsetDateTime(null);
  }

  @Test public void toOffsetDateTime_no_tz() {
    OffsetDateTime expected = OffsetDateTime.of(2015, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    OffsetDateTime actual = DateUtils.toOffsetDateTime(new Datetime(2015, 1, 1, 12, 0, 0, 0));

    assertEquals(actual, expected);
  }

  @Test public void toOffsetDateTime_ok() {
    OffsetDateTime expected = OffsetDateTime.now().plusNanos(7).withOffsetSameInstant(ZoneOffset.ofHoursMinutes(2, 7));
    Datetime dt = new Datetime(expected.getYear(), expected.getMonthValue(), expected.getDayOfMonth(), expected.getHour(), expected.getMinute(),
            expected.getSecond(), 0);
    dt.setNanosecond(expected.getNano());
    dt.setTimezoneOffsetMinutes(2 * 60 + 7);

    OffsetDateTime actual = DateUtils.toOffsetDateTime(dt);

    assertEquals(actual, expected);
  }

  @Test public void toOffsetTime_ok() {
    OffsetTime expected = OffsetTime.now().plusNanos(7).withOffsetSameInstant(ZoneOffset.ofHoursMinutes(2, 7));
    Datetime dt = new Datetime(expected.getHour(), expected.getMinute(), expected.getSecond(), 0);
    dt.setNanosecond(expected.getNano());
    dt.setTimezoneOffsetMinutes(2 * 60 + 7);

    OffsetTime actual = DateUtils.toOffsetTime(dt);

    assertEquals(actual, expected);
  }

  @Test public void toLocalDate_ok() {
    LocalDate expected = LocalDate.of(2015, 1, 1);
    Datetime dt = new Datetime(2015, 1, 1);

    LocalDate actual = DateUtils.toLocalDate(dt);

    assertEquals(actual, expected);
  }

  private void assertDatesEquals(Datetime dt, OffsetDateTime odt) {
    assertEquals(dt.year(), odt.getYear());
    assertEquals(dt.month(), odt.getMonth().getValue());
    assertEquals(dt.dayOfMonth(), odt.getDayOfMonth());
    assertEquals(dt.hour(), odt.getHour());
    assertEquals(dt.minute(), odt.getMinute());
    assertEquals(dt.second(), odt.getSecond());
    assertEquals(dt.nanosecond(), odt.getNano());
    assertEquals(dt.timezoneOffsetMinutes(), odt.getOffset().getTotalSeconds() / 60);
  }

}