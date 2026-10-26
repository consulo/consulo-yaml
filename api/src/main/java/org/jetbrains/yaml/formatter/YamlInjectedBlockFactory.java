// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.yaml.formatter;

import consulo.document.DocumentWindow;
import consulo.document.util.TextRange;
import consulo.document.util.TextRangeUtil;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.codeStyle.Formatter;
import consulo.language.codeStyle.*;
import consulo.language.codeStyle.inject.DefaultInjectedLanguageBlockBuilder;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.OuterLanguageElement;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.util.collection.SmartList;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.YAMLFileType;
import org.jetbrains.yaml.YAMLLanguage;

import java.util.*;
import java.util.stream.Collectors;

public final class YamlInjectedBlockFactory {

    public static List<Block> substituteInjectedBlocks(CodeStyleSettings settings,
                                                       List<Block> rawSubBlocks,
                                                       ASTNode injectionHost,
                                                       Wrap wrap,
                                                       Alignment alignment) {
        SmartList<Block> injectedBlocks = new SmartList<>();
        List<Block> outerBlocks = rawSubBlocks.stream()
            .filter(block -> block instanceof ASTBlock && ((ASTBlock) block).getNode() instanceof OuterLanguageElement)
            .collect(Collectors.toList());
        Indent fixedIndent = Formatter.getInstance().getIndent(Indent.Type.SPACES, false, settings.getIndentSize(YAMLFileType.YML), false, false);
        YamlInjectedLanguageBlockBuilder builder = new YamlInjectedLanguageBlockBuilder(settings, outerBlocks);
        builder.addInjectedBlocks(injectedBlocks, injectionHost, wrap, alignment, fixedIndent);

        if (injectedBlocks.isEmpty()) {
            return rawSubBlocks;
        }

        int firstStart = injectedBlocks.get(0).getTextRange().getStartOffset();
        int lastEnd = injectedBlocks.get(injectedBlocks.size() - 1).getTextRange().getEndOffset();

        List<Block> beforeBlocks = new ArrayList<>();
        for (Block block : rawSubBlocks) {
            if (block.getTextRange().getEndOffset() <= firstStart) {
                beforeBlocks.add(block);
            }
        }
        injectedBlocks.addAll(0, beforeBlocks);

        for (Block block : rawSubBlocks) {
            if (block.getTextRange().getStartOffset() >= lastEnd) {
                injectedBlocks.add(block);
            }
        }

        return injectedBlocks;
    }

    private static class YamlInjectedLanguageBlockBuilder extends DefaultInjectedLanguageBlockBuilder {
        private final List<Block> outerBlocks;
        private ASTNode injectionHost;
        private PsiFile injectedFile;
        private Language injectionLanguage;

        YamlInjectedLanguageBlockBuilder(CodeStyleSettings settings, List<Block> outerBlocks) {
            super(settings);
            this.outerBlocks = outerBlocks;
        }

        @Override
        public boolean supportsMultipleFragments() {
            return true;
        }

        @Override
        public boolean addInjectedBlocks(List<? super Block> result,
                                         ASTNode injectionHost,
                                         Wrap wrap,
                                         Alignment alignment,
                                         Indent indent) {
            this.injectionHost = injectionHost;
            return super.addInjectedBlocks(result, injectionHost, wrap, alignment, indent);
        }

        @Override
        protected void addInjectedLanguageBlocks(List<? super Block> result, PsiFile injectedFile, Indent indent, int offset, TextRange injectedEditableRange, List<? extends PsiLanguageInjectionHost.Shred> shreds) {
            this.injectedFile = injectedFile;
            super.addInjectedLanguageBlocks(result, injectedFile, indent, offset, injectedEditableRange, shreds);
        }

        @Override
        public Block createBlockBeforeInjection(ASTNode node, Wrap wrap, Alignment alignment, Indent indent, TextRange range) {
            return super.createBlockBeforeInjection(node, wrap, alignment, indent, removeIndentFromRange(range));
        }

        private TextRange removeIndentFromRange(TextRange range) {
            TextRange shifted = range.shiftLeft(injectionHost.getStartOffset());
            String text = injectionHost.getText();
            String substring = text.substring(shifted.getStartOffset(), shifted.getEndOffset());
            return trimBlank(range, substring);
        }

        private TextRange injectedToHost(TextRange textRange) {
            return InjectedLanguageManager.getInstance(injectedFile.getProject()).injectedToHost(injectedFile, textRange);
        }

        @Nullable
        private TextRange hostToInjected(TextRange textRange) {
            var document = PsiDocumentManager.getInstance(injectedFile.getProject()).getCachedDocument(injectedFile);
            if (!(document instanceof DocumentWindow documentWindow)) {
                return null;
            }
            return new TextRange(documentWindow.hostToInjected(textRange.getStartOffset()),
                documentWindow.hostToInjected(textRange.getEndOffset()));
        }

        @Override
        public Block createInjectedBlock(ASTNode node,
                                         Block originalBlock,
                                         Indent indent,
                                         int offset,
                                         TextRange range,
                                         Language language) {
            this.injectionLanguage = language;
            String text = node.getText();
            String substring = text.substring(range.getStartOffset(), Math.min(range.getEndOffset(), text.length()));
            TextRange trimmedRange = trimBlank(range, substring);
            return new YamlInjectedLanguageBlockWrapper(originalBlock, injectedToHost(trimmedRange), trimmedRange, outerBlocks, indent, YAMLLanguage.INSTANCE);
        }

        private TextRange trimBlank(TextRange range, String substring) {
            int preWS = 0;
            for (int i = 0; i < substring.length(); i++) {
                if (Character.isWhitespace(substring.charAt(i))) {
                    preWS++;
                }
                else {
                    break;
                }
            }
            int postWS = 0;
            for (int i = substring.length() - 1; i >= 0; i--) {
                if (Character.isWhitespace(substring.charAt(i))) {
                    postWS++;
                }
                else {
                    break;
                }
            }
            if (preWS < range.getLength()) {
                return new TextRange(range.getStartOffset() + preWS, range.getEndOffset() - postWS);
            }
            return range;
        }

        private class YamlInjectedLanguageBlockWrapper implements BlockEx {
            private final Block original;
            private final TextRange rangeInHost;
            private final TextRange myRange;
            private final Collection<Block> outerBlocksCollection;
            private final Indent indent;
            private final Language language;
            private List<Block> myBlocks;

            YamlInjectedLanguageBlockWrapper(Block original,
                                             TextRange rangeInHost,
                                             TextRange myRange,
                                             Collection<Block> outerBlocks,
                                             Indent indent,
                                             Language language) {
                this.original = original;
                this.rangeInHost = rangeInHost;
                this.myRange = myRange;
                this.outerBlocksCollection = outerBlocks;
                this.indent = indent;
                this.language = language;
            }

            @Override
            public String toString() {
                String text = injectionHost.getPsi().getContainingFile().getText();
                String rangeText = text.substring(getTextRange().getStartOffset(),
                    Math.min(getTextRange().getEndOffset(), text.length()));
                String escaped = rangeText.replace("\n", "\\n");
                return "YamlInjectedLanguageBlockWrapper(" + original + ", " + myRange +
                    ", rangeInRoot = " + getTextRange() + " '" + escaped + "')";
            }

            @Nonnull
            @Override
            public TextRange getTextRange() {
                List<Block> blocks = getSubBlocks();
                if (blocks.isEmpty()) {
                    return rangeInHost;
                }
                int start = Math.min(blocks.get(0).getTextRange().getStartOffset(), rangeInHost.getStartOffset());
                int end = Math.max(blocks.get(blocks.size() - 1).getTextRange().getEndOffset(), rangeInHost.getEndOffset());
                return TextRange.create(start, end);
            }

            @Nonnull
            @Override
            public List<Block> getSubBlocks() {
                if (myBlocks == null) {
                    myBlocks = computeSubBlocks();
                }
                return myBlocks;
            }

            private List<Block> computeSubBlocks() {
                SmartList<Block> result = new SmartList<>();
                ArrayDeque<Block> outerBlocksQueue = new ArrayDeque<>(outerBlocksCollection);

                for (Block block : original.getSubBlocks()) {
                    TextRange intersection = myRange.intersection(block.getTextRange());
                    if (intersection != null) {
                        TextRange blockRange = intersection;
                        TextRange blockRangeInHost = injectedToHost(blockRange);

                        // Add outer blocks that come before this block
                        result.addAll(popWhile(outerBlocksQueue, b -> b.getTextRange().getEndOffset() <= blockRangeInHost.getStartOffset()));

                        if (!block.getSubBlocks().isEmpty()) {
                            List<Block> containedOuter = popWhile(outerBlocksQueue, b -> blockRangeInHost.contains(b.getTextRange()));
                            result.add(createInnerWrapper(block, blockRangeInHost, blockRange, containedOuter));
                        }
                        else {
                            List<Block> outer = popWhile(outerBlocksQueue, b -> blockRangeInHost.contains(b.getTextRange()));
                            List<TextRange> outerRanges = outer.stream().map(Block::getTextRange).collect(Collectors.toList());
                            Iterable<TextRange> remainingInjectedRanges = TextRangeUtil.excludeRanges(blockRangeInHost, outerRanges);

                            List<Block> splitInjectedLeaves = new ArrayList<>();
                            for (TextRange part : remainingInjectedRanges) {
                                TextRange hostToInjectedRange = hostToInjected(part);
                                TextRange rangeToUse = hostToInjectedRange != null ? hostToInjectedRange : blockRange;
                                splitInjectedLeaves.add(createInnerWrapper(block, part, rangeToUse, Collections.emptyList()));
                            }

                            List<Block> combined = new ArrayList<>(splitInjectedLeaves);
                            combined.addAll(outer);
                            combined.sort(Comparator.comparingInt(b -> b.getTextRange().getStartOffset()));
                            result.addAll(combined);
                        }
                    }
                }
                result.addAll(outerBlocksQueue);
                return result;
            }

            private YamlInjectedLanguageBlockWrapper createInnerWrapper(Block block,
                                                                        TextRange blockRangeInHost,
                                                                        TextRange blockRange,
                                                                        Collection<Block> outerNodes) {
                Indent blockIndent = replaceAbsoluteIndent(block);
                Language blockLanguage = block instanceof BlockEx ? ((BlockEx) block).getLanguage() : injectionLanguage;
                return new YamlInjectedLanguageBlockWrapper(block, blockRangeInHost, blockRange, outerNodes, blockIndent, blockLanguage);
            }

            @Nullable
            private Indent replaceAbsoluteIndent(Block block) {
                Indent blockIndent = block.getIndent();
                if (blockIndent instanceof Indent indentImpl && indentImpl.isAbsolute()) {
                    return Formatter.getInstance().getIndent(indentImpl.getType(), false, indentImpl.getSpaces(),
                        indentImpl.isRelativeToDirectParent(), indentImpl.isEnforceIndentToChildren());
                }
                return blockIndent;
            }

            @Override
            public Wrap getWrap() {
                return original.getWrap();
            }

            @Override
            public Indent getIndent() {
                return indent;
            }

            @Override
            public Alignment getAlignment() {
                return original.getAlignment();
            }

            @Override
            public Spacing getSpacing(Block child1, @Nonnull Block child2) {
                return original.getSpacing(unwrap(child1), unwrap(child2));
            }

            @Nonnull
            @Override
            public ChildAttributes getChildAttributes(int newChildIndex) {
                return original.getChildAttributes(newChildIndex);
            }

            @Override
            public boolean isIncomplete() {
                return original.isIncomplete();
            }

            @Override
            public boolean isLeaf() {
                return original.isLeaf();
            }

            @Override
            public Language getLanguage() {
                return language;
            }

            private Block unwrap(Block block) {
                if (block instanceof YamlInjectedLanguageBlockWrapper wrapper) {
                    return wrapper.original;
                }
                return block;
            }
        }

        private static <T> List<T> popWhile(ArrayDeque<T> deque, java.util.function.Predicate<T> predicate) {
            if (deque.isEmpty()) {
                return Collections.emptyList();
            }
            SmartList<T> result = new SmartList<>();
            while (!deque.isEmpty() && predicate.test(deque.peekFirst())) {
                result.add(deque.removeFirst());
            }
            return result;
        }
    }
}
