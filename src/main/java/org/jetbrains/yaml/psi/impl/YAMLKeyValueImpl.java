package org.jetbrains.yaml.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.language.ast.ASTNode;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.ReferenceProvidersRegistry;
import consulo.language.util.IncorrectOperationException;
import consulo.navigation.ItemPresentation;
import consulo.ui.image.Image;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.StringUtil;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.*;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author oleg
 */
public class YAMLKeyValueImpl extends YAMLPsiElementImpl implements YAMLKeyValue {
    public YAMLKeyValueImpl(@Nonnull final ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "YAML key value";
    }

    @Nullable
    @Override
    @RequiredReadAction
    public PsiElement getKey() {
        final PsiElement result = findChildByType(YAMLTokenTypes.SCALAR_KEY);
        if (result != null) {
            return result;
        }
        if (isExplicit()) {
            return findChildByClass(YAMLCompoundValue.class);
        }
        return null;
    }

    @Nullable
    @Override
    public YAMLMapping getParentMapping() {
        return ObjectUtil.tryCast(super.getParent(), YAMLMapping.class);
    }

    @Nullable
    @Override
    @RequiredReadAction
    public String getName() {
        return getKeyText();
    }

    @Nonnull
    @Override
    @RequiredReadAction
    public String getKeyText() {
        final PsiElement keyElement = getKey();
        if (keyElement == null) {
            return "";
        }

        if (keyElement instanceof YAMLCompoundValue compoundValue) {
            return compoundValue.getTextValue();
        }

        final String text = keyElement.getText();
        return StringUtil.unquoteString(text.substring(0, text.length() - 1));
    }

    @Nullable
    @Override
    @RequiredReadAction
    public YAMLValue getValue() {
        for (PsiElement child = getLastChild(); child != null; child = child.getPrevSibling()) {
            if (child instanceof YAMLValue value) {
                return value;
            }
        }
        return null;
    }

    @Nonnull
    @Override
    @RequiredReadAction
    public String getValueText() {
        final YAMLValue value = getValue();
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

    @RequiredWriteAction
    private void adjustWhitespaceToContentType(boolean isScalar) {
        assert getKey() != null;
        PsiElement key = getKey();

        if (key.getNextSibling() != null && key.getNextSibling().getNode().getElementType() == YAMLTokenTypes.COLON) {
            key = key.getNextSibling();
        }

        while (key.getNextSibling() != null && !(key.getNextSibling() instanceof YAMLValue)) {
            key.getNextSibling().delete();
        }
        final YAMLElementGenerator generator = YAMLElementGenerator.getInstance(getProject());
        if (isScalar) {
            addAfter(generator.createSpace(), key);
        }
        else {
            final int indent = YAMLUtil.getIndentToThisElement(this);
            addAfter(generator.createIndent(indent + 2), key);
            addAfter(generator.createEol(), key);
        }
    }

    @Override
    @RequiredReadAction
    public ItemPresentation getPresentation() {
        final YAMLFile yamlFile = (YAMLFile)getContainingFile();
        final PsiElement value = getValue();
        return new ItemPresentation() {
            @Override
            @RequiredReadAction
            public String getPresentableText() {
                if (value instanceof YAMLScalar) {
                    return getValueText();
                }
                return getName();
            }

            @Override
            @RequiredReadAction
            public String getLocationString() {
                return "[" + yamlFile.getName() + "]";
            }

            @Override
            @RequiredReadAction
            public Image getIcon() {
                return IconDescriptorUpdaters.getIcon(YAMLKeyValueImpl.this, 0);
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
    @Nonnull
    @Override
    public PsiReference[] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }

    private boolean isExplicit() {
        final ASTNode child = getNode().getFirstChildNode();
        return child != null && child.getElementType() == YAMLTokenTypes.QUESTION;
    }
}
