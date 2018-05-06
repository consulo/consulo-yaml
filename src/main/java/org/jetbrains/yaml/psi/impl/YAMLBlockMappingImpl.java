package org.jetbrains.yaml.psi.impl;

import javax.annotation.Nonnull;

import com.intellij.lang.ASTNode;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLKeyValue;

public class YAMLBlockMappingImpl extends YAMLMappingImpl {
  public YAMLBlockMappingImpl(@Nonnull ASTNode node) {
    super(node);
  }

  @Override
  protected void addNewKey(@Nonnull YAMLKeyValue key) {
    final int indent = YAMLUtil.getIndentToThisElement(this);

    final YAMLElementGenerator generator = YAMLElementGenerator.getInstance(getProject());
    add(generator.createEol());
    if (indent > 0) {
      add(generator.createIndent(indent));
    }
    add(key);
  }
}
