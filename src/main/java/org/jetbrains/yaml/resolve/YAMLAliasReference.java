// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.resolve;

import consulo.document.util.TextRange;
import consulo.language.impl.psi.LeafPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.util.IncorrectOperationException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.yaml.psi.YAMLAnchor;
import org.jetbrains.yaml.psi.impl.YAMLAliasImpl;

import java.util.Collection;
import java.util.Objects;

public class YAMLAliasReference extends PsiReferenceBase<YAMLAliasImpl> {
    public YAMLAliasReference(YAMLAliasImpl alias) {
        super(alias);
    }

    @Override
    public @Nullable YAMLAnchor resolve() {
        return YAMLLocalResolveUtil.getResolveAliasMap(myElement.getContainingFile()).get(myElement);
    }

    @Override
    public PsiElement handleElementRename(@Nonnull String newElementName) throws IncorrectOperationException {
        getIdentifier().replaceWithText(newElementName);
        return myElement;
    }

    @Override
    public @Nonnull TextRange getRangeInElement() {
        return TextRange.from(getIdentifier().getStartOffsetInParent(), getIdentifier().getTextLength());
    }

    @Contract(pure = true)
    @Nonnull
    private LeafPsiElement getIdentifier() {
        return (LeafPsiElement) Objects.requireNonNull(myElement.getIdentifierPsi(), "Reference should not be created for aliases without name");
    }

    @Override
    @Nonnull
    public Object[] getVariants() {
        Collection<YAMLAnchor> defs = YAMLLocalResolveUtil.getFirstAnchorDefs(myElement.getContainingFile().getOriginalFile());
        return defs.toArray();
    }
}
