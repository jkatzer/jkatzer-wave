// Copyright 2009 Google Inc. All Rights Reserved.
package org.waveprotocol.wave.model.document.util;

import org.waveprotocol.wave.model.document.ReadableDocument;

/**
 * Composite filter that calculates its filtering based on one or more sub-filters,
 *   passing their results to an external skipping strategy.
 *
 * @author patcoleman@google.com (Pat Coleman)
 */
public class FilterProduct<N, E extends N, T extends N> extends FilteredView<N, E, T> {
  /** Defines a strategy for skipping nodes. */
  public interface SkipStrategy<N> {
    /**
     * Calculates the skip level for a node, given how each filter determined it.
     * Note that the order in skipLevels will be the results from calling the
     *   filters provided to the constructor in order.
     *
     * @param node The node the calculation is for
     * @param skipLevels The sub-filter skip levels for the given node
     * @return The overall skip level as calculated by the strategy
     */
    Skip resolveSkip(N node, Skip[] skipLevels);
  }

  /** Strategy for calculating final skip. */
  private final SkipStrategy<N> resolver;

  /** Filters to delegate calls to. */
  private final FilteredView<N, E, T>[] filters;

  /**
   * Constructs a product of multiple filters, given a resolution strategy.
   * See {@link #argumentCheck} for details on preconditions for parameters.
   */
  public FilterProduct(SkipStrategy<N> resolver, FilteredView<N, E, T>... filters) {
    super(argumentCheck(filters));
    this.filters = filters;
    this.resolver = resolver;
  }

  @Override
  protected Skip getSkipLevel(N node) {
    // Calculate an array of all sub-filter skip results...
    Skip[] levels = new Skip[filters.length];
    for (int i = 0; i < filters.length; i++) {
      levels[i] = filters[i].getSkipLevel(node);
    }
    return resolver.resolveSkip(node, levels); // ...and delegate to resolver
  }

  /**
   * Parameter checking for the filter product.
   * This is a slightly nasty way of checking the constructor parameters while still allowing
   * a call to a superconstructor, hence returning the document needed for that call.
   *
   * Checks to see that there's at least one filter given, that it's not null, and that
   * all the filters provided are over the same inner raw substrate.
   *
   * @param filters The sub-filters for this product.
   * @return The readable inner document that all the filters wrap around.
   */
  public static <N, E extends N, T extends N> ReadableDocument<N, E, T>
      argumentCheck(FilteredView<N, E, T>... filters) {
    assert filters.length > 0;
    assert filters[0] != null;
    for (int i = 1; i < filters.length; i++) {
      assert filters[i] != null;
      assert filters[0].inner == filters[i].inner;
    }
    return filters[0].inner;
  }
}
