/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.yaml.lexer;

import consulo.component.ProcessCanceledException;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenType;
import consulo.language.lexer.FlexLexer;
import consulo.language.lexer.LexerBase;
import consulo.logging.Logger;

import javax.annotation.Nonnull;

/**
 * @author max
 * <p>
 * TODO [VISTALL] this is only for fixing problems with lexing, need drop it
 */
@Deprecated
public class FlexAdapter extends LexerBase {
    private static final Logger LOG = Logger.getInstance(FlexAdapter.class);

    private final FlexLexer myFlex;

    private IElementType myTokenType;
    private CharSequence myText;

    private int myTokenStart;
    private int myTokenEnd;

    private int myBufferEnd;
    private int myState;

    private boolean myFailed;

    public FlexAdapter(@Nonnull FlexLexer flex) {
        myFlex = flex;
    }

    public FlexLexer getFlex() {
        return myFlex;
    }

    @Override
    public void start(@Nonnull final CharSequence buffer, int startOffset, int endOffset, final int initialState) {
        myText = buffer;
        myTokenStart = myTokenEnd = startOffset;
        myBufferEnd = endOffset;
        myFlex.reset(myText, startOffset, endOffset, initialState);
        myTokenType = null;
    }

    @Override
    public int getState() {
        locateToken();
        return myState;
    }

    @Override
    public IElementType getTokenType() {
        locateToken();
        return myTokenType;
    }

    @Override
    public int getTokenStart() {
        locateToken();
        return myTokenStart;
    }

    @Override
    public int getTokenEnd() {
        locateToken();
        return myTokenEnd;
    }

    @Override
    public void advance() {
        locateToken();
        myTokenType = null;
    }

    @Nonnull
    @Override
    public CharSequence getBufferSequence() {
        return myText;
    }

    @Override
    public int getBufferEnd() {
        return myBufferEnd;
    }

    protected void locateToken() {
        if (myTokenType != null) {
            return;
        }

        myTokenStart = myTokenEnd;
        if (myFailed) {
            return;
        }

        try {
            myState = myFlex.yystate();
            myTokenType = myFlex.advance();
            myTokenEnd = myFlex.getTokenEnd();
        } catch (ProcessCanceledException e) {
            throw e;
        } catch (Throwable e) {
            myFailed = true;
            myTokenType = TokenType.BAD_CHARACTER;
            myTokenEnd = myBufferEnd;
            LOG.warn(myFlex.getClass().getName(), e);
        }
    }

    @Override
    public String toString() {
        return "FlexAdapter for " + myFlex.getClass().getName();
    }
}
