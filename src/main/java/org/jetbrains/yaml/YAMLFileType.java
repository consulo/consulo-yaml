package org.jetbrains.yaml;

import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import consulo.yaml.icon.YAMLIconGroup;
import consulo.yaml.localize.YAMLLocalize;

import javax.annotation.Nonnull;

public class YAMLFileType extends LanguageFileType {
    public static final YAMLFileType YML = new YAMLFileType();
    public static final String DEFAULT_EXTENSION = "yml";

    private YAMLFileType() {
        super(YAMLLanguage.INSTANCE);
    }

    @Nonnull
    @Override
    public String getId() {
        return "YAML";
    }

    @Nonnull
    @Override
    public LocalizeValue getDescription() {
        return YAMLLocalize.filetypeDescriptionYaml();
    }

    @Nonnull
    @Override
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    @Nonnull
    @Override
    public Image getIcon() {
        return YAMLIconGroup.yaml();
    }
}

