package org.jetbrains.yaml;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Commenter;
import consulo.language.Language;

import jakarta.annotation.Nonnull;

/**
 * @author Roman Chernyatchik
 */
@ExtensionImpl
public class YAMLCommenter implements Commenter {
    private static final String LINE_COMMENT_PREFIX = "#";

    @Override
    public String getLineCommentPrefix() {
        return LINE_COMMENT_PREFIX;
    }

    @Override
    public String getBlockCommentPrefix() {
        // N/A
        return null;
    }

    @Override
    public String getBlockCommentSuffix() {
        // N/A
        return null;
    }

    @Override
    public String getCommentedBlockCommentPrefix() {
        return null;
    }

    @Override
    public String getCommentedBlockCommentSuffix() {
        return null;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }
}
