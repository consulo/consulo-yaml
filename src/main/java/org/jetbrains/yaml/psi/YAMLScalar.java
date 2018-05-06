package org.jetbrains.yaml.psi;

import com.intellij.psi.PsiLanguageInjectionHost;
import javax.annotation.Nonnull;

public interface YAMLScalar extends YAMLValue, PsiLanguageInjectionHost {
  @Nonnull
  String getTextValue();

  boolean isMultiline();
}
