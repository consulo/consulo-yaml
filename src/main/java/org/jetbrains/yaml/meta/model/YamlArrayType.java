// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.yaml.meta.model;

import consulo.language.editor.inspection.ProblemsHolder;
import consulo.yaml.localize.YAMLLocalize;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLSequence;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class YamlArrayType extends YamlMetaType {

    private final @Nonnull YamlMetaType myElementType;

    public YamlArrayType(@Nonnull YamlMetaType elementType) {
        super(elementType.getTypeName() + "[]");
        myElementType = elementType;
    }

    @Override
    public void validateValue(@Nonnull YAMLValue value, @Nonnull ProblemsHolder problemsHolder) {
        if (!(value instanceof YAMLSequence)) {
            problemsHolder.registerProblem(value, YAMLLocalize.yamlunknownvaluesinspectionbaseErrorArrayIsRequired().get());
        }
    }

    public @Nonnull YamlMetaType getElementType() {
        return myElementType;
    }

    @Override
    public @Nullable Field findFeatureByName(@Nonnull String name) {
        return null;
    }

    @Override
    public @Nonnull List<String> computeMissingFields(@Nonnull Set<String> existingFields) {
        return Collections.emptyList();
    }

    @Override
    public @Nonnull List<Field> computeKeyCompletions(@Nullable YAMLMapping existingMapping) {
        return Collections.emptyList();
    }

    @Override
    public void buildInsertionSuffixMarkup(@Nonnull YamlInsertionMarkup markup,
                                           @Nonnull Field.Relation relation,
                                           @Nonnull ForcedCompletionPath.Iteration iteration) {
    }
}
