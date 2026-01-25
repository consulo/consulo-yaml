// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLValue;
import org.jetbrains.yaml.psi.YamlPsiElementVisitor;

public abstract class YAMLValueImpl extends YAMLPsiElementImpl implements YAMLValue {
    YAMLValueImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiElement getTag() {
        final PsiElement firstChild = getFirstChild();
        if (firstChild.getNode().getElementType() == YAMLTokenTypes.TAG) {
            return firstChild;
        }
        else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "YAML value";
    }

    @Override
    public void accept(@Nonnull PsiElementVisitor visitor) {
        if (visitor instanceof YamlPsiElementVisitor) {
            ((YamlPsiElementVisitor) visitor).visitValue(this);
        }
        else {
            super.accept(visitor);
        }
    }
}
