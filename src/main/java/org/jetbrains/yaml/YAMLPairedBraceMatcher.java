package org.jetbrains.yaml;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.BracePair;
import consulo.language.Language;
import consulo.language.PairedBraceMatcher;

import javax.annotation.Nonnull;

/**
 * @author oleg
 */
@ExtensionImpl
public class YAMLPairedBraceMatcher implements PairedBraceMatcher, YAMLTokenTypes {
    private static final BracePair[] PAIRS = new BracePair[]{
            new BracePair(LBRACE, RBRACE, true),
            new BracePair(LBRACKET, RBRACKET, true),
    };

    @Nonnull
    public BracePair[] getPairs() {
        return PAIRS;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }
}

