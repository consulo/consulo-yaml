package org.jetbrains.yaml;

import consulo.annotation.DeprecationInfo;
import consulo.annotation.internal.MigratedExtensionsTo;
import consulo.yaml.localize.YAMLLocalize;

/**
 * @author VISTALL
 * @since 2026-01-25
 */
@Deprecated
@DeprecationInfo("Use Localize - just for migration this class left. Do not delete")
@MigratedExtensionsTo(YAMLLocalize.class)
public class YAMLBundle {
    public static String message(String key, Object... args) {
        throw new UnsupportedOperationException();
    }
}
