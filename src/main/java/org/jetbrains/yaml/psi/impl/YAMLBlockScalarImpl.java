package org.jetbrains.yaml.psi.impl;

import consulo.application.util.CachedValue;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiModificationTracker;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.SmartList;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.YAMLElementTypes;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.YAMLUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class YAMLBlockScalarImpl extends YAMLScalarImpl {
    public static final int DEFAULT_CONTENT_INDENT = 2;
    private static final int IMPLICIT_INDENT = -1;

    public YAMLBlockScalarImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Nonnull
    protected abstract IElementType getContentType();

    @Override
    public boolean isMultiline() {
        return true;
    }

    @Nonnull
    @Override
    public List<TextRange> getContentRanges() {
        CachedValue<List<TextRange>> cachedValue = CachedValuesManager.getManager(getProject()).createCachedValue(() -> {
            int myStart = getTextRange().getStartOffset();
            int indent = locateIndent();

            List<List<ASTNode>> linesNodes = getLinesNodes();
            List<TextRange> contentRanges = new SmartList<>();

            for (List<ASTNode> line : linesNodes) {
                if (line.isEmpty()) continue;

                ASTNode first = line.get(0);
                int indentAdjustment = 0;
                if (first.getElementType() == YAMLTokenTypes.INDENT) {
                    indentAdjustment = Math.min(first.getTextLength(), indent);
                }
                int start = first.getTextRange().getStartOffset() - myStart + indentAdjustment;
                int end = line.get(line.size() - 1).getTextRange().getEndOffset() - myStart;

                if (start <= end) {
                    TextRange range = TextRange.create(start, end);
                    // Merge adjacent ranges
                    if (!contentRanges.isEmpty() && contentRanges.get(contentRanges.size() - 1).getEndOffset() == range.getStartOffset()) {
                        TextRange last = contentRanges.get(contentRanges.size() - 1);
                        contentRanges.set(contentRanges.size() - 1, TextRange.create(last.getStartOffset(), range.getEndOffset()));
                    } else {
                        contentRanges.add(range);
                    }
                }
            }

            List<TextRange> result;
            if (!includeFirstLineInContent() && contentRanges.size() == 1) {
                TextRange single = contentRanges.get(0);
                result = Collections.singletonList(TextRange.create(single.getEndOffset(), single.getEndOffset()));
            } else if (contentRanges.isEmpty()) {
                result = Collections.emptyList();
            } else if (includeFirstLineInContent()) {
                result = contentRanges;
            } else {
                result = contentRanges.size() > 1 ? contentRanges.subList(1, contentRanges.size()) : Collections.emptyList();
            }

            return CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT);
        });
        return cachedValue.getValue();
    }

    protected boolean includeFirstLineInContent() {
        return false;
    }

    public boolean hasExplicitIndent() {
        return getExplicitIndent() != IMPLICIT_INDENT;
    }

    /**
     * @return Nth child of this scalar block item type ({@link YAMLElementTypes#BLOCK_SCALAR_ITEMS}).
     * Child with number 0 is a header. Content children have numbers more than 0.
     */
    @Nullable
    public ASTNode getNthContentTypeChild(int nth) {
        int number = 0;
        ASTNode child = getNode().getFirstChildNode();
        while (child != null) {
            if (child.getElementType() == getContentType()) {
                if (number == nth) {
                    return child;
                }
                number++;
            }
            child = child.getTreeNext();
        }
        return null;
    }

    /**
     * See <a href="http://www.yaml.org/spec/1.2/spec.html#id2793979">8.1.1.1. Block Indentation Indicator</a>
     */
    public int locateIndent() {
        int indent = getExplicitIndent();
        if (indent != IMPLICIT_INDENT) {
            return indent;
        }
        ASTNode firstLine = getNthContentTypeChild(includeFirstLineInContent() ? 0 : 1);
        if (firstLine != null) {
            return YAMLUtil.getIndentInThisLine(firstLine.getPsi());
        } else {
            List<List<ASTNode>> linesNodes = getLinesNodes();
            if (linesNodes.size() > 1) {
                List<ASTNode> line = linesNodes.get(1);
                ASTNode lineIndentElement = ContainerUtil.find(line, l -> l.getElementType() == YAMLTokenTypes.INDENT);
                if (lineIndentElement != null) {
                    return lineIndentElement.getTextLength();
                }
            }
        }
        return 0;
    }

    @Nonnull
    public String getIndentString() {
        return StringUtil.repeatSymbol(' ', locateIndent());
    }

    @Nonnull
    @Override
    protected List<Pair<TextRange, String>> getEncodeReplacements(@Nonnull CharSequence input) throws IllegalArgumentException {
        String indentString = getIndentString();
        List<Pair<TextRange, String>> result = new ArrayList<>();

        List<TextRange> lineRanges = splitLineRanges(input);
        for (int i = 0; i < lineRanges.size() - 1; i++) {
            TextRange current = lineRanges.get(i);
            TextRange next = lineRanges.get(i + 1);
            result.add(Pair.create(TextRange.create(current.getEndOffset(), next.getStartOffset()), indentString));
        }
        return result;
    }

    @Nonnull
    private static List<TextRange> splitLineRanges(@Nonnull CharSequence input) {
        List<TextRange> result = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '\n') {
                result.add(TextRange.create(start, i + 1));
                start = i + 1;
            }
        }
        if (start < input.length()) {
            result.add(TextRange.create(start, input.length()));
        }
        return result;
    }

    @Nonnull
    protected List<List<ASTNode>> getLinesNodes() {
        List<List<ASTNode>> result = new SmartList<>();
        List<ASTNode> currentLine = new SmartList<>();
        ASTNode child = getFirstContentNode();
        while (child != null) {
            currentLine.add(child);
            if (isEol(child)) {
                result.add(currentLine);
                currentLine = new SmartList<>();
            }
            child = child.getTreeNext();
        }
        if (!currentLine.isEmpty()) {
            result.add(currentLine);
        }
        return result;
    }

    // YAML 1.2 standard does not allow more than 1 symbol in indentation number
    private int getExplicitIndent() {
        if (includeFirstLineInContent()) return IMPLICIT_INDENT;
        ASTNode headerNode = getNthContentTypeChild(0);
        if (headerNode == null) return IMPLICIT_INDENT;
        String header = headerNode.getText();
        for (int i = 0; i < header.length(); i++) {
            if (Character.isDigit(header.charAt(i))) {
                int k = i + 1;
                // YAML 1.2 standard does not allow more than 1 symbol in indentation number
                if (k < header.length() && Character.isDigit(header.charAt(k))) {
                    return IMPLICIT_INDENT;
                }
                int res = Integer.parseInt(header.substring(i, k));
                if (res == 0) {
                    // zero is not allowed as c-indentation-indicator
                    return IMPLICIT_INDENT;
                }
                return res;
            }
        }
        return IMPLICIT_INDENT;
    }

    public static boolean isEol(@Nullable ASTNode node) {
        if (node == null) {
            return false;
        }
        return YAMLElementTypes.EOL_ELEMENTS.contains(node.getElementType());
    }
}
