package org.jetbrains.yaml.psi;

import javax.annotation.Nonnull;

/**
 * @author oleg
 */
public interface YAMLCompoundValue extends YAMLValue {
    @Nonnull
    String getTextValue();
}