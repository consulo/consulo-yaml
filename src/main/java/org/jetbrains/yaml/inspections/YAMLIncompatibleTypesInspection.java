// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.inspections;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.inspection.*;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElementVisitor;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.util.dataholder.Key;
import consulo.yaml.localize.YAMLLocalize;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;
import org.jetbrains.yaml.psi.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Inspection for incompatible types in YAML files.
 * This inspection checks if scalar values have consistent types among siblings.
 */
@ExtensionImpl
public class YAMLIncompatibleTypesInspection extends LocalInspectionTool {
    private static final Key<Map<YAMLSequence, Map<List<String>, YamlMetaType>>> STRUCTURAL_SIBLINGS_TYPE_KEY =
            Key.create("STRUCTURAL_SIBLINGS");

    @Nonnull
    @Override
    public PsiElementVisitor buildVisitor(@Nonnull ProblemsHolder holder,
                                          boolean isOnTheFly,
                                          @Nonnull LocalInspectionToolSession session,
                                          Object state) {
        return new YamlPsiElementVisitor() {
            @Override
            public void visitScalar(@Nonnull YAMLScalar scalar) {
                YamlMetaType estimatedType = YamlPsiUtil.estimatedType(scalar);
                if (estimatedType == null) return;

                YamlMetaType mostPopularType = getMostPopularTypeForSiblings(scalar, session);
                if (mostPopularType == null || mostPopularType.equals(estimatedType)) return;

                LocalizeValue message = YAMLLocalize.inspectionsIncompatibleTypesMessage(
                        estimatedType.getDisplayName(),
                        mostPopularType.getDisplayName()
                );

                if (mostPopularType == YamlStringType.getInstance()) {
                    List<YAMLPsiElement> siblings = YamlPsiUtil.findStructuralSiblings(scalar);
                    boolean singleQuote = siblings.stream()
                            .filter(s -> s instanceof YAMLQuotedText)
                            .map(s -> (YAMLQuotedText) s)
                            .findFirst()
                            .map(YAMLQuotedText::isSingleQuote)
                            .orElse(false);

                    LocalQuickFix[] fixes;
                    boolean hasSiblingsToWrap = siblings.stream()
                            .anyMatch(s -> s != scalar && !(s instanceof YAMLQuotedText));

                    if (hasSiblingsToWrap) {
                        fixes = new LocalQuickFix[]{
                                new YAMLAddQuoteQuickFix(YAMLLocalize.inspectionsIncompatibleTypesQuickfixWrapQuotesMessage(), singleQuote),
                                new YAMLAddQuotesToSiblingsQuickFix(singleQuote)
                        };
                    } else {
                        fixes = new LocalQuickFix[]{
                                new YAMLAddQuoteQuickFix(YAMLLocalize.inspectionsIncompatibleTypesQuickfixWrapQuotesMessage(), singleQuote)
                        };
                    }

                    holder.registerProblem(scalar, message.get(), fixes);
                } else {
                    holder.registerProblem(scalar, message.get());
                }

                super.visitScalar(scalar);
            }

            @Nullable
            private YamlMetaType getMostPopularTypeForSiblings(@Nonnull YAMLValue value,
                                                               @Nonnull LocalInspectionToolSession session) {
                YAMLSequence topSeq = YamlPsiUtil.findParentSequence(value);
                if (topSeq == null) return null;

                List<String> keys = YamlPsiUtil.getKeysInBetween(value, topSeq);

                // Memoization
                Map<YAMLSequence, Map<List<String>, YamlMetaType>> cache = session.getUserData(STRUCTURAL_SIBLINGS_TYPE_KEY);
                if (cache == null) {
                    cache = new HashMap<>();
                    session.putUserData(STRUCTURAL_SIBLINGS_TYPE_KEY, cache);
                }

                Map<List<String>, YamlMetaType> seqCache = cache.computeIfAbsent(topSeq, k -> new HashMap<>());
                if (seqCache.containsKey(keys)) {
                    return seqCache.get(keys);
                }

                // Compute the most popular type
                List<YAMLPsiElement> siblings = YamlPsiUtil.findStructuralSiblings(topSeq, keys);
                Map<YamlMetaType, Integer> typeCounts = new HashMap<>();
                for (YAMLPsiElement sibling : siblings) {
                    if (sibling instanceof YAMLScalar scalar) {
                        YamlMetaType type = YamlPsiUtil.estimatedType(scalar);
                        if (type != null) {
                            typeCounts.merge(type, 1, Integer::sum);
                        }
                    }
                }

                YamlMetaType mostPopular = null;
                int maxCount = 0;
                for (Map.Entry<YamlMetaType, Integer> entry : typeCounts.entrySet()) {
                    if (entry.getValue() > maxCount) {
                        maxCount = entry.getValue();
                        mostPopular = entry.getKey();
                    }
                }

                seqCache.put(keys, mostPopular);
                return mostPopular;
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
        return YAMLLocalize.inspectionsTypesMismatchName();
    }

    @Nullable
    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    private static class YAMLAddQuotesToSiblingsQuickFix implements LocalQuickFix {
        private final boolean singleQuote;

        YAMLAddQuotesToSiblingsQuickFix(boolean singleQuote) {
            this.singleQuote = singleQuote;
        }

        @Nonnull
        @Override
        public LocalizeValue getName() {
            return YAMLLocalize.inspectionsIncompatibleTypesQuickfixWrapAllQuotesMessage();
        }

        @Override
        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            if (!(descriptor.getPsiElement() instanceof YAMLValue baseElement)) return;

            List<YAMLPsiElement> siblings = YamlPsiUtil.findStructuralSiblings(baseElement);
            for (YAMLPsiElement sibling : siblings) {
                if (!(sibling instanceof YAMLQuotedText)) {
                    YAMLAddQuoteQuickFix.wrapWithQuotes(sibling, singleQuote);
                }
            }
        }
    }
}
