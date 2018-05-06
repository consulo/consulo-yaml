package org.jetbrains.yaml;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author oleg
 */
public class YAMLPairedBraceMatcher implements PairedBraceMatcher, YAMLTokenTypes {
    private static final BracePair[] PAIRS = new BracePair[]{
            new BracePair(LBRACE, RBRACE, true),
            new BracePair(LBRACKET, RBRACKET, true),
    };

    @Nonnull
    public BracePair[] getPairs() {
        return PAIRS;
    }

    public boolean isPairedBracesAllowedBeforeType(@Nonnull IElementType iElementType, @Nullable IElementType iElementType1) {
        return true;
    }

  public int getCodeConstructStart(final PsiFile file, final int openingBraceOffset) {
    return openingBraceOffset;
  }
}

