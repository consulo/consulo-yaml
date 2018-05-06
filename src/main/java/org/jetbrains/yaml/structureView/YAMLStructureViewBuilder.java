package org.jetbrains.yaml.structureView;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

/**
 * @author oleg
 */
public class YAMLStructureViewBuilder extends TreeBasedStructureViewBuilder {
  private final YAMLFile myPsiFile;

  public YAMLStructureViewBuilder(@Nonnull final YAMLFile psiFile) {
    myPsiFile = psiFile;
  }

  @Override
  @Nonnull
  public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
    return new StructureViewModelBase(myPsiFile, editor, new YAMLStructureViewElement(myPsiFile))
      .withSorters(Sorter.ALPHA_SORTER)
      .withSuitableClasses(YAMLFile.class, YAMLDocument.class, YAMLKeyValue.class);
  }
}