// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.navigation.ItemPresentation;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import consulo.yaml.localize.YAMLLocalize;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLValue;
import org.jetbrains.yaml.psi.YamlPsiElementVisitor;

public class YAMLDocumentImpl extends YAMLPsiElementImpl implements YAMLDocument {
    public YAMLDocumentImpl(final @Nonnull ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "YAML document";
    }

    @Override
    public @Nullable YAMLValue getTopLevelValue() {
        return PsiTreeUtil.findChildOfType(this, YAMLValue.class);
    }

    @Override
    public void accept(@Nonnull PsiElementVisitor visitor) {
        if (visitor instanceof YamlPsiElementVisitor) {
            ((YamlPsiElementVisitor) visitor).visitDocument(this);
        }
        else {
            super.accept(visitor);
        }
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Override
            public @Nonnull String getPresentableText() {
                return YAMLLocalize.elementPresentationDocumentType().get();
            }

            @Override
            public @Nonnull String getLocationString() {
                return getContainingFile().getName();
            }

            @Override
            public @Nonnull Image getIcon() {
                return PlatformIconGroup.nodesOperatorclass();
            }
        };
    }
}
