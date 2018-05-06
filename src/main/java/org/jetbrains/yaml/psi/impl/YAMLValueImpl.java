package org.jetbrains.yaml.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLValue;

abstract class YAMLValueImpl extends YAMLPsiElementImpl implements YAMLValue {
  public YAMLValueImpl(@Nonnull ASTNode node) {
    super(node);
  }

  @Nullable
  @Override
  public PsiElement getTag() {
    final PsiElement firstChild = getFirstChild();
    if (firstChild.getNode().getElementType() == YAMLTokenTypes.TAG) {
      return firstChild;
    }
    else {
      return null;
    }
  }

  @Override
  public String toString() {
    return "YAML value";
  }
}
