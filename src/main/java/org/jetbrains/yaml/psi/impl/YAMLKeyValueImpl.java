// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.psi.impl;

import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.pom.PsiDeclaredTarget;
import consulo.language.psi.*;
import consulo.language.util.IncorrectOperationException;
import consulo.navigation.ItemPresentation;
import consulo.navigation.ItemPresentationProvider;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.YAMLElementTypes;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.*;

public class YAMLKeyValueImpl extends YAMLPsiElementImpl implements YAMLKeyValue, PsiDeclaredTarget {
    public static final Image YAML_KEY_ICON = PlatformIconGroup.nodesProperty();

    public YAMLKeyValueImpl(final @Nonnull ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "YAML key value";
    }

    @Override
    public @Nullable PsiElement getKey() {
        PsiElement colon = findChildByType(YAMLTokenTypes.COLON);
        if (colon == null) {
            return null;
        }
        ASTNode node = colon.getNode();
        do {
            node = node.getTreePrev();
        }
        while (YAMLElementTypes.BLANK_ELEMENTS.contains(PsiUtilCore.getElementType(node)));

        if (node == null || PsiUtilCore.getElementType(node) == YAMLTokenTypes.QUESTION) {
            return null;
        }
        else {
            return node.getPsi();
        }
    }

    @Override
    public @Nullable YAMLMapping getParentMapping() {
        return ObjectUtil.tryCast(super.getParent(), YAMLMapping.class);
    }

    @Override
    public @Nullable String getName() {
        return getKeyText();
    }

    @Override
    public @Nonnull String getKeyText() {
        final PsiElement keyElement = getKey();
        if (keyElement == null) {
            return "";
        }

        if (keyElement instanceof YAMLScalar) {
            return ((YAMLScalar) keyElement).getTextValue();
        }
        if (keyElement instanceof YAMLCompoundValue) {
            return ((YAMLCompoundValue) keyElement).getTextValue();
        }

        final String text = keyElement.getText();
        return StringUtil.unquoteString(text);
    }

    @Override
    public @Nullable YAMLValue getValue() {
        for (PsiElement child = getLastChild(); child != null; child = child.getPrevSibling()) {
            if (PsiUtilCore.getElementType(child) == YAMLTokenTypes.COLON) {
                return null;
            }
            if (child instanceof YAMLValue) {
                return ((YAMLValue) child);
            }
        }
        return null;
    }

    @Override
    public @Nonnull String getValueText() {
        final YAMLValue value = getValue();
        if (value instanceof YAMLScalar) {
            return ((YAMLScalar) value).getTextValue();
        }
        else if (value instanceof YAMLCompoundValue) {
            return ((YAMLCompoundValue) value).getTextValue();
        }
        return "";
    }


    @Override
    public void setValue(@Nonnull YAMLValue value) {
        adjustWhitespaceToContentType(value instanceof YAMLScalar);

        if (getValue() != null) {
            getValue().replace(value);
            return;
        }

        final YAMLElementGenerator generator = YAMLElementGenerator.getInstance(getProject());
        if (isExplicit()) {
            if (findChildByType(YAMLTokenTypes.COLON) == null) {
                add(generator.createColon());
                add(generator.createSpace());
                add(value);
            }
        }
        else {
            add(value);
        }
    }

    private void adjustWhitespaceToContentType(boolean isScalar) {
        PsiElement colon = findChildByType(YAMLTokenTypes.COLON);
        assert colon != null;

        while (colon.getNextSibling() != null && !(colon.getNextSibling() instanceof YAMLValue)) {
            colon.getNextSibling().delete();
        }
        final YAMLElementGenerator generator = YAMLElementGenerator.getInstance(getProject());
        if (isScalar) {
            addAfter(generator.createSpace(), colon);
        }
        else {
            final int indent = YAMLUtil.getIndentToThisElement(this);
            addAfter(generator.createIndent(indent + 2), colon);
            addAfter(generator.createEol(), colon);
        }
    }

    @Override
    public ItemPresentation getPresentation() {
        ItemPresentation custom = ItemPresentationProvider.getItemPresentation(this);
        if (custom != null) {
            return custom;
        }
        final YAMLFile yamlFile = (YAMLFile) getContainingFile();
        final PsiElement value = getValue();
        return new ItemPresentation() {
            @Override
            public String getPresentableText() {
                if (value instanceof YAMLScalar) {
                    ItemPresentation presentation = ((YAMLScalar) value).getPresentation();
                    return presentation != null ? presentation.getPresentableText() : getValueText();
                }
                return getName();
            }

            @Override
            public String getLocationString() {
                return yamlFile.getName();
            }

            @Override
            public Image getIcon() {
                return PlatformIconGroup.nodesProperty();
            }
        };
    }

    @Override
    public PsiElement setName(@NonNls @Nonnull String newName) throws IncorrectOperationException {
        return YAMLUtil.rename(this, newName);
    }

    /**
     * Provide reference contributor with given method registerReferenceProviders implementation:
     * registrar.registerReferenceProvider(PlatformPatterns.psiElement(YAMLKeyValue.class), ReferenceProvider);
     */
    @Override
    @Nonnull
    public PsiReference[] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }

    private boolean isExplicit() {
        final ASTNode child = getNode().getFirstChildNode();
        return child != null && child.getElementType() == YAMLTokenTypes.QUESTION;
    }

    @Override
    public void accept(@Nonnull PsiElementVisitor visitor) {
        if (visitor instanceof YamlPsiElementVisitor) {
            ((YamlPsiElementVisitor) visitor).visitKeyValue(this);
        }
        else {
            super.accept(visitor);
        }
    }

    @Override
    public @Nullable TextRange getNameIdentifierRange() {
        PsiElement key = getKey();
        return key == null ? null : key.getTextRangeInParent();
    }
}
