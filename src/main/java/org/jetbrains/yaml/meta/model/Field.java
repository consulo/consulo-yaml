// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.meta.model;

import consulo.execution.debug.icon.ExecutionDebugIconGroup;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.ui.image.Image;
import consulo.util.lang.Pair;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("UnusedReturnValue")
public class Field {
  private static final Pattern PATTERN_ANYTHING = Pattern.compile(".*");

  public enum Relation {
    SCALAR_VALUE,
    SEQUENCE_ITEM,
    OBJECT_CONTENTS
  }

  private final String myName;
  private final MetaTypeSupplier myMetaTypeSupplier;
  // must be accessed with getMainType()
  @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
  private YamlMetaType myMainType;
  private boolean myIsRequired;
  private boolean myEditable = true;
  private boolean myDeprecated = false;
  private @Nullable Pattern myNamePattern;
  private boolean myEmptyValueAllowed;
  private boolean myIsMany;
  private Relation myOverriddenDefaultRelation;
  private Pair<String, List<String>> myRequiredSiblingValues;
  private final Map<Relation, YamlMetaType> myPerRelationTypes = new HashMap<>();

  /**
   * Used in {@link Field#Field(String, MetaTypeSupplier)}.
   * Invoked only once
   */
  public interface MetaTypeSupplier {
    @Nonnull
    YamlMetaType getMainType();

    default @Nullable YamlMetaType getSpecializedType(@SuppressWarnings("unused") @Nonnull YAMLValue element) {
      return null;
    }
  }

  public Field(@NonNls @Nonnull String name, @Nonnull YamlMetaType mainType) {
    myName = name;
    myMainType = mainType;
    if(myMainType instanceof YamlArrayType) {
      myMainType = ((YamlArrayType)myMainType).getElementType();
      myIsMany = !(myMainType instanceof YamlArrayType);
    }
    myMetaTypeSupplier = null;
  }

  /**
   * Used for late initialization of the field metatype.
   * Useful when the type isn't fully constructed at the moment of the field initialization (e.g. for cyclic dependencies)
   */
  public Field(@NonNls @Nonnull String name, @Nonnull MetaTypeSupplier supplier) {
    myName = name;
    myMetaTypeSupplier = supplier;
  }

  public @Nonnull Field withDefaultRelation(@Nonnull Relation relation) {
    myOverriddenDefaultRelation = relation;
    return this;
  }

  public @Nonnull Field withRequiredSibling(String key, List<String> values) {
    myRequiredSiblingValues = new Pair<>(key, values);
    return this;
  }

  public Pair<String, List<String>> getRequiredSibling() {
    return myRequiredSiblingValues;
  }

  public Field withRelationSpecificType(@Nonnull Relation relation, @Nonnull YamlMetaType specificType) {
    myPerRelationTypes.put(relation, specificType);
    return this;
  }

  public @Nonnull Field withMultiplicityMany() {
    return withMultiplicityManyNotOne(true);
  }

  public @Nonnull Field withMultiplicityManyNotOne(boolean manyNotOne) {
    myIsMany = manyNotOne;
    return this;
  }

  @Contract(pure = true)
  public boolean isMany() {
    return myIsMany;
  }

  public @Nonnull Field setRequired() {
    myIsRequired = true;
    return this;
  }

  public @Nonnull Field setDeprecated() {
    myDeprecated = true;
    return this;
  }

  /**
   * Marks the field non-editable. This is useful when the file content is not created initially by user, but rather machine-generated,
   * and contains fields not intended for editing, but still valid in terms of the data schema.
   * (This is very common for Kubernetes resource files, for example.)
   * Non-editable fields aren't included in completion lists. Also there is an inspection for highlighting such data.
   */
  public @Nonnull Field setNonEditable() {
    myEditable = false;
    return this;
  }

  @Contract(pure = true)
  public final boolean isRequired() {
    return myIsRequired;
  }

  /**
   * Returns whether the field is editable. True by default
   *
   * @see #setNonEditable()
   */
  @Contract(pure = true)
  public final boolean isEditable() {
    return myEditable;
  }

  /**
   * Returns whether the field is deprecated. False by default
   *
   * @see #setDeprecated()
   */
  @Contract(pure = true)
  public boolean isDeprecated() {
    return myDeprecated;
  }

  @Contract(pure = true)
  public final String getName() {
    return myName;
  }

  @Contract(pure = true)
  public @Nonnull YamlMetaType getType(@Nonnull Relation relation) {
    return myPerRelationTypes.getOrDefault(relation, getMainType());
  }

  @Contract(pure = true)
  public @Nonnull YamlMetaType getDefaultType() {
    return getType(getDefaultRelation());
  }

  /**
   * Returns the default relation between the field and its value. For most normal fields it can be computed based on type and multiplicity
   * but for polymorphic fields the main relation should be assigned explicitly.
   */
  public @Nonnull Relation getDefaultRelation() {
    if (myOverriddenDefaultRelation != null) {
      return myOverriddenDefaultRelation;
    }
    if (myIsMany || getMainType() instanceof YamlArrayType) {
      return Relation.SEQUENCE_ITEM;
    }
    return getMainType() instanceof YamlScalarType ? Relation.SCALAR_VALUE : Relation.OBJECT_CONTENTS;
  }

  public @Nonnull Field withEmptyValueAllowed(boolean allow) {
    myEmptyValueAllowed = allow;
    return this;
  }

  public final @Nonnull Field withAnyName() {
    return withAnyName(true);
  }

  public @Nonnull Field withAnyName(boolean allowAnyName) {
    myNamePattern = allowAnyName ? PATTERN_ANYTHING : null;
    return this;
  }

  public @Nonnull Field withNamePattern(@Nonnull Pattern pattern) {
    myNamePattern = pattern.pattern().equals(PATTERN_ANYTHING.pattern()) ? PATTERN_ANYTHING : pattern;
    return this;
  }

  public final boolean isAnyNameAllowed() {
    return PATTERN_ANYTHING == myNamePattern;
  }

  public final boolean isByPattern() {
    return myNamePattern != null;
  }

  public final boolean acceptsFieldName(@Nonnull String actualName) {
    if (myNamePattern == null) {
      return false;
    }
    if (myNamePattern == PATTERN_ANYTHING) {
      return true;
    }
    return myNamePattern.matcher(actualName).matches();
  }

  public final boolean isEmptyValueAllowed() {
    return myEmptyValueAllowed;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append("[").append(getName()).append("]@");
    result.append(Integer.toHexString(hashCode()));
    result.append(" : ");
    result.append(getMainType().getTypeName());

    List<String> nonDefaultTypes = myPerRelationTypes.entrySet().stream()
      .filter(e -> e.getValue() == getMainType())
      .map(e -> e.getKey() + ":" + e.getValue())
      .collect(Collectors.toList());

    if (!nonDefaultTypes.isEmpty()) {
      result.append(nonDefaultTypes);
    }
    return result.toString();
  }

  public @Nonnull List<LookupElementBuilder> getKeyLookups(@Nonnull YamlMetaType ownerClass,
                                                           @Nonnull PsiElement insertedScalar) {
    if (isByPattern()) {
      return Collections.emptyList();
    }

    LookupElementBuilder lookup = LookupElementBuilder
      .create(new TypeFieldPair(ownerClass, this), getName())
      .withTypeText(getMainType().getDisplayName(), true)
      .withIcon(getLookupIcon())
      .withStrikeoutness(isDeprecated());

    if (isRequired()) {
      lookup = lookup.bold();
    }
    return Collections.singletonList(lookup);
  }

  public @Nullable PsiReference getReferenceFromKey(@Nonnull YAMLKeyValue keyValue) {
    return null;
  }

  public PsiReference[] getReferencesFromKey(@Nonnull YAMLKeyValue keyValue) {
    return Optional.ofNullable(getReferenceFromKey(keyValue))
      .map(ref -> new PsiReference[]{ref})
      .orElse(PsiReference.EMPTY_ARRAY);
  }

  public boolean hasRelationSpecificType(@Nonnull Relation relation) {
    return relation == getDefaultRelation() || myPerRelationTypes.containsKey(relation);
  }

  public @Nullable Image getLookupIcon() {
    return myIsMany ? ExecutionDebugIconGroup.nodeArray() : getMainType().getIcon();
  }

  public @Nonnull Field resolveToSpecializedField(@Nonnull YAMLValue element) {
    if(myMetaTypeSupplier == null)
      return this;

    YamlMetaType specializedType = myMetaTypeSupplier.getSpecializedType(element);
    if(specializedType == null)
      return this;

    return cloneWithNewType(specializedType);
  }

  private Field cloneWithNewType(@Nonnull YamlMetaType newType) {
    var result = newField(newType);

    result.myIsRequired = myIsRequired;
    result.myEditable = myEditable;
    result.myDeprecated = myDeprecated;
    result.myNamePattern = myNamePattern;
    result.myEmptyValueAllowed = myEmptyValueAllowed;
    result.myIsMany = myIsMany;
    result.myOverriddenDefaultRelation = myOverriddenDefaultRelation;
    result.myPerRelationTypes.putAll(myPerRelationTypes);

    return result;
  }

  protected @Nonnull Field newField(@Nonnull YamlMetaType type) {
    return new Field(this.myName, type);
  }

  private @Nonnull YamlMetaType getMainType() {
    if(myMainType != null)
      return myMainType;

    assert myMetaTypeSupplier != null;

    synchronized (myMetaTypeSupplier) {
      if(myMainType == null) {
        try {
          YamlMetaType mainType = myMetaTypeSupplier.getMainType();
          assert !(myMainType instanceof YamlArrayType) : "Type supplier must not provide array types";

          myMainType = mainType;
        }
        catch (Exception e) {
          throw new RuntimeException("Supplier failed to return a metatype for field: " + this, e);
        }
      }
      return myMainType;
    }
  }
}
