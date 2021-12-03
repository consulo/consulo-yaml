package org.jetbrains.yaml;

import com.intellij.openapi.fileTypes.LanguageFileType;
import consulo.ui.image.Image;
import consulo.yaml.icon.YAMLIconGroup;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;

public class YAMLFileType extends LanguageFileType {
  public static final YAMLFileType YML = new YAMLFileType();
  @NonNls public static final String DEFAULT_EXTENSION = "yml";
  @NonNls private static final String NAME = "YAML";
  @NonNls private static final String DESCRIPTION = YAMLBundle.message("filetype.description.yaml");

  private YAMLFileType() {
    super(YAMLLanguage.INSTANCE);
  }

  @Nonnull
  public String getId() {
    return NAME;
  }

  @Nonnull
  public String getDescription() {
    return DESCRIPTION;
  }

  @Nonnull
  public String getDefaultExtension() {
    return DEFAULT_EXTENSION;
  }

  @Nonnull
  public Image getIcon() {
    return YAMLIconGroup.yaml();
  }
}

