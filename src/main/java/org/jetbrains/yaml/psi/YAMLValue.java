package org.jetbrains.yaml.psi;

import consulo.language.psi.PsiElement;

import jakarta.annotation.Nullable;

public interface YAMLValue extends YAMLPsiElement {
    @Nullable
    PsiElement getTag();
}
