package org.jetbrains.yaml;

import javax.annotation.Nonnull;

import com.intellij.lang.Language;

/**
 * @author oleg
 */
public class YAMLLanguage extends Language {
  public static final YAMLLanguage INSTANCE = new YAMLLanguage();

  private YAMLLanguage() {
    super("yaml");
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "YAML";
  }
}
