// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.yaml.psi;

import consulo.application.progress.ProgressManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiRecursiveVisitor;
import jakarta.annotation.Nonnull;

public abstract class YamlRecursivePsiElementVisitor extends YamlPsiElementVisitor implements PsiRecursiveVisitor {
    @Override
    public void visitElement(@Nonnull PsiElement element) {
        ProgressManager.checkCanceled();
        element.acceptChildren(this);
    }
}
