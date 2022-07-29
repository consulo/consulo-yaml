package org.jetbrains.yaml.psi;

import consulo.language.pom.PomTarget;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author oleg
 */
public interface YAMLKeyValue extends YAMLPsiElement, PsiNamedElement, PomTarget {
  @Contract(pure = true)
  @Nullable
  PsiElement getKey();

  @Contract(pure = true)
  @Nonnull
  String getKeyText();

  @Contract(pure = true)
  @Nullable
  YAMLValue getValue();

  @Contract(pure = true)
  @Nonnull
  String getValueText();
  
  YAMLMapping getParentMapping();

  void setValue(@Nonnull YAMLValue value);
}