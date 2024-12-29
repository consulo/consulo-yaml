package org.jetbrains.yaml.psi.impl;

import consulo.annotation.access.RequiredReadAction;import consulo.language.ast.ASTNode;
import consulo.language.psi.util.PsiTreeUtil;
import org.jetbrains.yaml.psi.YAMLSequence;
import org.jetbrains.yaml.psi.YAMLSequenceItem;

import jakarta.annotation.Nonnull;
import java.util.List;

public abstract class YAMLSequenceImpl extends YAMLCompoundValueImpl implements YAMLSequence {
    public YAMLSequenceImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Nonnull
    @Override
    public List<YAMLSequenceItem> getItems() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, YAMLSequenceItem.class);
    }

    @Nonnull
    @Override
    @RequiredReadAction
    public String getTextValue() {
        return "<sequence:" + Integer.toHexString(getText().hashCode()) + ">";
    }

    @Override
    public String toString() {
        return "YAML sequence";
    }
}
