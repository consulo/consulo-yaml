package org.jetbrains.yaml.psi;

import consulo.language.psi.PsiFile;

import java.util.List;

/**
 * @author oleg
 */
public interface YAMLFile extends PsiFile, YAMLPsiElement {
  List<YAMLDocument> getDocuments();
}
