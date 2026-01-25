package org.jetbrains.yaml.annotator;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.language.Language;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.AnnotatorFactory;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.YAMLLanguage;

/**
 * @author VISTALL
 * @since 2026-01-25
 */
@ExtensionImpl
public class YAMLInvalidBlockChildrenErrorAnnotatorFactory implements AnnotatorFactory, DumbAware {
    @Nullable
    @Override
    public Annotator createAnnotator() {
        return new YAMLInvalidBlockChildrenErrorAnnotator();
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }
}
