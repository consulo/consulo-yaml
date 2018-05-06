package org.jetbrains.yaml.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import javax.annotation.Nonnull;
import org.jetbrains.yaml.psi.YAMLSequence;
import org.jetbrains.yaml.psi.YAMLSequenceItem;

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
  public String getTextValue() {
    return "<sequence:" + Integer.toHexString(getText().hashCode()) + ">";
  }

  @Override
  public String toString() {
    return "YAML sequence";
  }
}
