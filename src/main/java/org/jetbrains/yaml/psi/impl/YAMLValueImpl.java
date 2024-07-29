package org.jetbrains.yaml.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

abstract class YAMLValueImpl extends YAMLPsiElementImpl implements YAMLValue {
    public YAMLValueImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    @RequiredReadAction
    public PsiElement getTag() {
        final PsiElement firstChild = getFirstChild();
        return firstChild.getNode().getElementType() == YAMLTokenTypes.TAG ? firstChild : null;
    }

    @Override
    public String toString() {
        return "YAML value";
    }
}
