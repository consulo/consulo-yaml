// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.util.PsiTreeUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.psi.*;

import java.util.Collection;
import java.util.Collections;

public class YAMLSequenceItemImpl extends YAMLPsiElementImpl implements YAMLSequenceItem {
    public YAMLSequenceItemImpl(final @Nonnull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable YAMLValue getValue() {
        return PsiTreeUtil.findChildOfType(this, YAMLValue.class);
    }

    @Override
    public @Nonnull Collection<YAMLKeyValue> getKeysValues() {
        final YAMLMapping mapping = PsiTreeUtil.findChildOfType(this, YAMLMapping.class);
        if (mapping == null) {
            return Collections.emptyList();
        }
        else {
            return mapping.getKeyValues();
        }
    }

    @Override
    public String toString() {
        return "YAML sequence item";
    }

    @Override
    public void accept(@Nonnull PsiElementVisitor visitor) {
        if (visitor instanceof YamlPsiElementVisitor) {
            ((YamlPsiElementVisitor) visitor).visitSequenceItem(this);
        }
        else {
            super.accept(visitor);
        }
    }

    @Override
    public int getItemIndex() {
        PsiElement parent = getParent();
        if (parent instanceof YAMLSequence) {
            return ((YAMLSequence) parent).getItems().indexOf(this);
        }
        return 0;
    }
}