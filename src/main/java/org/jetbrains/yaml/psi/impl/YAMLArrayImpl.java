package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;
import org.jetbrains.yaml.psi.YAMLSequence;

import jakarta.annotation.Nonnull;

/**
 * @author oleg
 */
public class YAMLArrayImpl extends YAMLSequenceImpl implements YAMLSequence {
    public YAMLArrayImpl(@Nonnull final ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "YAML array";
    }
}