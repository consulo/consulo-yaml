package org.jetbrains.yaml.psi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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