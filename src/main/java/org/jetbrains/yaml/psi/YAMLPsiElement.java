package org.jetbrains.yaml.psi;

import consulo.language.psi.NavigatablePsiElement;

import java.util.List;

/**
 * @author oleg
 */
public interface YAMLPsiElement extends NavigatablePsiElement {
    List<YAMLPsiElement> getYAMLElements();
}
