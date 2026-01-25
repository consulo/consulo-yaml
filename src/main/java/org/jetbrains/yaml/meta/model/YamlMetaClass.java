// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.meta.model;

import consulo.language.editor.inspection.ProblemsHolder;
import consulo.util.collection.ContainerUtil;
import consulo.yaml.localize.YAMLLocalize;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NonNls;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.meta.impl.YamlMetaUtil;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("UnusedReturnValue")
public class YamlMetaClass extends YamlMetaType {
    private final List<Field> myFeatures = new LinkedList<>();
    private final List<Field> myFeaturesRO = Collections.unmodifiableList(myFeatures);

    public YamlMetaClass(@NonNls @Nonnull String typeName) {
        super(typeName);
    }

    protected YamlMetaClass(@Nonnull String typeName, @Nonnull String displayName) {
        super(typeName, displayName);
    }

    @Override
    public @Nullable Field findFeatureByName(@Nonnull String name) {
        if (getFeatures().isEmpty()) {
            return null;
        }

        Optional<Field> byExactName = getFeatures().stream()
            .filter(f -> !f.isByPattern() && name.equals(f.getName()))
            .findAny();

        return byExactName.orElse(
            ContainerUtil.find(getFeatures(), f -> f.isByPattern() && f.acceptsFieldName(name))
        );
    }

    @Override
    public @Nonnull List<String> computeMissingFields(@Nonnull Set<String> existingFields) {
        return myFeatures.stream()
            .filter(Field::isRequired)
            .map(Field::getName)
            .filter(name -> !existingFields.contains(name))
            .collect(Collectors.toList());
    }

    @Override
    public @Nonnull List<Field> computeKeyCompletions(@Nullable YAMLMapping existingMapping) {
        return ContainerUtil.filter(myFeatures, Field::isEditable);
    }

    public @Nonnull List<Field> getFeatures() {
        return myFeaturesRO;
    }

    protected final @Nonnull Field addStringFeature(@Nonnull String name) {
        return addFeature(new Field(name, YamlStringType.getInstance()));
    }

    protected @Nonnull Field addBooleanFeature(@Nonnull String name) {
        return addScalarFeature(name, YamlBooleanType.getSharedInstance());
    }

    protected final @Nonnull Field addScalarFeature(@Nonnull YamlScalarType type) {
        return addScalarFeature(type.getTypeName(), type);
    }

    protected final @Nonnull Field addScalarFeature(@Nonnull String name, @Nonnull YamlScalarType type) {
        return addFeature(new Field(name, type));
    }

    protected final @Nonnull Field addObjectFeature(@Nonnull YamlMetaClass metaClass) {
        return addFeature(new Field(metaClass.getTypeName(), metaClass));
    }

    protected <T extends Field> T addFeature(@Nonnull T child) {
        myFeatures.add(child);
        return child;
    }

    @Override
    public void buildInsertionSuffixMarkup(@Nonnull YamlInsertionMarkup markup,
                                           @Nonnull Field.Relation relation,
                                           @Nonnull ForcedCompletionPath.Iteration iteration) {
        switch (relation) {
            case SCALAR_VALUE ->
                throw new IllegalArgumentException("Default relation " + relation + " requested for complex type: " + this);
            case OBJECT_CONTENTS -> doBuildInsertionSuffixMarkup(markup, false, iteration);
            case SEQUENCE_ITEM -> doBuildInsertionSuffixMarkup(markup, true, iteration);
            default -> throw new IllegalArgumentException("Unknown relation: " + relation);
        }
    }

    @Override
    public void validateValue(@Nonnull YAMLValue value, @Nonnull ProblemsHolder problemsHolder) {
        super.validateValue(value, problemsHolder);
        if (value instanceof YAMLScalar && !YamlMetaUtil.isNull(value)) {
            problemsHolder.registerProblem(value,
                YAMLLocalize.yamlmetaclassErrorScalarValue().get());
        }
    }

    private void doBuildInsertionSuffixMarkup(@Nonnull YamlInsertionMarkup markup,
                                              boolean manyNotOne,
                                              @Nonnull ForcedCompletionPath.Iteration iteration) {
        markup.append(":");
        markup.doTabbedBlock(manyNotOne ? 2 : 1, () -> {
            markup.newLineAndTabs(manyNotOne);

            List<Field> allRequired =
                ContainerUtil.filter(myFeatures, field -> field.isRequired() || iteration.isNextOnPath(field));
            if (allRequired.isEmpty() && iteration.isEndOfPathReached()) {
                markup.appendCaret();
            }
            else {
                for (Iterator<Field> iterator = allRequired.iterator(); iterator.hasNext(); ) {
                    Field field = iterator.next();
                    buildCompleteKeyMarkup(markup, field, iteration.nextIterationFor(field));
                    if (iterator.hasNext()) {
                        markup.newLineAndTabs();
                    }
                }
            }
        });
    }
}
