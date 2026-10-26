package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiElement;
import org.jetbrains.yaml.psi.YAMLPsiElement;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oleg
 */
public class YAMLPsiElementImpl extends ASTWrapperPsiElement implements YAMLPsiElement {
    public YAMLPsiElementImpl(@Nonnull final ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "YAML element";
    }
}
