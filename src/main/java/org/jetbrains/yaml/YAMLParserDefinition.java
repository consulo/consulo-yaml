package org.jetbrains.yaml;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.IFileElementType;
import consulo.language.ast.TokenSet;
import consulo.language.file.FileViewProvider;
import consulo.language.lexer.Lexer;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;
import jakarta.annotation.Nonnull;
import org.jetbrains.yaml.lexer.YAMLFlexLexer;
import org.jetbrains.yaml.parser.YAMLParser;
import org.jetbrains.yaml.psi.impl.*;

/**
 * @author oleg
 */
@ExtensionImpl
public class YAMLParserDefinition implements ParserDefinition, YAMLElementTypes {
    private static final TokenSet ourCommentTokens = TokenSet.create(YAMLTokenTypes.COMMENT);
    public static final IFileElementType FILE = new IFileElementType(YAMLLanguage.INSTANCE);

    @Nonnull
    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }

    @Nonnull
    @Override
    public Lexer createLexer(@Nonnull LanguageVersion languageVersion) {
        return new YAMLFlexLexer();
    }

    @Nonnull
    @Override
    public PsiParser createParser(@Nonnull LanguageVersion languageVersion) {
        return new YAMLParser();
    }

    @Nonnull
    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    @Nonnull
    @Override
    public TokenSet getWhitespaceTokens(@Nonnull LanguageVersion languageVersion) {
        return YAMLElementTypes.WHITESPACE_TOKENS;
    }

    @Nonnull
    @Override
    public TokenSet getCommentTokens(@Nonnull LanguageVersion languageVersion) {
        return YAMLElementTypes.YAML_COMMENT_TOKENS;
    }

    @Nonnull
    @Override
    public TokenSet getStringLiteralElements(@Nonnull LanguageVersion languageVersion) {
        return YAMLElementTypes.TEXT_SCALAR_ITEMS;
    }

    @Nonnull
    @Override
    @RequiredReadAction
    public PsiElement createElement(final ASTNode node) {
        final IElementType type = node.getElementType();
        if (type == YAMLElementTypes.DOCUMENT) {
            return new YAMLDocumentImpl(node);
        }
        if (type == YAMLElementTypes.KEY_VALUE_PAIR) {
            return new YAMLKeyValueImpl(node);
        }
        if (type == YAMLElementTypes.COMPOUND_VALUE) {
            return new YAMLCompoundValueImpl(node);
        }
        if (type == YAMLElementTypes.SEQUENCE) {
            return new YAMLBlockSequenceImpl(node);
        }
        if (type == YAMLElementTypes.MAPPING) {
            return new YAMLBlockMappingImpl(node);
        }
        if (type == YAMLElementTypes.SEQUENCE_ITEM) {
            return new YAMLSequenceItemImpl(node);
        }
        if (type == YAMLElementTypes.HASH) {
            return new YAMLHashImpl(node);
        }
        if (type == YAMLElementTypes.ARRAY) {
            return new YAMLArrayImpl(node);
        }
        if (type == YAMLElementTypes.SCALAR_LIST_VALUE) {
            return new YAMLScalarListImpl(node);
        }
        if (type == YAMLElementTypes.SCALAR_TEXT_VALUE) {
            return new YAMLScalarTextImpl(node);
        }
        if (type == YAMLElementTypes.SCALAR_PLAIN_VALUE) {
            return new YAMLPlainTextImpl(node);
        }
        if (type == YAMLElementTypes.SCALAR_QUOTED_STRING) {
            return new YAMLQuotedTextImpl(node);
        }
        if (type == YAMLElementTypes.ANCHOR_NODE) {
            return new YAMLAnchorImpl(node);
        }
        if (type == YAMLElementTypes.ALIAS_NODE) {
            return new YAMLAliasImpl(node);
        }
        return new YAMLPsiElementImpl(node);
    }

    @Nonnull
    @Override
    public PsiFile createFile(@Nonnull final FileViewProvider viewProvider) {
        return new YAMLFileImpl(viewProvider);
    }

    @Override
    @Nonnull
    public SpaceRequirements spaceExistenceTypeBetweenTokens(final ASTNode left, final ASTNode right) {
        return SpaceRequirements.MAY;
    }
}
