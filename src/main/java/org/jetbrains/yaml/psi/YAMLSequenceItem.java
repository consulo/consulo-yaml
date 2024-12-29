package org.jetbrains.yaml.psi;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;

/**
 * @author oleg
 */
public interface YAMLSequenceItem extends YAMLPsiElement {
    @Nullable
    YAMLValue getValue();

    @Nonnull
    Collection<YAMLKeyValue> getKeysValues();
}