// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.yaml.psi;

import consulo.language.psi.PsiElementVisitor;
import jakarta.annotation.Nonnull;

public abstract class YamlPsiElementVisitor extends PsiElementVisitor {
    public void visitAlias(@Nonnull YAMLAlias alias) {
        visitValue(alias);
    }

    public void visitAnchor(@Nonnull YAMLAnchor anchor) {
        visitElement(anchor);
    }

    public void visitCompoundValue(@Nonnull YAMLCompoundValue compoundValue) {
        visitValue(compoundValue);
    }

    public void visitDocument(@Nonnull YAMLDocument document) {
        visitElement(document);
    }

    public void visitKeyValue(@Nonnull YAMLKeyValue keyValue) {
        visitElement(keyValue);
    }

    public void visitMapping(@Nonnull YAMLMapping mapping) {
        visitCompoundValue(mapping);
    }

    public void visitSequenceItem(@Nonnull YAMLSequenceItem sequenceItem) {
        visitElement(sequenceItem);
    }

    public void visitQuotedText(@Nonnull YAMLQuotedText quotedText) {
        visitScalar(quotedText);
    }

    public void visitScalar(@Nonnull YAMLScalar scalar) {
        visitValue(scalar);
    }

    public void visitScalarList(@Nonnull YAMLScalarList scalarList) {
        visitScalar(scalarList);
    }

    public void visitScalarText(@Nonnull YAMLScalarText scalarText) {
        visitScalar(scalarText);
    }

    public void visitValue(@Nonnull YAMLValue value) {
        visitElement(value);
    }

    public void visitSequence(@Nonnull YAMLSequence sequence) {
        visitCompoundValue(sequence);
    }
}
