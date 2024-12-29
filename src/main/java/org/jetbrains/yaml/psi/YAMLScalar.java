package org.jetbrains.yaml.psi;

import consulo.language.psi.PsiLanguageInjectionHost;

import jakarta.annotation.Nonnull;

public interface YAMLScalar extends YAMLValue, PsiLanguageInjectionHost {
    @Nonnull
    String getTextValue();

    boolean isMultiline();
}
