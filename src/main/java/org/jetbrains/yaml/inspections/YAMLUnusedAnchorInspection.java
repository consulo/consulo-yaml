// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.inspections;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.codeStyle.PostprocessReformattingAspect;
import consulo.language.editor.inspection.*;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.impl.ast.TreeUtil;
import consulo.language.impl.psi.CodeEditUtil;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.ReferencesSearch;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.yaml.localize.YAMLLocalize;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.yaml.YAMLElementTypes;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.YAMLAnchor;
import org.jetbrains.yaml.psi.YamlPsiElementVisitor;

import java.util.Collection;

@ExtensionImpl
public final class YAMLUnusedAnchorInspection extends LocalInspectionTool {
    @Override
    public @Nonnull PsiElementVisitor buildVisitor(final @Nonnull ProblemsHolder holder, boolean isOnTheFly) {
        return new YamlPsiElementVisitor() {
            @Override
            public void visitAnchor(@Nonnull YAMLAnchor anchor) {
                Collection<PsiReference> references =
                    ReferencesSearch.search(anchor, GlobalSearchScope.fileScope(anchor.getContainingFile())).findAll();
                if (references.isEmpty()) {
                    holder.registerProblem(
                        anchor,
                        YAMLLocalize.inspectionsUnusedAnchorMessage(anchor.getName()).get(),
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                        new RemoveAnchorQuickFix()
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

    @Nullable
    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return YAMLLocalize.inspectionsUnusedAnchorName();
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    private static class RemoveAnchorQuickFix implements LocalQuickFix {
        @Override
        public @Nls @Nonnull LocalizeValue getName() {
            return YAMLLocalize.inspectionsUnusedAnchorQuickfixName();
        }

        @Override
        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            YAMLAnchor anchor = (YAMLAnchor) descriptor.getPsiElement();
            if (anchor == null) {
                return;
            }
            PostprocessReformattingAspect.getInstance(project).disablePostprocessFormattingInside(() -> {
                ASTNode node = TreeUtil.prevLeaf(anchor.getNode());
                while (YAMLElementTypes.SPACE_ELEMENTS.contains(PsiUtilCore.getElementType(node))) {
                    ASTNode prev = TreeUtil.prevLeaf(node);
                    ASTNode parent = node.getTreeParent();
                    if (parent != null) {
                        CodeEditUtil.removeChild(parent, node);
                    }
                    node = prev;
                }
                anchor.delete();
            });
        }
    }
}
