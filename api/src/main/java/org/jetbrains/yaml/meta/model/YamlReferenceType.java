/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package org.jetbrains.yaml.meta.model;

import consulo.language.psi.PsiReference;
import jakarta.annotation.Nonnull;
import org.jetbrains.yaml.psi.YAMLScalar;

public abstract class YamlReferenceType extends YamlScalarType {
    /**
     * @deprecated initialise the {@code displayName} explicitly via {@link #YamlReferenceType(String, String)}
     */
    @Deprecated(forRemoval = true)
    protected YamlReferenceType(@Nonnull String typeName) {
        super(typeName);
    }

    protected YamlReferenceType(@Nonnull String typeName, @Nonnull String displayName) {
        super(typeName, displayName);
    }

    @Nonnull
    public PsiReference[] getReferencesFromValue(@Nonnull YAMLScalar valueScalar) {
        return PsiReference.EMPTY_ARRAY;
    }
}
