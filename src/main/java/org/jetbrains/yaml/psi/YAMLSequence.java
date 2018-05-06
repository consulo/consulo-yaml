package org.jetbrains.yaml.psi;

import javax.annotation.Nonnull;

import java.util.List;

/**
 * A collection representing a sequence of items
 */
public interface YAMLSequence extends YAMLCompoundValue {
  @Nonnull
  List<YAMLSequenceItem> getItems();
}
