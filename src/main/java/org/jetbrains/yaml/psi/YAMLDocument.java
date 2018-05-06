package org.jetbrains.yaml.psi;

import javax.annotation.Nullable;

/**
 * @author oleg
 */
public interface YAMLDocument extends YAMLPsiElement {
  @Nullable
  YAMLValue getTopLevelValue();
}
