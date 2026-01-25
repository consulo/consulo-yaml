// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.inspections;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.yaml.localize.YAMLLocalize;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.YAMLAlias;
import org.jetbrains.yaml.psi.YAMLAnchor;
import org.jetbrains.yaml.psi.YAMLValue;
import org.jetbrains.yaml.psi.YamlPsiElementVisitor;
import org.jetbrains.yaml.resolve.YAMLAliasReference;

@ExtensionImpl
public class YAMLRecursiveAliasInspection extends LocalInspectionTool {
    @Override
    @Nonnull
    public PsiElementVisitor buildVisitor(final @Nonnull ProblemsHolder holder, boolean isOnTheFly) {
        return new YamlPsiElementVisitor() {
            @Override
            public void visitAlias(@Nonnull YAMLAlias alias) {
                YAMLAliasReference reference = alias.getReference();
                YAMLAnchor anchor = reference == null ? null : reference.resolve();
                YAMLValue value = anchor == null ? null : anchor.getMarkedValue();
                if (value == null) {
                    return;
                }

                if (PsiTreeUtil.isAncestor(value, alias.getParent(), false)) {
                    holder.registerProblem(
                        reference,
                        YAMLLocalize.inspectionsRecursiveAliasMessage().get(),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                    );
                }
            }
        };
    }

    @Nonnull
    @Override
    public LocalizeValue getGroupDisplayName() {
        return LocalizeValue.empty();
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return YAMLLocalize.inspectionsRecursiveAliasName();
    }

    @Nullable
    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }
}
