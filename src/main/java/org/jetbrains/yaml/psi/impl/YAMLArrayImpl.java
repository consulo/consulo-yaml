package org.jetbrains.yaml.psi.impl;

import javax.annotation.Nonnull;

import com.intellij.lang.ASTNode;
import org.jetbrains.yaml.psi.YAMLSequence;

/**
 * @author oleg
 */
public class YAMLArrayImpl extends YAMLSequenceImpl implements YAMLSequence {
  public YAMLArrayImpl(@Nonnull final ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "YAML array";
  }
}