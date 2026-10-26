package org.jetbrains.yaml.psi;

import jakarta.annotation.Nonnull;

import java.util.List;

/**
 * A collection representing a sequence of items
 */
public interface YAMLSequence extends YAMLCompoundValue {
    @Nonnull
    List<YAMLSequenceItem> getItems();
}
