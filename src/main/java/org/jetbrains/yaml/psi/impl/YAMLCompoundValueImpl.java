package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import org.jetbrains.yaml.psi.YAMLCompoundValue;
import org.jetbrains.yaml.psi.YAMLScalar;

import javax.annotation.Nonnull;

/**
 * @author oleg
 */
public class YAMLCompoundValueImpl extends YAMLValueImpl implements YAMLCompoundValue {
  public YAMLCompoundValueImpl(@Nonnull final ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "YAML compound value";
  }

  @Nonnull
  @Override
  public String getTextValue() {
    PsiElement element = getTag() != null ? getTag().getNextSibling() : getFirstChild();

    while (element != null && !(element instanceof YAMLScalar)) {
      element = element.getNextSibling();
    }

    if (element != null) {
      return ((YAMLScalar)element).getTextValue();
    }
    else {
      return "<compoundValue:" + Integer.toHexString(getText().hashCode()) + ">";
    }
  }
}