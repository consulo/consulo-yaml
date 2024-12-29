package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.util.PsiTreeUtil;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLValue;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author oleg
 */
public class YAMLDocumentImpl extends YAMLPsiElementImpl implements YAMLDocument {
    public YAMLDocumentImpl(@Nonnull final ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "YAML document";
    }

    @Nullable
    @Override
    public YAMLValue getTopLevelValue() {
        return PsiTreeUtil.findChildOfType(this, YAMLValue.class);
    }
}
