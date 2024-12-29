package org.jetbrains.yaml.structureView;

import consulo.codeEditor.Editor;
import consulo.fileEditor.structureView.StructureViewModel;
import consulo.fileEditor.structureView.TreeBasedStructureViewBuilder;
import consulo.fileEditor.structureView.tree.Sorter;
import consulo.language.editor.structureView.StructureViewModelBase;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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