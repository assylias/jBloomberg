/*
 * Copyright (C) 2016 - present by Yann Le Tallec.
 * Please see distribution for license.
 */

package com.assylias.jbloomberg;

/**
 * An error received after subscribing to a real time feed.
 * This can be a subscription failure due to the security being inactive for example.
 * The details of the error as reported by the Bloomberg API can be accessed through the getters.
 */
public final class SubscriptionError {

  private final String type;
  private final String topic;
  private final int errorCode;
  private final String category;
  private final String description;

  SubscriptionError(String type, String topic, int errorCode, String category, String description) {
    this.type = type;
    this.topic = topic;
    this.errorCode = errorCode;
    this.category = category;
    this.description = description;
  }

  /**
   *
   * @return Bloomberg internal error code.
   */
  public int getErrorCode() {
    return errorCode;
  }

  /**
   *
   * @return the error category, e.g. "NOT_MONITORABLE".
   */
  public String getCategory() {
    return category;
  }

  /**
   *
   * @return the error description, e.g. "Not monitorable. Security not active.".
   */
  public String getDescription() {
    return description;
  }

  /**
   *
   * @return the topic, typically the security symbol.
   */
  public String getTopic() {
    return topic;
  }

  /**
   *
   * @return The error type, e.g. SubscriptionFailure.
   */
  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return "SubscriptionError{" + "type=" + type + ", topic=" + topic + ", errorCode=" + errorCode + ", category=" + category + ", description=" + description +
            '}';
  }
}
