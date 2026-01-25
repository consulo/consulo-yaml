// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.meta.model;

import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import consulo.yaml.localize.YAMLLocalize;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NonNls;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.psi.YAMLCompoundValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class YamlScalarType extends YamlMetaType {

    /**
     * @deprecated initialise the {@code displayName} explicitly via {@link #YamlScalarType(String, String)}
     */
    @Deprecated(forRemoval = true)
    protected YamlScalarType(@NonNls @Nonnull String typeName) {
        super(typeName);
    }

    protected YamlScalarType(@NonNls @Nonnull String typeName, @NonNls @Nonnull String displayName) {
        super(typeName, displayName);
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
    public @Nonnull Image getIcon() {
        return PlatformIconGroup.nodesProperty();
    }

    @Override
    public void validateValue(@Nonnull YAMLValue value, @Nonnull ProblemsHolder problemsHolder) {
        if (value instanceof YAMLScalar) {
            validateScalarValue((YAMLScalar) value, problemsHolder);
        }
        else if (value instanceof YAMLCompoundValue) {
            problemsHolder.registerProblem(value, YAMLLocalize.yamlscalartypeErrorScalarValue().get(), ProblemHighlightType.ERROR);
        }
    }

    protected void validateScalarValue(@Nonnull YAMLScalar scalarValue, @Nonnull ProblemsHolder holder) {
        //
    }

    @Override
    public void buildInsertionSuffixMarkup(@Nonnull YamlInsertionMarkup markup,
                                           @Nonnull Field.Relation relation,
                                           @Nonnull ForcedCompletionPath.Iteration iteration) {
        switch (relation) {
            case OBJECT_CONTENTS /* weird, but let's ignore and breakthrough to defaults */, SCALAR_VALUE -> {
                markup.append(": ");
                if (iteration.isEndOfPathReached()) {
                    markup.appendCaret();
                }
            }
            case SEQUENCE_ITEM -> {
                markup.append(":");
                markup.doTabbedBlockForSequenceItem(() -> {
                    if (iteration.isEndOfPathReached()) {
                        markup.appendCaret();
                    }
                });
            }
            default -> throw new IllegalStateException("Unknown relation: " + relation); //NON-NLS
        }
    }
}
