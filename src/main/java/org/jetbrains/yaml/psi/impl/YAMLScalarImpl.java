// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.psi.impl;

import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.psi.*;
import consulo.navigation.ItemPresentation;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.YAMLElementTypes;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.lexer.YAMLGrammarCharUtil;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YamlPsiElementVisitor;

import java.util.Collections;
import java.util.List;

public abstract class YAMLScalarImpl extends YAMLValueImpl implements YAMLScalar {
    protected static final int MAX_SCALAR_LENGTH_PREDEFINED = 60;

    public YAMLScalarImpl(@Nonnull ASTNode node) {
        super(node);
    }

    public abstract @Nonnull List<TextRange> getContentRanges();

    public abstract @Nonnull YamlScalarTextEvaluator getTextEvaluator();

    protected List<Pair<TextRange, String>> getDecodeReplacements(@Nonnull CharSequence input) {
        return Collections.emptyList();
    }

    protected List<Pair<TextRange, String>> getEncodeReplacements(@Nonnull CharSequence input) throws IllegalArgumentException {
        return Collections.emptyList();
    }

    @Override
    public final @Nonnull String getTextValue() {
        return getTextEvaluator().getTextValue(null);
    }

    public final @Nonnull String getTextValue(@Nullable TextRange rangeInHost) {
        return getTextEvaluator().getTextValue(rangeInHost);
    }

    @Override
    public PsiReference getReference() {
        final PsiReference[] references = getReferences();
        return references.length == 1 ? references[0] : null;
    }

    @Override
    @Nonnull
    public PsiReference[] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }

    @Override
    public boolean isValidHost() {
        return true;
    }

    @Override
    public PsiLanguageInjectionHost updateText(@Nonnull String text) {
        return ElementManipulators.handleContentChange(this, text);
    }

    @Override
    public @Nonnull LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
        return new MyLiteralTextEscaper(this);
    }

    static @Nonnull String processReplacements(@Nonnull CharSequence input,
                                               @Nonnull List<? extends Pair<TextRange, String>> replacements) throws IndexOutOfBoundsException {
        StringBuilder result = new StringBuilder();
        int currentOffset = 0;
        for (Pair<TextRange, String> replacement : replacements) {
            result.append(input.subSequence(currentOffset, replacement.getFirst().getStartOffset()));
            result.append(replacement.getSecond());
            currentOffset = replacement.getFirst().getEndOffset();
        }
        result.append(input.subSequence(currentOffset, input.length()));
        return result.toString();
    }

    protected static boolean isSurroundedByNoSpace(CharSequence text, int pos) {
        return (pos - 1 < 0 || !YAMLGrammarCharUtil.isSpaceLike(text.charAt(pos - 1)))
            && (pos + 1 >= text.length() || !YAMLGrammarCharUtil.isSpaceLike(text.charAt(pos + 1)));
    }

    @Nullable
    protected final ASTNode getFirstContentNode() {
        ASTNode node = getNode().getFirstChildNode();
        while (node != null && (
            node.getElementType() == YAMLTokenTypes.TAG || YAMLElementTypes.BLANK_ELEMENTS.contains(node.getElementType()))) {
            node = node.getTreeNext();
        }
        return node;
    }

    private static class MyLiteralTextEscaper extends LiteralTextEscaper<YAMLScalarImpl> {
        MyLiteralTextEscaper(YAMLScalarImpl scalar) {
            super(scalar);
        }

        private String text;
        private List<TextRange> contentRanges;

        @Override
        public boolean decode(@Nonnull TextRange rangeInsideHost, @Nonnull StringBuilder outChars) {
            text = myHost.getText();
            contentRanges = myHost.getContentRanges();
            boolean decoded = false;
            for (TextRange range : contentRanges) {
                TextRange intersection = range.intersection(rangeInsideHost);
                if (intersection == null) {
                    continue;
                }
                decoded = true;
                String substring = intersection.substring(text);
                outChars.append(processReplacements(substring, myHost.getDecodeReplacements(substring)));
            }
            return decoded;
        }

        @Override
        public @Nonnull TextRange getRelevantTextRange() {
            if (contentRanges == null) {
                contentRanges = myHost.getContentRanges();
            }
            if (contentRanges.isEmpty()) {
                return TextRange.EMPTY_RANGE;
            }
            return TextRange.create(contentRanges.get(0).getStartOffset(), contentRanges.get(contentRanges.size() - 1).getEndOffset());
        }

        @Override
        public int getOffsetInHost(int offsetInDecoded, @Nonnull TextRange rangeInsideHost) {

            int currentOffsetInDecoded = 0;

            TextRange last = null;
            for (int i = 0; i < contentRanges.size(); i++) {
                final TextRange range = rangeInsideHost.intersection(contentRanges.get(i));
                if (range == null) {
                    continue;
                }
                last = range;

                String curString = range.subSequence(text).toString();

                final List<Pair<TextRange, String>> replacementsForThisLine = myHost.getDecodeReplacements(curString);
                int encodedOffsetInCurrentLine = 0;
                for (Pair<TextRange, String> replacement : replacementsForThisLine) {
                    final int deltaLength = replacement.getFirst().getStartOffset() - encodedOffsetInCurrentLine;
                    int currentOffsetBeforeReplacement = currentOffsetInDecoded + deltaLength;
                    if (currentOffsetBeforeReplacement > offsetInDecoded) {
                        return range.getStartOffset() + encodedOffsetInCurrentLine + (offsetInDecoded - currentOffsetInDecoded);
                    }
                    else if (currentOffsetBeforeReplacement == offsetInDecoded && !replacement.getSecond().isEmpty()) {
                        return range.getStartOffset() + encodedOffsetInCurrentLine + (offsetInDecoded - currentOffsetInDecoded);
                    }
                    currentOffsetInDecoded += deltaLength + replacement.getSecond().length();
                    encodedOffsetInCurrentLine += deltaLength + replacement.getFirst().getLength();
                }

                final int deltaLength = curString.length() - encodedOffsetInCurrentLine;
                if (currentOffsetInDecoded + deltaLength > offsetInDecoded) {
                    return range.getStartOffset() + encodedOffsetInCurrentLine + (offsetInDecoded - currentOffsetInDecoded);
                }
                currentOffsetInDecoded += deltaLength;
            }

            return last != null ? last.getEndOffset() : -1;
        }

        @Override
        public boolean isOneLine() {
            return !myHost.isMultiline();
        }
    }

    @Override
    public void accept(@Nonnull PsiElementVisitor visitor) {
        if (visitor instanceof YamlPsiElementVisitor) {
            ((YamlPsiElementVisitor) visitor).visitScalar(this);
        }
        else {
            super.accept(visitor);
        }
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Override
            public @Nonnull String getPresentableText() {
                return StringUtil.shortenTextWithEllipsis(getTextValue(), 20, 0, true);
            }

            @Override
            public @Nonnull String getLocationString() {
                return getContainingFile().getName();
            }

            @Override
            public @Nonnull Image getIcon() {
                return PlatformIconGroup.nodesVariable();
            }
        };
    }
}
