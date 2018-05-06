package org.jetbrains.yaml.psi;

import javax.annotation.Nullable;

import com.intellij.psi.PsiElement;

public interface YAMLValue extends YAMLPsiElement {
  @Nullable
  PsiElement getTag();
}
