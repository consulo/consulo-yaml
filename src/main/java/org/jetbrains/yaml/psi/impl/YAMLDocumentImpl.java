package org.jetbrains.yaml.psi.impl;

import javax.annotation.Nonnull;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;

import javax.annotation.Nullable;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLValue;

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
