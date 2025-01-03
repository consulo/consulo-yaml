package org.jetbrains.yaml.psi.impl;

import consulo.annotation.access.RequiredReadAction;import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.lexer.YAMLGrammarCharUtil;
import org.jetbrains.yaml.psi.YAMLScalarText;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oleg
 */
public class YAMLScalarTextImpl extends YAMLBlockScalarImpl implements YAMLScalarText {
    public YAMLScalarTextImpl(@Nonnull final ASTNode node) {
        super(node);
    }

    @Nonnull
    @Override
    protected IElementType getContentType() {
        return YAMLTokenTypes.SCALAR_TEXT;
    }

    @Nonnull
    @Override
    protected String getRangesJoiner(@Nonnull CharSequence text, @Nonnull List<TextRange> contentRanges, int indexBefore) {
        final TextRange leftRange = contentRanges.get(indexBefore);
        final TextRange rightRange = contentRanges.get(indexBefore + 1);
        if (leftRange.isEmpty()) {
            return "\n";
        }
        if (startsWithWhitespace(text, leftRange) || startsWithWhitespace(text, rightRange)) {
            return "\n";
        }
        if (rightRange.isEmpty()) {
            int i = indexBefore + 2;
            // Unfortunately we need to scan to the nearest non-empty line to understand
            // whether we should add a line here
            while (i < contentRanges.size() && contentRanges.get(i).isEmpty()) {
                i++;
            }
            if (i < contentRanges.size() && startsWithWhitespace(text, contentRanges.get(i))) {
                return "\n";
            }
            else {
                return "";
            }
        }
        return " ";
    }

    private static boolean startsWithWhitespace(@Nonnull CharSequence text, @Nonnull TextRange range) {
        if (range.isEmpty()) {
            return false;
        }
        final char c = text.charAt(range.getStartOffset());
        return c == ' ' || c == '\t';
    }

    @Override
    protected List<Pair<TextRange, String>> getEncodeReplacements(@Nonnull CharSequence input) throws IllegalArgumentException {
        if (!StringUtil.endsWithChar(input, '\n')) {
            throw new IllegalArgumentException("Should end with a line break");
        }

        int indent = locateIndent();
        if (indent == 0) {
            indent = YAMLUtil.getIndentToThisElement(this) + DEFAULT_CONTENT_INDENT;
        }
        final String indentString = StringUtil.repeatSymbol(' ', indent);

        final List<Pair<TextRange, String>> result = new ArrayList<>();

        int currentLength = 0;
        boolean currentLineIsIndented = input.length() > 0 && input.charAt(0) == ' ';
        for (int i = 0; i < input.length(); ++i) {
            if (input.charAt(i) == '\n') {
                final String replacement;
                if (i + 1 >= input.length() ||
                    YAMLGrammarCharUtil.isSpaceLike(input.charAt(i + 1)) ||
                    input.charAt(i + 1) == '\n' ||
                    currentLineIsIndented) {
                    replacement = "\n" + indentString;
                }
                else {
                    replacement = "\n\n" + indentString;
                }

                result.add(Pair.create(TextRange.from(i, 1), replacement));
                currentLength = 0;
                currentLineIsIndented = i + 1 < input.length() && input.charAt(i + 1) == ' ';
                continue;
            }

            if (currentLength > MAX_SCALAR_LENGTH_PREDEFINED &&
                input.charAt(i) == ' ' && i + 1 < input.length() && YAMLGrammarCharUtil.isNonSpaceChar(input.charAt(i + 1))) {
                result.add(Pair.create(TextRange.from(i, 1), "\n" + indentString));
                currentLength = 0;
                continue;
            }

            currentLength++;
        }

        return result;
    }

    @Nonnull
    @Override
    @RequiredReadAction
    public String getTextValue() {
        return super.getTextValue() + "\n";
    }

    @Override
    public String toString() {
        return "YAML scalar text";
    }
}