package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElementVisitor;
import jakarta.annotation.Nonnull;
import org.jetbrains.yaml.psi.YamlPsiElementVisitor;

public class YAMLBlockSequenceImpl extends YAMLSequenceImpl {
    public YAMLBlockSequenceImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@Nonnull PsiElementVisitor visitor) {
        if (visitor instanceof YamlPsiElementVisitor) {
            ((YamlPsiElementVisitor) visitor).visitSequence(this);
        }
        else {
            super.accept(visitor);
        }
    }
}
