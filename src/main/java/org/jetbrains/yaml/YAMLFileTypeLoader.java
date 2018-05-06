package org.jetbrains.yaml;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import javax.annotation.Nonnull;

/**
 * @author oleg
 */
public class YAMLFileTypeLoader extends FileTypeFactory {
  public void createFileTypes(final @Nonnull FileTypeConsumer consumer) {
    consumer.consume(YAMLFileType.YML, YAMLFileType.DEFAULT_EXTENSION + ";yaml");
  }
}