package org.jetbrains.yaml;

import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileTypeConsumer;
import consulo.virtualFileSystem.fileType.FileTypeFactory;

import javax.annotation.Nonnull;

/**
 * @author oleg
 */
@ExtensionImpl
public class YAMLFileTypeLoader extends FileTypeFactory {
    @Override
    public void createFileTypes(final @Nonnull FileTypeConsumer consumer) {
        consumer.consume(YAMLFileType.YML, YAMLFileType.DEFAULT_EXTENSION + ";yaml");
    }
}