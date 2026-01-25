// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.meta.model;

import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.util.collection.ArrayUtil;
import consulo.yaml.localize.YAMLLocalize;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class YamlEnumType extends YamlScalarType {
    private String[] myLiterals = ArrayUtil.EMPTY_STRING_ARRAY;
    private String[] myHiddenLiterals = ArrayUtil.EMPTY_STRING_ARRAY;
    private String[] myDeprecatedLiterals = ArrayUtil.EMPTY_STRING_ARRAY;


    public YamlEnumType(@Nonnull String typeName) {
        super(typeName);
    }

    public YamlEnumType(@Nonnull String typeName, @Nonnull String displayName) {
        super(typeName, displayName);
    }

    public @Nonnull YamlEnumType withLiterals(String... literals) {
        myLiterals = cloneArray(literals);
        return this;
    }

    /**
     * Specifies subset of the valid values which will be accepted by validation but will NOT be offered in
     * completion lists. These values must not overlap with the visible (see {@link #withLiterals(String...)}),
     * but may overlap with the deprecated (see {@link #withDeprecatedLiterals(String...)})
     */
    public @Nonnull YamlEnumType withHiddenLiterals(String... hiddenLiterals) {
        myHiddenLiterals = hiddenLiterals.clone();
        return this;
    }

    /**
     * Specifies subset of the valid values which will be considered deprecated.
     * They will be available in completion list (struck out) and highlighted after validation.
     * These values must not overlap with the visible (see {@link #withLiterals(String...)}),
     * but may overlap with the hidden (see {@link #withHiddenLiterals(String...)})
     */
    public @Nonnull YamlEnumType withDeprecatedLiterals(String... deprecatedLiterals) {
        myDeprecatedLiterals = deprecatedLiterals.clone();
        return this;
    }

    public boolean isLiteralDeprecated(@Nonnull String literal) {
        return Arrays.asList(myDeprecatedLiterals).contains(literal);
    }

    protected final @Nonnull Stream<String> getLiteralsStream() {
        return Stream.concat(Arrays.stream(myLiterals), Arrays.stream(myDeprecatedLiterals));
    }

    @Override
    protected void validateScalarValue(@Nonnull YAMLScalar scalarValue, @Nonnull ProblemsHolder holder) {
        super.validateScalarValue(scalarValue, holder);

        String text = scalarValue.getTextValue();
        if (text.isEmpty()) {
            // not our business
            return;
        }

        if (Stream.concat(Arrays.stream(myHiddenLiterals), getLiteralsStream()).noneMatch(text::equals)) {
            //TODO quickfix makes sense here if !text.equals(text.toLowerCase)
            holder.registerProblem(scalarValue,
                YAMLLocalize.yamlenumtypeValidationErrorValueUnknown(text).get(),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        }
    }

    @Override
    public @Nonnull List<LookupElement> getValueLookups(@Nonnull YAMLScalar insertedScalar, @Nullable CompletionContext completionContext) {
        return Stream.concat(
                Arrays.stream(myLiterals).map((String literal) -> createValueLookup(literal, false)),
                Arrays.stream(myDeprecatedLiterals).map((String literal) -> createValueLookup(literal, true))
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    protected @Nullable LookupElement createValueLookup(@Nonnull String literal, boolean deprecated) {
        return LookupElementBuilder.create(literal).withStrikeoutness(deprecated);
    }

    @Nonnull
    private static String[] cloneArray(@Nonnull String[] array) {
        return array.length == 0 ? ArrayUtil.EMPTY_STRING_ARRAY : array.clone();
    }
}
