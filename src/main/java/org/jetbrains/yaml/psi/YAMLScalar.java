package org.jetbrains.yaml.psi;

import consulo.language.psi.PsiLanguageInjectionHost;

import javax.annotation.Nonnull;

public interface YAMLScalar extends YAMLValue, PsiLanguageInjectionHost {
  @Nonnull
  String getTextValue();

  boolean isMultiline();
}
