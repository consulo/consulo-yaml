// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.meta.model;

import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.StringUtil;
import consulo.yaml.localize.YAMLLocalize;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.yaml.psi.YAMLQuotedText;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public class YamlBooleanType extends YamlEnumType {
    public static YamlBooleanType getSharedInstance() {
        return StandardYamlBoolean.SHARED;
    }

    public YamlBooleanType(@NonNls @Nonnull String name) {
        super(name);
    }

    @Override
    protected void validateScalarValue(@Nonnull YAMLScalar scalarValue, @Nonnull ProblemsHolder holder) {
        if (scalarValue instanceof YAMLQuotedText) {
            //TODO: quickfix would be nice here
            holder.registerProblem(scalarValue,
                YAMLLocalize.yamlbooleantypeValidationErrorQuotedValue().get(),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            return;
        }

        super.validateScalarValue(scalarValue, holder);
    }

    @Override
    public boolean isSupportedTag(@Nonnull String tag) {
        return tag.contains("bool");
    }

    private static class StandardYamlBoolean extends YamlBooleanType {
        private static final StandardYamlBoolean SHARED = new StandardYamlBoolean();

        StandardYamlBoolean() {
            super("yaml:boolean");
            setDisplayName("boolean");
            withLiterals("true", "false");
      /*
      Theoretically, YAML spec allows more exotic variants for boolean values, e.g "ON", "off", "No", or even "Y".
      Different consumers of YAML support different subsets of the accepted values, e.g Compose does not accept one-character variants,
      but, contrary to the spec, supports all-caps "YES" or mixed-caps "Off".
      */
            withHiddenLiterals(new LiteralBuilder()
                .withLiteral("true", LiteralBuilder::CAPS, LiteralBuilder::First)
                .withLiteral("false", LiteralBuilder::CAPS, LiteralBuilder::First)
                .withAllCasesOf("on", "off", "yes", "no")
                .toArray());
        }
    }

    public static class LiteralBuilder {
        private final Set<String> myResult = new LinkedHashSet<>();

        public LiteralBuilder withAllCasesOf(@Nonnull String... literals) {
            for (String next : literals) {
                if (next != null) {
                    withLiteral(next, LiteralBuilder::lower, LiteralBuilder::CAPS, LiteralBuilder::First);
                }
            }
            return this;
        }

        public LiteralBuilder withLiteral(@Nonnull String literal, Function<String, String>... capitalizations) {
            if (capitalizations.length == 0) {
                myResult.add(literal);
            }
            else {
                for (Function<String, String> next : capitalizations) {
                    myResult.add(next.apply(literal));
                }
            }
            return this;
        }

        public String[] toArray() {
            return ArrayUtil.toStringArray(myResult);
        }

        protected static @Nonnull String lower(@Nonnull String text) {
            return StringUtil.toLowerCase(text);
        }

        protected static @Nonnull String CAPS(@Nonnull String text) {
            return StringUtil.toUpperCase(text);
        }

        protected static @Nonnull String First(@Nonnull String text) {
            return StringUtil.toTitleCase(text);
        }
    }
}
