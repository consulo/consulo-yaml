package org.jetbrains.yaml.psi;

import jakarta.annotation.Nullable;

/**
 * @author oleg
 */
public interface YAMLDocument extends YAMLPsiElement {
    @Nullable
    YAMLValue getTopLevelValue();
}
