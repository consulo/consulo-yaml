package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import javax.annotation.Nonnull;

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
