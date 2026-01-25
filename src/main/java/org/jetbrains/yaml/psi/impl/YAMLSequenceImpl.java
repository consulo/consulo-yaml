// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.util.PsiTreeUtil;
import jakarta.annotation.Nonnull;
import org.jetbrains.yaml.psi.YAMLSequence;
import org.jetbrains.yaml.psi.YAMLSequenceItem;
import org.jetbrains.yaml.psi.YamlPsiElementVisitor;

import java.util.List;

public abstract class YAMLSequenceImpl extends YAMLCompoundValueImpl implements YAMLSequence {
    public YAMLSequenceImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Override
    public @Nonnull List<YAMLSequenceItem> getItems() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, YAMLSequenceItem.class);
    }

    @Override
    public @Nonnull String getTextValue() {
        return "<sequence:" + Integer.toHexString(getText().hashCode()) + ">";
    }

    @Override
    public String toString() {
        return "YAML sequence";
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
