// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import jakarta.annotation.Nonnull;
import org.jetbrains.yaml.psi.YAMLCompoundValue;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YamlPsiElementVisitor;

/**
 * @author oleg
 */
public class YAMLCompoundValueImpl extends YAMLValueImpl implements YAMLCompoundValue {
    public YAMLCompoundValueImpl(final @Nonnull ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "YAML compound value";
    }

    @Override
    public @Nonnull String getTextValue() {
        PsiElement element = getTag() != null ? getTag().getNextSibling() : getFirstChild();

        while (element != null && !(element instanceof YAMLScalar)) {
            element = element.getNextSibling();
        }

        if (element != null) {
            return ((YAMLScalar) element).getTextValue();
        }
        else {
            return "<compoundValue:" + Integer.toHexString(getText().hashCode()) + ">";
        }
    }

    @Override
    public void accept(@Nonnull PsiElementVisitor visitor) {
        if (visitor instanceof YamlPsiElementVisitor) {
            ((YamlPsiElementVisitor) visitor).visitCompoundValue(this);
        }
        else {
            super.accept(visitor);
        }
    }
}