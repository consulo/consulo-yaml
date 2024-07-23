package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.util.PsiTreeUtil;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLSequenceItem;
import org.jetbrains.yaml.psi.YAMLValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * @author oleg
 */
public class YAMLSequenceItemImpl extends YAMLPsiElementImpl implements YAMLSequenceItem {
    public YAMLSequenceItemImpl(@Nonnull final ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    public YAMLValue getValue() {
        return PsiTreeUtil.findChildOfType(this, YAMLValue.class);
    }

    @Nonnull
    public Collection<YAMLKeyValue> getKeysValues() {
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
}