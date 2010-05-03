// Copyright 2009 Google Inc. All Rights Reserved.
package org.waveprotocol.wave.model.util;

import java.util.Map;

/**
 * A read-write interface to a map of strings to V.
 *
 * Null is not permitted as a key. All methods, even
 * {@link #containsKey(String)} will reject null keys.
 *
 * @param <V> type of values in the map
 */
public interface StringMap<V> extends ReadableStringMap<V> {
  /**
   * A function that accepts a key and the corresponding value from the map.
   */
  // TODO(ohler): Rename to EntryPredicate.
  public interface EntryFilter<V> {
    boolean apply(String key, V value);
  }

  /**
   * Sets the value associated with key to value.
   * If key already has a value, replaces it.
   * @param key must not be null
   */
  void put(String key, V value);

  /**
   * Removes the value associated with key.  Does nothing if there is none.
   */
  void remove(String key);

  /**
   * Equivalent to calling put(key, value) for every key-value pair in
   * pairsToAdd.
   *
   * TODO(ohler): Remove this requirement.
   * Any data structure making use of a CollectionsFactory must only pass
   * instances of ReadableStringMap created by that factory as the pairsToAdd
   * argument.  That is, if the Factory only creates StringMaps of a certain
   * type, you can rely on the fact that this method will only be called with
   * StringMaps of that type.
   */
  void putAll(ReadableStringMap<V> pairsToAdd);

  /**
   * The equivalent of calling put(key, value) for every key-value pair in
   * sourceMap.
   */
  void putAll(Map<String, V> sourceMap);

  /**
   * Removes all key-value pairs from this map.
   */
  void clear();

  /**
   * Call the filter for each key-value pair in the map, in undefined
   * order.  If the filter returns false, the pair is removed from the map;
   * if it returns true, it remains.
   */
  void filter(EntryFilter<V> filter);

}
