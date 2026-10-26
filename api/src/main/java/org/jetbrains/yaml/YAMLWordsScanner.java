package org.jetbrains.yaml;

import consulo.language.ast.TokenSet;
import consulo.language.cacheBuilder.DefaultWordsScanner;
import org.jetbrains.yaml.lexer.YAMLFlexLexer;

/**
 * @author shalupov
 */
public class YAMLWordsScanner extends DefaultWordsScanner {
    public YAMLWordsScanner() {
        super(
            new YAMLFlexLexer(),
            TokenSet.create(YAMLTokenTypes.SCALAR_KEY),
            TokenSet.create(YAMLTokenTypes.COMMENT),
            YAMLElementTypes.SCALAR_VALUES);
        setMayHaveFileRefsInLiterals(true);
    }
}
