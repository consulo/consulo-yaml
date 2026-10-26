// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.meta.model;

import jakarta.annotation.Nonnull;

public class TypeFieldPair {
  private final @Nonnull Field myField;
  private final @Nonnull YamlMetaType myOwnerClass;

  public TypeFieldPair(@Nonnull YamlMetaType ownerClass, @Nonnull Field field) {
    myField = field;
    myOwnerClass = ownerClass;
  }

  public @Nonnull YamlMetaType getMetaType() {
    return myOwnerClass;
  }

  public @Nonnull Field getField() {
    return myField;
  }
}
