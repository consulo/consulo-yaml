package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;

import javax.annotation.Nonnull;

public class YAMLBlockSequenceImpl extends YAMLSequenceImpl {
    public YAMLBlockSequenceImpl(@Nonnull ASTNode node) {
        super(node);
    }
}
