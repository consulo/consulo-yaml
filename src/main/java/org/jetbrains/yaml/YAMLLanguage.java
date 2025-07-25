package org.jetbrains.yaml;

import consulo.language.Language;

import consulo.localize.LocalizeValue;
import consulo.yaml.localize.YAMLLocalize;
import jakarta.annotation.Nonnull;

/**
 * @author oleg
 */
public class YAMLLanguage extends Language {
    public static final YAMLLanguage INSTANCE = new YAMLLanguage();

    private YAMLLanguage() {
        super("yaml", "application/x-yaml", "text/yaml", "text/x-yaml");
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return YAMLLocalize.yamlLanguageDisplayName();
    }
}
