package org.jetbrains.yaml.psi;

import javax.annotation.Nonnull;

import com.intellij.pom.PomTarget;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.Contract;

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