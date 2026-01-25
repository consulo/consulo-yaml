// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.inspections;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiUtilCore;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.util.collection.MultiMap;
import consulo.yaml.localize.YAMLLocalize;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLSequence;
import org.jetbrains.yaml.psi.YAMLSequenceItem;
import org.jetbrains.yaml.psi.YamlPsiElementVisitor;

import java.util.Collection;
import java.util.List;

@ExtensionImpl
public class YAMLDuplicatedKeysInspection extends LocalInspectionTool {
    @Nullable
    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }

    @Nonnull
    @Override
    public LocalizeValue getGroupDisplayName() {
        return LocalizeValue.empty();
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return YAMLLocalize.inspectionsDuplicatedKeysName();
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }

    @Nonnull
    @Override
    public PsiElementVisitor buildVisitor(@Nonnull ProblemsHolder holder, boolean isOnTheFly) {
        return new YamlPsiElementVisitor() {
            @Override
            public void visitMapping(@Nonnull YAMLMapping mapping) {
                MultiMap<String, YAMLKeyValue> occurrences = MultiMap.create();

                for (YAMLKeyValue keyValue : mapping.getKeyValues()) {
                    String keyName = keyValue.getKeyText().trim();
                    // http://yaml.org/type/merge.html
                    if ("<<".equals(keyName)) {
                        continue;
                    }
                    if (!keyName.isEmpty()) {
                        occurrences.putValue(keyName, keyValue);
                    }
                }

                for (var entry : occurrences.entrySet()) {
                    String key = entry.getKey();
                    Collection<YAMLKeyValue> values = entry.getValue();
                    if (values.size() > 1) {
                        boolean allObjects = values.stream().allMatch(kv -> kv.getValue() instanceof YAMLMapping);
                        boolean allLists = values.stream().allMatch(kv -> kv.getValue() instanceof YAMLSequence);
                        LocalQuickFix[] fixes;
                        if (allObjects || allLists) {
                            fixes = new LocalQuickFix[]{new MergeDuplicatedSectionsQuickFix(), new RemoveDuplicatedKeyQuickFix()};
                        }
                        else {
                            fixes = new LocalQuickFix[]{new RemoveDuplicatedKeyQuickFix()};
                        }
                        for (YAMLKeyValue duplicatedKey : values) {
                            PsiElement keyElement = duplicatedKey.getKey();
                            if (keyElement == null) {
                                continue;
                            }
                            if (duplicatedKey.getParentMapping() == null) {
                                continue;
                            }
                            holder.registerProblem(keyElement,
                                YAMLLocalize.yamlduplicatedkeysinspectionDuplicatedKey(key).get(),
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                fixes);
                        }
                    }
                }
            }
        };
    }

    private static class MergeDuplicatedSectionsQuickFix implements LocalQuickFix {
        @Nonnull
        @Override
        public LocalizeValue getName() {
            return YAMLLocalize.yamlduplicatedkeysinspectionMergeQuickfixName();
        }

        @Override
        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            PsiElement parent = descriptor.getPsiElement().getParent();
            if (!(parent instanceof YAMLKeyValue keyVal)) {
                return;
            }
            mergeDuplicates(YAMLElementGenerator.getInstance(project), keyVal);
        }

        private void mergeDuplicates(YAMLElementGenerator generator, YAMLKeyValue keyVal) {
            YAMLMapping parentMapping = keyVal.getParentMapping();
            if (parentMapping == null) {
                return;
            }
            String key = keyVal.getKeyText();
            List<YAMLKeyValue> allProps = parentMapping.getKeyValues().stream()
                .filter(kv -> kv.getKeyText().equals(key))
                .toList();
            if (allProps.size() <= 1) {
                return;
            }
            YAMLKeyValue firstProperty = allProps.get(0);
            for (int i = 1; i < allProps.size(); i++) {
                YAMLKeyValue it = allProps.get(i);
                boolean merged = false;
                if (firstProperty.getValue() instanceof YAMLMapping mapping) {
                    merged = mergeMappings(mapping, it, generator);
                }
                else if (firstProperty.getValue() instanceof YAMLSequence sequence) {
                    merged = mergeSequences(sequence, it, generator);
                }
                if (merged) {
                    deleteWithPrecedingEol(it);
                }
            }
        }

        private boolean mergeMappings(YAMLMapping mapping, YAMLKeyValue it, YAMLElementGenerator generator) {
            if (!(it.getValue() instanceof YAMLMapping currentMapping)) {
                return false;
            }
            for (YAMLKeyValue pp : currentMapping.getKeyValues()) {
                YAMLKeyValue existing = mapping.getKeyValueByKey(pp.getKeyText());
                if (existing != null) {
                    String existingText = existing.getValue() != null ? existing.getValue().getText() : null;
                    String ppText = pp.getValue() != null ? pp.getValue().getText() : null;
                    if (existingText != null && existingText.equals(ppText)) {
                        continue;
                    }
                }
                if (pp.getValue() != null && pp.getName() != null) {
                    YAMLKeyValue newProp = generator.createYamlKeyValue(pp.getName(), "foo");
                    if (newProp.getValue() != null) {
                        newProp.getValue().replace(pp.getValue());
                    }
                    Collection<YAMLKeyValue> keyValues = mapping.getKeyValues();
                    if (!keyValues.isEmpty()) {
                        YAMLKeyValue lastKeyValue = null;
                        for (YAMLKeyValue kv : keyValues) {
                            lastKeyValue = kv;
                        }
                        PsiElement eol = mapping.addAfter(generator.createEol(), lastKeyValue);
                        PsiElement addedProp = mapping.addAfter(newProp, eol);
                        if (addedProp instanceof YAMLKeyValue addedKeyValue) {
                            mergeDuplicates(generator, addedKeyValue);
                        }
                    }
                }
                deleteWithPrecedingEol(pp);
            }
            return true;
        }

        private boolean mergeSequences(YAMLSequence sequence, YAMLKeyValue it, YAMLElementGenerator generator) {
            if (!(it.getValue() instanceof YAMLSequence currentSequence)) {
                return false;
            }
            for (YAMLSequenceItem pp : currentSequence.getItems()) {
                if (pp.getValue() != null) {
                    String ppText = pp.getValue().getText();
                    boolean exists = sequence.getItems().stream()
                        .anyMatch(item -> item.getValue() != null && item.getValue().getText().equals(ppText));
                    if (exists) {
                        continue;
                    }
                    YAMLSequenceItem newItem = generator.createSequenceItem("foo");
                    if (newItem.getValue() != null) {
                        newItem.getValue().replace(pp.getValue());
                    }
                    List<YAMLSequenceItem> items = sequence.getItems();
                    if (!items.isEmpty()) {
                        YAMLSequenceItem lastItem = items.get(items.size() - 1);
                        PsiElement eol = sequence.addAfter(generator.createEol(), lastItem);
                        sequence.addAfter(newItem, eol);
                    }
                }
                deleteWithPrecedingEol(pp);
            }
            return true;
        }

        private void deleteWithPrecedingEol(PsiElement it) {
            PsiElement prevSibling = it.getPrevSibling();
            it.delete();
            if (prevSibling != null) {
                IElementType elementType = PsiUtilCore.getElementType(prevSibling);
                if (elementType == YAMLTokenTypes.EOL) {
                    prevSibling.delete();
                }
            }
        }
    }

    private static class RemoveDuplicatedKeyQuickFix implements LocalQuickFix {
        @Nonnull
        @Override
        public LocalizeValue getName() {
            return YAMLLocalize.yamlduplicatedkeysinspectionRemoveKeyQuickfixName();
        }

        @Override
        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            PsiElement parent = descriptor.getPsiElement().getParent();
            if (!(parent instanceof YAMLKeyValue keyVal)) {
                return;
            }
            YAMLMapping parentMapping = keyVal.getParentMapping();
            if (parentMapping != null) {
                parentMapping.deleteKeyValue(keyVal);
            }
        }
    }
}
