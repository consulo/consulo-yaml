package org.jetbrains.yaml.psi.impl;

import javax.annotation.Nonnull;

import com.intellij.lang.ASTNode;

public class YAMLBlockSequenceImpl extends YAMLSequenceImpl {
  public YAMLBlockSequenceImpl(@Nonnull ASTNode node) {
    super(node);
  }
}
