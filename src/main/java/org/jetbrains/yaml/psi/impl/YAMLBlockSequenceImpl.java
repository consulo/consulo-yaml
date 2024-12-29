package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;

import jakarta.annotation.Nonnull;

public class YAMLBlockSequenceImpl extends YAMLSequenceImpl {
    public YAMLBlockSequenceImpl(@Nonnull ASTNode node) {
        super(node);
    }
}
