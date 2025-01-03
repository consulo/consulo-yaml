package org.jetbrains.yaml.lexer;

import consulo.language.ast.TokenSet;
import consulo.language.lexer.MergingLexerAdapter;
import org.jetbrains.yaml.YAMLTokenTypes;

import jakarta.annotation.Nonnull;

/**
 * @author oleg
 */
public class YAMLFlexLexer extends MergingLexerAdapter {
    private static final TokenSet TOKENS_TO_MERGE = TokenSet.create(YAMLTokenTypes.TEXT);

    private static final int DIRTY_STATE = 239;

    public YAMLFlexLexer() {
        super(new MyFlexAdapter(new _YAMLLexer(null)), TOKENS_TO_MERGE);
    }

    private static class MyFlexAdapter extends FlexAdapter {
        private boolean myStateCleanliness = false;

        public MyFlexAdapter(_YAMLLexer flex) {
            super(flex);
        }

        @Override
        public void start(@Nonnull CharSequence buffer, int startOffset, int endOffset, int initialState) {
            if (initialState != DIRTY_STATE) {
                ((_YAMLLexer)getFlex()).cleanMyState();
            }
            else {
                // That should not occur normally, but some complex lexers (e.g. black and white lexer)
                // require "suspending" of the lexer to pass some template language. In these cases we
                // believe that the same instance of the lexer would be restored (with its internal state)
                initialState = 0;
            }

            super.start(buffer, startOffset, endOffset, initialState);
        }

        @Override
        public int getState() {
            final int state = super.getState();
            if (state != 0 || myStateCleanliness) {
                return state;
            }
            return DIRTY_STATE;
        }

        @Override
        protected void locateToken() {
            myStateCleanliness = ((_YAMLLexer)getFlex()).isCleanState();
            super.locateToken();
        }
    }
}
