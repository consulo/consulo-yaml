package org.jetbrains.yaml;

import consulo.codeEditor.DefaultLanguageHighlighterColors;
import consulo.codeEditor.HighlighterColors;
import consulo.colorScheme.TextAttributesKey;

/**
 * @author oleg
 */
public class YAMLHighlighter {
    // Text default attrs
    public static final TextAttributesKey SCALAR_KEY_DEFAULT_ATTRS = DefaultLanguageHighlighterColors.KEYWORD;
    public static final TextAttributesKey COMMENT_DEFAULT_ATTRS = DefaultLanguageHighlighterColors.DOC_COMMENT;
    public static final TextAttributesKey SCALAR_TEXT_DEFAULT_ATTRS = HighlighterColors.TEXT;
    public static final TextAttributesKey SCALAR_STRING_DEFAULT_ATTRS = DefaultLanguageHighlighterColors.STRING;
    public static final TextAttributesKey SCALAR_DSTRING_DEFAULT_ATTRS = DefaultLanguageHighlighterColors.STRING;
    public static final TextAttributesKey SCALAR_LIST_DEFAULT_ATTRS = HighlighterColors.TEXT;
    public static final TextAttributesKey TEXT_DEFAULT_ATTRS = HighlighterColors.TEXT;
    public static final TextAttributesKey SIGN_DEFAULT_ATTRS = DefaultLanguageHighlighterColors.OPERATION_SIGN;

    // text attributes keys
    public static final TextAttributesKey SCALAR_KEY = TextAttributesKey.of("YAML_SCALAR_KEY", SCALAR_KEY_DEFAULT_ATTRS);
    public static final TextAttributesKey SCALAR_TEXT = TextAttributesKey.of("YAML_SCALAR_VALUE", SCALAR_TEXT_DEFAULT_ATTRS);
    public static final TextAttributesKey SCALAR_STRING =
        TextAttributesKey.of("YAML_SCALAR_STRING", SCALAR_STRING_DEFAULT_ATTRS);
    public static final TextAttributesKey SCALAR_DSTRING =
        TextAttributesKey.of("YAML_SCALAR_DSTRING", SCALAR_DSTRING_DEFAULT_ATTRS);
    public static final TextAttributesKey SCALAR_LIST = TextAttributesKey.of("YAML_SCALAR_LIST", SCALAR_LIST_DEFAULT_ATTRS);
    public static final TextAttributesKey COMMENT = TextAttributesKey.of("YAML_COMMENT", COMMENT_DEFAULT_ATTRS);
    public static final TextAttributesKey TEXT = TextAttributesKey.of("YAML_TEXT", TEXT_DEFAULT_ATTRS);
    public static final TextAttributesKey SIGN = TextAttributesKey.of("YAML_SIGN", SIGN_DEFAULT_ATTRS);

    private YAMLHighlighter() {
    }
}
