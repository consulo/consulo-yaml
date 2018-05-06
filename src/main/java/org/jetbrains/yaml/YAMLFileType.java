package org.jetbrains.yaml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NonNls;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import consulo.ui.image.Image;

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

  @Nullable
  public Image getIcon() {
    return AllIcons.Nodes.DataTables;
  }
}

