package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
