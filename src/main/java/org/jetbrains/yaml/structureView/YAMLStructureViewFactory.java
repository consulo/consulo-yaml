package org.jetbrains.yaml.structureView;

import consulo.annotation.component.ExtensionImpl;
import consulo.fileEditor.structureView.StructureViewBuilder;
import consulo.language.Language;
import consulo.language.editor.structureView.PsiStructureViewFactory;
import consulo.language.psi.PsiFile;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.YAMLFile;

import jakarta.annotation.Nonnull;

/**
 * @author oleg
 */
@ExtensionImpl
public class YAMLStructureViewFactory implements PsiStructureViewFactory {
    @Override
    public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
        return new YAMLStructureViewBuilder((YAMLFile)psiFile);
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }
}
