package org.jetbrains.yaml;

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
import org.jetbrains.yaml.lexer.YAMLFlexLexer;
import org.jetbrains.yaml.parser.YAMLParser;
import org.jetbrains.yaml.psi.impl.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author oleg
 */
@ExtensionImpl
public class YAMLParserDefinition implements ParserDefinition, YAMLElementTypes {
  private static final TokenSet ourCommentTokens = TokenSet.create(YAMLTokenTypes.COMMENT);

  @Nonnull
  @Override
  public Language getLanguage() {
    return YAMLLanguage.INSTANCE;
  }

  @Nonnull
  public Lexer createLexer(LanguageVersion languageVersion) {
    return new YAMLFlexLexer();
  }

  @Nullable
  public PsiParser createParser(LanguageVersion languageVersion) {
    return new YAMLParser();
  }

  public IFileElementType getFileNodeType() {
    return FILE;
  }

  @Nonnull
  public TokenSet getWhitespaceTokens(LanguageVersion languageVersion) {
    return TokenSet.create(YAMLTokenTypes.WHITESPACE);
  }

  @Nonnull
  public TokenSet getCommentTokens(LanguageVersion languageVersion) {
    return ourCommentTokens;
  }

  @Nonnull
  public TokenSet getStringLiteralElements(LanguageVersion languageVersion) {
    return TokenSet.create(YAMLTokenTypes.SCALAR_STRING, YAMLTokenTypes.SCALAR_DSTRING, YAMLTokenTypes.TEXT);
  }

  @Nonnull
  public PsiElement createElement(final ASTNode node) {
    final IElementType type = node.getElementType();
    if (type == DOCUMENT){
      return new YAMLDocumentImpl(node);
    }
    if (type == KEY_VALUE_PAIR) {
      return new YAMLKeyValueImpl(node);
    }
    if (type == COMPOUND_VALUE) {
      return new YAMLCompoundValueImpl(node);
    }
    if (type == SEQUENCE) {
      return new YAMLBlockSequenceImpl(node);
    }
    if (type == MAPPING) {
      return new YAMLBlockMappingImpl(node);
    }
    if (type == SEQUENCE_ITEM) {
      return new YAMLSequenceItemImpl(node);
    }
    if (type == HASH) {
      return new YAMLHashImpl(node);
    }
    if (type == ARRAY) {
      return new YAMLArrayImpl(node);
    }
    if (type == SCALAR_LIST_VALUE) {
      return new YAMLScalarListImpl(node);
    }
    if (type == SCALAR_TEXT_VALUE) {
      return new YAMLScalarTextImpl(node);
    }
    if (type == SCALAR_PLAIN_VALUE) {
      return new YAMLPlainTextImpl(node);
    }
    if (type == SCALAR_QUOTED_STRING) {
      return new YAMLQuotedTextImpl(node);
    }
    return new YAMLPsiElementImpl(node);
  }

  public PsiFile createFile(final FileViewProvider viewProvider) {
    return new YAMLFileImpl(viewProvider);
  }

  public SpaceRequirements spaceExistanceTypeBetweenTokens(final ASTNode left, final ASTNode right) {
    return SpaceRequirements.MAY;
  }
}
