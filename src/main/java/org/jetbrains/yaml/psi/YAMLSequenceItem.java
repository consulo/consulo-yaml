// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.yaml.psi;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;

public interface YAMLSequenceItem extends YAMLPsiElement {
    @Nullable
    YAMLValue getValue();

    @Nonnull
    Collection<YAMLKeyValue> getKeysValues();

    int getItemIndex();
}