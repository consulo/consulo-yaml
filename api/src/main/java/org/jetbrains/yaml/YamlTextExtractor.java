// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.yaml;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.spellcheker.text.TextContent;
import consulo.language.spellcheker.text.TextContent.TextDomain;
import consulo.language.spellcheker.text.TextContentBuilder;
import consulo.language.spellcheker.text.TextExtractor;
import consulo.language.spellcheker.text.util.EscapeUtils;
import consulo.language.spellcheker.text.util.PsiUtils;
import consulo.util.collection.ContainerUtil;
import org.jetbrains.yaml.psi.YAMLQuotedText;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.impl.YAMLAnchorImpl;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static org.jetbrains.yaml.YAMLTokenTypes.COMMENT;
import static org.jetbrains.yaml.YAMLTokenTypes.EOL;
import static org.jetbrains.yaml.YAMLTokenTypes.INDENT;
import static org.jetbrains.yaml.YAMLTokenTypes.SCALAR_KEY;
import static org.jetbrains.yaml.YAMLTokenTypes.SCALAR_LIST;
import static org.jetbrains.yaml.YAMLTokenTypes.SCALAR_TEXT;
import static org.jetbrains.yaml.YAMLTokenTypes.WHITESPACE;

@ExtensionImpl
public class YamlTextExtractor extends TextExtractor {
    private final TextContentBuilder myCommentBuilder = TextContentBuilder.FromPsi.removingIndents(" \t#");

    @Override
    protected @Nullable TextContent buildTextContent(PsiElement root, Set<TextDomain> allowedDomains) {
        if (allowedDomains.contains(TextDomain.COMMENTS) && root instanceof PsiComment) {
            List<PsiElement> siblings = PsiUtils.getNotSoDistantSimilarSiblings(
                root,
                TokenSet.create(WHITESPACE, INDENT, EOL),
                it -> PsiUtilCore.getElementType(it) == COMMENT
            );
            return TextContent.joinWithWhitespace(
                '\n',
                ContainerUtil.mapNotNull(siblings, it -> myCommentBuilder.build(it, TextDomain.COMMENTS))
            );
        }
        if (allowedDomains.contains(TextDomain.LITERALS)
            && (root instanceof YAMLScalar || (root.getNode() != null && root.getNode().getElementType() == SCALAR_KEY))) {
            if (YAMLSchemaNameMatcher.matchesNameFromSchema(root)) {
                return null;
            }
            TextContent content = TextContentBuilder.FromPsi.excluding(this::isStealth).build(root, TextDomain.LITERALS);
            if (content == null) {
                return null;
            }
            return root instanceof YAMLQuotedText quotedText && !quotedText.isSingleQuote()
                ? EscapeUtils.replaceBackslashEscapes(content)
                : content;
        }
        if (allowedDomains.contains(TextDomain.LITERALS) && (root instanceof YAMLAnchorImpl && !(root.getParent() instanceof YAMLScalar))) {
            return TextContentBuilder.FromPsi.excluding(this::isStealth).build(root, TextDomain.LITERALS);
        }
        return null;
    }

    private boolean isStealth(PsiElement element) {
        IElementType elementType = element.getNode().getElementType();
        if (elementType == INDENT) {
            return true;
        }
        if (elementType == SCALAR_LIST) {
            return element.getTextLength() == 1 && element.textContains('|');
        }
        if (elementType == SCALAR_TEXT) {
            return element.getTextLength() == 1 && element.textContains('>');
        }
        return false;
    }

    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }
}
