// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;
import jakarta.annotation.Nonnull;
import org.jetbrains.yaml.psi.YAMLSequence;

public class YAMLArrayImpl extends YAMLSequenceImpl implements YAMLSequence {
    public YAMLArrayImpl(final @Nonnull ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "YAML array";
    }
}