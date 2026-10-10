// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.pom.PsiDeclaredTarget;
import consulo.language.psi.*;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.navigation.ItemPresentationProvider;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.YAMLElementTypes;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.*;

public class YAMLKeyValueImpl extends YAMLPsiElementImpl implements YAMLKeyValue, PsiDeclaredTarget {
    public static final Image YAML_KEY_ICON = PlatformIconGroup.nodesProperty();

    public YAMLKeyValueImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "YAML key value";
    }

    @Override
    @RequiredReadAction
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
    @RequiredReadAction
    public @Nullable YAMLMapping getParentMapping() {
        return ObjectUtil.tryCast(super.getParent(), YAMLMapping.class);
    }

    @Override
    @RequiredReadAction
    public @Nullable String getName() {
        return getKeyText();
    }

    @Override
    @RequiredReadAction
    public @Nonnull String getKeyText() {
        PsiElement keyElement = getKey();
        if (keyElement == null) {
            return "";
        }

        if (keyElement instanceof YAMLScalar scalar) {
            return scalar.getTextValue();
        }
        if (keyElement instanceof YAMLCompoundValue compoundValue) {
            return compoundValue.getTextValue();
        }

        String text = keyElement.getText();
        return StringUtil.unquoteString(text);
    }

    @Override
    @RequiredReadAction
    public @Nullable YAMLValue getValue() {
        for (PsiElement child = getLastChild(); child != null; child = child.getPrevSibling()) {
            if (PsiUtilCore.getElementType(child) == YAMLTokenTypes.COLON) {
                return null;
            }
            if (child instanceof YAMLValue value) {
                return value;
            }
        }
        return null;
    }

    @Override
    @RequiredReadAction
    public @Nonnull String getValueText() {
        YAMLValue value = getValue();
        if (value instanceof YAMLScalar scalar) {
            return scalar.getTextValue();
        }
        else if (value instanceof YAMLCompoundValue compoundValue) {
            return compoundValue.getTextValue();
        }
        return "";
    }

    @Override
    @RequiredWriteAction
    public void setValue(@Nonnull YAMLValue value) {
        adjustWhitespaceToContentType(value instanceof YAMLScalar);

        if (getValue() != null) {
            getValue().replace(value);
            return;
        }

        YAMLElementGenerator generator = YAMLElementGenerator.getInstance(getProject());
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

    @RequiredWriteAction
    private void adjustWhitespaceToContentType(boolean isScalar) {
        PsiElement colon = findChildByType(YAMLTokenTypes.COLON);
        assert colon != null;

        while (colon.getNextSibling() != null && !(colon.getNextSibling() instanceof YAMLValue)) {
            colon.getNextSibling().delete();
        }
        YAMLElementGenerator generator = YAMLElementGenerator.getInstance(getProject());
        if (isScalar) {
            addAfter(generator.createSpace(), colon);
        }
        else {
            int indent = YAMLUtil.getIndentToThisElement(this);
            addAfter(generator.createIndent(indent + 2), colon);
            addAfter(generator.createEol(), colon);
        }
    }

    @Override
    @RequiredReadAction
    public ItemPresentation getPresentation() {
        ItemPresentation custom = ItemPresentationProvider.getItemPresentation(this);
        if (custom != null) {
            return custom;
        }
        final YAMLFile yamlFile = (YAMLFile) getContainingFile();
        final PsiElement value = getValue();
        return new ItemPresentation() {
            @Override
            @RequiredReadAction
            public LocalizeValue getPresentableText() {
                if (value instanceof YAMLScalar scalar) {
                    ItemPresentation presentation = scalar.getPresentation();
                    return presentation != null ? presentation.getPresentableText() : LocalizeValue.of(getValueText());
                }
                return LocalizeValue.ofNullable(getName());
            }

            @Override
            @RequiredReadAction
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
    @RequiredWriteAction
    public PsiElement setName(@Nonnull String newName) throws IncorrectOperationException {
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

    @RequiredReadAction
    private boolean isExplicit() {
        ASTNode child = getNode().getFirstChildNode();
        return child != null && child.getElementType() == YAMLTokenTypes.QUESTION;
    }

    @Override
    public void accept(@Nonnull PsiElementVisitor visitor) {
        if (visitor instanceof YamlPsiElementVisitor yamlPsiElementVisitor) {
            yamlPsiElementVisitor.visitKeyValue(this);
        }
        else {
            super.accept(visitor);
        }
    }

    @Override
    @RequiredReadAction
    public @Nullable TextRange getNameIdentifierRange() {
        PsiElement key = getKey();
        return key == null ? null : key.getTextRangeInParent();
    }
}
