package org.jetbrains.yaml.psi;

import jakarta.annotation.Nonnull;

/**
 * @author oleg
 */
public interface YAMLCompoundValue extends YAMLValue {
    @Nonnull
    String getTextValue();
}