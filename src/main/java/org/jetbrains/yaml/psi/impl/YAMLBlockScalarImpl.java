package org.jetbrains.yaml.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.logging.Logger;
import consulo.util.collection.ContainerUtil;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.YAMLUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class YAMLBlockScalarImpl extends YAMLScalarImpl {
    protected static final int DEFAULT_CONTENT_INDENT = 2;

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
    @RequiredReadAction
    public List<TextRange> getContentRanges() {
        final int myStart = getTextOffset();
        final ASTNode node = getNode();
        final List<TextRange> result = new ArrayList<>();

        final int indent = locateIndent();

        final ASTNode firstEol = node.findChildByType(YAMLTokenTypes.EOL);
        if (firstEol == null) {
            return Collections.emptyList();
        }

        int thisLineStart = firstEol.getStartOffset() + 1;
        for (ASTNode child = firstEol.getTreeNext(); child != null; child = child.getTreeNext()) {
            final IElementType childType = child.getElementType();
            final TextRange childRange = child.getTextRange();

            if (childType == YAMLTokenTypes.INDENT && isEol(child.getTreePrev())) {
                thisLineStart = child.getStartOffset() + Math.min(indent, child.getTextLength());
            }
            else if (childType == YAMLTokenTypes.EOL) {
                if (thisLineStart != -1) {
                    result.add(TextRange.create(thisLineStart, child.getStartOffset()).shiftRight(-myStart));
                }
                thisLineStart = child.getStartOffset() + 1;
            }
            else {
                if (isEol(child.getTreeNext())) {
                    if (thisLineStart == -1) {
                        Logger.getInstance(YAMLBlockScalarImpl.class).warn("thisLineStart == -1: '" + getText() + "'", new Throwable());
                        continue;
                    }
                    result.add(TextRange.create(thisLineStart, childRange.getEndOffset()).shiftRight(-myStart));
                    thisLineStart = -1;
                }
            }
        }
        if (thisLineStart != -1 && thisLineStart != getTextRange().getEndOffset()) {
            result.add(TextRange.create(thisLineStart, getTextRange().getEndOffset()).shiftRight(-myStart));
        }

        final int lastNonEmpty = ContainerUtil.lastIndexOf(result, range -> range.getLength() != 0);

        return lastNonEmpty == -1 ? result : result.subList(0, lastNonEmpty + 1);
    }

    @RequiredReadAction
    protected int locateIndent() {
        int number = 0;
        for (ASTNode child = getNode().getFirstChildNode(); child != null; child = child.getTreeNext()) {
            if (child.getElementType() == getContentType()) {
                number++;
                if (number == 2) {
                    return YAMLUtil.getIndentInThisLine(child.getPsi());
                }
            }
        }
        return 0;
    }

    private static boolean isEol(@Nullable ASTNode node) {
        return node != null && node.getElementType() == YAMLTokenTypes.EOL;
    }
}
