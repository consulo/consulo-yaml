// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.smart;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.codeEditor.HighlighterIterator;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.editor.action.LanguageQuoteHandler;
import jakarta.annotation.Nonnull;
import org.jetbrains.yaml.YAMLElementTypes;
import org.jetbrains.yaml.YAMLLanguage;

@ExtensionImpl
public final class YamlQuoteHandler implements LanguageQuoteHandler {
    @Nonnull
    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }

    private static boolean isQuote(char c) {
        return c == '"' || c == '\'';
    }

    private static boolean isOneQuote(HighlighterIterator iterator) {
        if (!YAMLElementTypes.TEXT_SCALAR_ITEMS.contains((IElementType) iterator.getTokenType())) {
            return false;
        }
        CharSequence chars = iterator.getDocument().getCharsSequence();
        int start = iterator.getStart();
        if (start >= chars.length()) {
            return false;
        }
        char firstChar = chars.charAt(start);
        if (!isQuote(firstChar)) {
            return false;
        }
        int nextIndex = start + 1;
        if (nextIndex >= chars.length()) {
            return true;
        }
        return !isQuote(chars.charAt(nextIndex));
    }

    @Override
    public boolean isClosingQuote(HighlighterIterator iterator, int offset) {
        return isOneQuote(iterator) && iterator.getEnd() == offset;
    }

    @Override
    public boolean isOpeningQuote(HighlighterIterator iterator, int offset) {
        if (!isOneQuote(iterator)) {
            return false;
        }
        int start = iterator.getStart();
        int end = iterator.getEnd();
        return start == offset || (end - start) == 1;
    }

    @Override
    public boolean hasNonClosedLiteral(Editor editor, HighlighterIterator iterator, int offset) {
        return isOneQuote(iterator);
    }

    @Override
    public boolean isInsideLiteral(HighlighterIterator iterator) {
        return false;
    }
}
