// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.LeafPsiElement;
import consulo.language.psi.PsiElementVisitor;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.Contract;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.psi.YAMLAlias;
import org.jetbrains.yaml.psi.YamlPsiElementVisitor;
import org.jetbrains.yaml.resolve.YAMLAliasReference;

/**
 * Current implementation consists of 2 nodes: star symbol and name identifier
 */
public class YAMLAliasImpl extends YAMLValueImpl implements YAMLAlias {
    public YAMLAliasImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Override
    public @Nonnull String getAliasName() {
        LeafPsiElement identifier = getIdentifierPsi();
        return identifier == null ? "" : identifier.getText();
    }

    @Override
    public YAMLAliasReference getReference() {
        return getIdentifierPsi() == null ? null : new YAMLAliasReference(this);
    }

    @Override
    public String toString() {
        return "YAML alias";
    }

    /**
     * For now it could not return null but better do not rely on it
     */
    @Contract(pure = true)
    public @Nullable LeafPsiElement getIdentifierPsi() {
        return (LeafPsiElement) getLastChild();
    }

    @Override
    public void accept(@Nonnull PsiElementVisitor visitor) {
        if (visitor instanceof YamlPsiElementVisitor) {
            ((YamlPsiElementVisitor) visitor).visitAlias(this);
        }
        else {
            super.accept(visitor);
        }
    }
}
