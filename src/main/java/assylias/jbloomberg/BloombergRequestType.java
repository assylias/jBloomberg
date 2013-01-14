/*
 * Copyright 2013 Yann Le Tallec.
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
package assylias.jbloomberg;

/**
 * The names internally used by Bloomberg to identify the various types of requests - users don't need to use these
 * values directly.
 */
public enum BloombergRequestType {

    HISTORICAL_DATA("HistoricalDataRequest"),
    INTRADAY_TICK("IntradayTickRequest"),
    INTRADAY_BAR("IntradayBarRequest"),
    REFERENCE_DATA("ReferenceDataRequest"),
    PORTFOLIO_DATA("PortfolioDataRequest");

    private final String requestName;

    BloombergRequestType(String requestName) {
        this.requestName = requestName;
    }

    /**
     * @return the type of request, for example "HistoricalDataRequest"
     */
    @Override
    public String toString() {
        return requestName;
    }
}
