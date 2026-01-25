// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.formatter;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.AbstractWhiteSpaceFormattingStrategy;
import consulo.util.collection.SmartList;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.YAMLTokenTypes;

import java.util.List;

@ExtensionImpl
public final class YamlWhiteSpaceFormattingStrategy extends AbstractWhiteSpaceFormattingStrategy {
    @Override
    public int check(@Nonnull CharSequence text, int start, int end) {
        return start;
    }

    @Nonnull
    @Override
    public CharSequence adjustWhiteSpaceIfNecessary(@Nonnull CharSequence whiteSpaceText,
                                                    @Nonnull CharSequence text,
                                                    int startOffset,
                                                    int endOffset,
                                                    @Nullable CodeStyleSettings codeStyleSettings,
                                                    @Nullable ASTNode nodeAfter) {
        if (nodeAfter != null && YAMLTokenTypes.SEQUENCE_MARKER == nodeAfter.getElementType()) {
            return whiteSpaceText;
        }

        SmartList<Integer> lineBreaksPositions = new SmartList<>();
        for (int i = 0; i < whiteSpaceText.length(); i++) {
            if (whiteSpaceText.charAt(i) == '\n') {
                lineBreaksPositions.add(i);
            }
        }
        lineBreaksPositions.add(whiteSpaceText.length());

        List<CharSequence> split = new SmartList<>();
        for (int i = 0; i < lineBreaksPositions.size() - 1; i++) {
            int from = lineBreaksPositions.get(i);
            int to = lineBreaksPositions.get(i + 1);
            split.add(whiteSpaceText.subSequence(from, to));
        }

        if (split.size() <= 1) {
            return whiteSpaceText;
        }

        boolean hasLengthOne = false;
        for (CharSequence seq : split) {
            if (seq.length() == 1) {
                hasLengthOne = true;
                break;
            }
        }
        if (!hasLengthOne) {
            return whiteSpaceText;
        }

        CharSequence withIndent = null;
        int minLength = Integer.MAX_VALUE;
        for (CharSequence seq : split) {
            if (seq.length() > 1 && seq.length() < minLength) {
                minLength = seq.length();
                withIndent = seq;
            }
        }
        if (withIndent == null) {
            return whiteSpaceText;
        }

        StringBuilder result = new StringBuilder();
        for (CharSequence seq : split) {
            if (seq.length() == 1) {
                result.append(withIndent);
            } else {
                result.append(seq);
            }
        }
        return result.toString();
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }
}
