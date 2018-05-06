package org.jetbrains.yaml.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import javax.annotation.Nonnull;
import org.jetbrains.yaml.psi.YAMLCompoundValue;
import org.jetbrains.yaml.psi.YAMLScalar;

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