// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.LeafPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.util.IncorrectOperationException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.psi.YAMLAnchor;
import org.jetbrains.yaml.psi.YAMLValue;
import org.jetbrains.yaml.psi.YamlPsiElementVisitor;

/**
 * Current implementation  consists of 2 nodes: ampersand symbol and name identifier
 */
public class YAMLAnchorImpl extends YAMLPsiElementImpl implements YAMLAnchor {
    public YAMLAnchorImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Override
    public @Nonnull String getName() {
        return getNameIdentifier().getText();
    }

    @Override
    public @Nonnull PsiElement getNameIdentifier() {
        return getLastChild();
    }

    @Override
    public @Nonnull PsiElement getNavigationElement() {
        return getNameIdentifier();
    }

    @Override
    public int getTextOffset() {
        return getNavigationElement().getNode().getStartOffset();
    }

    @Override
    public PsiElement setName(@Nonnull String name) throws IncorrectOperationException {
        PsiElement nameIdentifier = getNameIdentifier();
        assert nameIdentifier instanceof LeafPsiElement;

        ((LeafPsiElement) nameIdentifier).replaceWithText(name);

        return this;
    }

    @Override
    public @Nullable YAMLValue getMarkedValue() {
        PsiElement parent = getParent();
        if (parent instanceof YAMLValue) {
            return (YAMLValue) parent;
        }
        return null;
    }

    @Override
    public String toString() {
        return "YAML anchor";
    }

    @Override
    public void accept(@Nonnull PsiElementVisitor visitor) {
        if (visitor instanceof YamlPsiElementVisitor) {
            ((YamlPsiElementVisitor) visitor).visitAnchor(this);
        }
        else {
            super.accept(visitor);
        }
    }
}
