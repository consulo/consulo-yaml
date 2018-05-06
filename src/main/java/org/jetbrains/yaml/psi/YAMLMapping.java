package org.jetbrains.yaml.psi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Collection;

/**
 * A collection representing a set of key-value pairs
 */
public interface YAMLMapping extends YAMLCompoundValue {
  @Nonnull
  Collection<YAMLKeyValue> getKeyValues();

  @Nullable
  YAMLKeyValue getKeyValueByKey(@Nonnull String keyText);

  void putKeyValue(@Nonnull YAMLKeyValue keyValueToAdd);

  /**
   * This one's different from plain deletion in a way that excess newlines/commas are also deleted
   */
  void deleteKeyValue(@Nonnull YAMLKeyValue keyValueToDelete);
}
