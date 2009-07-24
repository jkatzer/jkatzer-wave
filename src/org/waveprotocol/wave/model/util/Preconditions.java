/**
 * Copyright 2009 Google Inc.
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
 *
 */

package org.waveprotocol.wave.model.util;

/**
 * Utility functions for checking preconditions and throwing appropriate
 * Exceptions.
 *
 * This class mostly mimics com.google.common.base.Preconditions but only
 * implement the parts we need.
 * (we avoid external dependencies from model/)
 *
 *
 *
 */
public final class Preconditions {
  private Preconditions() {}

  public static void checkElementIndex(int index, int size) {
    assert size >= 0;
    if (index < 0) {
      throw new IndexOutOfBoundsException("index (" + index + ") must not be negative");
    }
    if (index >= size) {
      throw new IndexOutOfBoundsException(
          "index (" + index + ") must be less than size (" + size + ")");
    }
  }

  public static void checkPositionIndex(int index, int size) {
    assert size >= 0;
    if (index < 0) {
      throw new IndexOutOfBoundsException("index (" + index + ") must not be negative");
    }
    if (index > size) {
      throw new IndexOutOfBoundsException(
          "index (" + index + ") must not be greater than size (" + size + ")");
    }
  }

  public static void checkPositionIndexesInRange(int rangeStart, int start, int end, int rangeEnd) {
    assert rangeStart >= 0;
    assert rangeEnd >= rangeStart;

    if (start < rangeStart) {
      throw new IndexOutOfBoundsException("start index (" + start + ") " +
          "must not be less than start of range (" + rangeStart + ")");
    }
    if (end < start) {
      throw new IndexOutOfBoundsException(
          "end index (" + end + ") must not be less than start index (" + start + ")");
    }
    if (end > rangeEnd) {
      throw new IndexOutOfBoundsException(
          "end index (" + end + ") must not be greater than end of range (" + rangeEnd + ")");
    }
  }

  public static void checkPositionIndexes(int start, int end, int size) {
    checkPositionIndexesInRange(0, start, end, size);
  }

  public static void checkNotNull(Object x, Object errorMessage) {
    if (x == null) {
      throw new NullPointerException("" + errorMessage);
    }
  }

  public static void checkArgument(boolean condition, Object errorMessage) {
    if (!condition) {
      throw new IllegalArgumentException("" + errorMessage);
    }
  }
}
