// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
/*
 * Copyright 2013-2026 consulo.io
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
package org.jetbrains.yaml.schema;

import com.jetbrains.jsonSchema.extension.adapter.JsonArrayValueAdapter;
import com.jetbrains.jsonSchema.extension.adapter.JsonObjectValueAdapter;
import com.jetbrains.jsonSchema.extension.adapter.JsonValueAdapter;
import consulo.annotation.access.RequiredReadAction;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.StringUtil;
import org.jetbrains.yaml.psi.YAMLAnchor;
import org.jetbrains.yaml.psi.YAMLValue;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.regex.Pattern;

public final class YamlGenericValueAdapter implements JsonValueAdapter {
    private static final Pattern FLOAT_PATTERN = Pattern.compile("[-+]?(\\.[0-9]+|[0-9]+(\\.[0-9]*)?)([eE][-+]?[0-9]+)?");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("[-+]?(?:0[bB][0-1_]+|0[oO][0-7_]+|0[xX][0-9a-fA-F_]+|[0-9][0-9_]*)");

    private static final Set<String> NULLS = Set.of("null", "Null", "NULL", "~");
    private static final Set<String> BOOLS = Set.of("true", "True", "TRUE", "false", "False", "FALSE");
    private static final Set<String> INFS = Set.of(".inf", ".Inf", ".INF");
    private static final Set<String> NANS = Set.of(".nan", ".NaN", ".NAN");
    private final YAMLValue myValue;

    public YamlGenericValueAdapter(YAMLValue value) {
        myValue = value;
    }

    @Override
    public boolean isShouldBeIgnored() {
        return true;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    @RequiredReadAction
    public boolean isStringLiteral() {
        return !isNumberLiteral() && !isBooleanLiteral() && !isNull();
    }

    @RequiredReadAction
    private String getTextWithoutRefs() {
        YAMLAnchor[] anchors = PsiTreeUtil.getChildrenOfType(myValue, YAMLAnchor.class);
        if (anchors == null || anchors.length == 0) {
            return myValue.getText();
        }
        int endOffset = anchors[anchors.length - 1].getTextRange().getEndOffset();
        TextRange valueTextRange = myValue.getTextRange();
        int offset = valueTextRange.getEndOffset();
        TextRange range = new TextRange(endOffset, offset);
        range = range.shiftLeft(valueTextRange.getStartOffset());
        String text = myValue.getText();
        return text.substring(range.getStartOffset()).trim();
    }

    private static boolean hasTag(String text, String tagName) {
        return StringUtil.startsWith(text, "!!" + tagName);
    }

    @Override
    @RequiredReadAction
    public boolean isNumberLiteral() {
        String text = getTextWithoutRefs();
        return isNumber(text);
    }

    @Override
    @RequiredReadAction
    public boolean isBooleanLiteral() {
        String text = getTextWithoutRefs();
        return BOOLS.contains(text) || hasTag(text, "bool");
    }

    @Override
    @RequiredReadAction
    public boolean isNull() {
        String text = getTextWithoutRefs();
        return NULLS.contains(text) || hasTag(text, "null");
    }

    @Override
    public PsiElement getDelegate() {
        return myValue;
    }

    @Override
    public @Nullable JsonObjectValueAdapter getAsObject() {
        return null;
    }

    @Override
    public @Nullable JsonArrayValueAdapter getAsArray() {
        return null;
    }

    @Override
    public boolean shouldCheckIntegralRequirements() {
        return false;
    }

    @Override
    @RequiredReadAction
    public boolean shouldCheckAsValue() {
        return !isNonFinite(getTextWithoutRefs());
    }

    private static boolean isNumber(@Nullable String s) {
        if (s == null) {
            return false;
        }
        return isInteger(s) || isFloat(s);
    }

    // http://yaml.org/spec/1.2/spec.html#id2803828
    private static boolean isInteger(String s) {
        if (s.isEmpty()) {
            return false;
        }
        if ("0".equals(s) || "-0".equals(s) || "+0".equals(s)) {
            return true;
        }
        if (hasTag(s, "int")) {
            return true;
        }
        if (matchesInt(s)) {
            return true;
        }
        return false;
    }

    private static boolean matchesInt(String s) {
        return INTEGER_PATTERN.matcher(s).matches();
    }

    // http://yaml.org/spec/1.2/spec.html#id2804092
    private static boolean isFloat(String s) {
        if (INFS.contains(trimSign(s)) || NANS.contains(trimSign(s))) {
            return true;
        }
        if (hasTag(s, "float")) {
            return true;
        }
        return FLOAT_PATTERN.matcher(s).matches();
    }

    private static boolean isNonFinite(String s) {
        String text = s;
        if (hasTag(text, "float")) {
            int spaceIndex = text.indexOf(' ');
            text = spaceIndex > 0 ? text.substring(spaceIndex + 1).trim() : "";
        }
        String value = trimSign(text);
        return INFS.contains(value) || NANS.contains(value);
    }

    private static String trimSign(String s) {
        if (s.isEmpty()) {
            return s;
        }
        char c = s.charAt(0);
        return c == '+' || c == '-' ? s.substring(1) : s;
    }
}
