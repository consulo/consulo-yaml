// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.yaml.annotator;

import consulo.annotation.access.RequiredReadAction;
import consulo.document.util.TextRange;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.impl.ast.TreeUtil;
import consulo.language.impl.psi.LeafPsiElement;
import consulo.language.psi.OuterLanguageElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.yaml.localize.YAMLLocalize;
import jakarta.annotation.Nonnull;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLAnchor;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequenceItem;
import org.jetbrains.yaml.psi.YAMLValue;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;
import org.jetbrains.yaml.psi.impl.YAMLBlockSequenceImpl;
import org.jetbrains.yaml.psi.impl.YAMLKeyValueImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class YAMLInvalidBlockChildrenErrorAnnotator implements Annotator {
    @Override
    @RequiredReadAction
    public void annotate(@Nonnull PsiElement element, @Nonnull AnnotationHolder holder) {
        Collection<OuterLanguageElement> outerElements = PsiTreeUtil.findChildrenOfType(element, OuterLanguageElement.class);
        if (!outerElements.isEmpty()) return;

        if (anotherErrorWillBeReported(element)) return;

        if (reportSameLineWarning(element, holder)) return;

        if (element instanceof YAMLBlockMappingImpl blockMapping) {
            if (!isValidBlockMapChild(blockMapping.getFirstChild())) {
                PsiElement reportElement = blockMapping.getFirstKeyValue().getKey();
                if (reportElement == null) {
                    reportElement = blockMapping.getFirstKeyValue();
                }
                reportWholeElementProblem(holder, blockMapping, reportElement);
                return;
            }

            for (PsiElement child : blockMapping.getChildren()) {
                if (!isValidBlockMapChild(child)) {
                    reportSubElementProblem(holder, YAMLLocalize.inspectionsInvalidChildInBlockMapping(), child);
                    break;
                }
            }

            checkIndent(new ArrayList<>(blockMapping.getKeyValues()), holder, YAMLLocalize.inspectionsInvalidKeyIndent());
        }

        if (element instanceof YAMLBlockSequenceImpl blockSequence) {
            if (!isValidBlockSequenceChild(blockSequence.getFirstChild())) {
                List<YAMLSequenceItem> items = blockSequence.getItems();
                PsiElement reportElement = items.isEmpty() ? blockSequence : items.get(0);
                reportWholeElementProblem(holder, blockSequence, reportElement);
                return;
            }

            for (PsiElement child : blockSequence.getChildren()) {
                if (!isValidBlockSequenceChild(child)) {
                    reportSubElementProblem(holder, YAMLLocalize.inspectionsInvalidChildInBlockSequence(), child);
                    break;
                }
            }

            checkIndent(new ArrayList<>(blockSequence.getItems()), holder, YAMLLocalize.inspectionsInvalidListItemIndent());
        }
    }

    @RequiredReadAction
    private void reportWholeElementProblem(AnnotationHolder holder, PsiElement element, PsiElement reportElement) {
        holder.newAnnotation(HighlightSeverity.ERROR, getMessageForParent(element))
                .range(TextRange.create(element.getTextRange().getStartOffset(), endOfLine(reportElement, element))).create();
    }

    @RequiredReadAction
    private int endOfLine(PsiElement subElement, PsiElement whole) {
        PsiElement current = subElement;
        while (true) {
            PsiElement next = PsiTreeUtil.nextLeaf(current);                 
            if (next == null) break;
            if (PsiUtilCore.getElementType(next) == YAMLTokenTypes.EOL) {
                break;
            }
            current = next;
            if (current.getTextRange().getEndOffset() >= whole.getTextRange().getEndOffset()) {
                break;
            }
        }
        return Math.min(current.getTextRange().getEndOffset(), whole.getTextRange().getEndOffset());
    }

    private void checkIndent(List<? extends PsiElement> elements, AnnotationHolder holder, LocalizeValue message) {
        if (elements.size() > 1) {
            int firstIndent = YAMLUtil.getIndentToThisElement(elements.get(0));
            for (int i = 1; i < elements.size(); i++) {
                PsiElement item = elements.get(i);
                if (YAMLUtil.getIndentToThisElement(item) != firstIndent) {
                    reportSubElementProblem(holder, message, item);
                }
            }
        }
    }

    private LocalizeValue getMessageForParent(PsiElement element) {
        PsiElement parent = findNeededParent(element);
        if (parent instanceof YAMLKeyValueImpl) {
            return YAMLLocalize.inspectionsInvalidChildInBlockMapping();
        } else {
            return YAMLLocalize.inspectionsInvalidChildInBlockSequence();
        }
    }

    private boolean isValidBlockMapChild(PsiElement element) {
        return element instanceof YAMLKeyValue || element instanceof YAMLAnchor || element instanceof LeafPsiElement;
    }

    private boolean isValidBlockSequenceChild(PsiElement element) {
        return element instanceof YAMLSequenceItem || element instanceof YAMLAnchor || element instanceof LeafPsiElement;
    }

    private boolean anotherErrorWillBeReported(PsiElement element) {
        PsiElement kvParent = findNeededParent(element);
        if (kvParent == null) return false;
        YAMLKeyValueImpl kvGrandParent = PsiTreeUtil.getParentOfType(kvParent, YAMLKeyValueImpl.class, false);
        if (kvGrandParent == null) return false;

        return YAMLUtil.psiAreAtTheSameLine(kvGrandParent, element);
    }

    private PsiElement findNeededParent(PsiElement element) {
        return PsiTreeUtil.findFirstParent(element, true,
                it -> it instanceof YAMLKeyValueImpl || it instanceof YAMLSequenceItem);
    }

    private boolean reportSameLineWarning(PsiElement value, AnnotationHolder holder) {
        PsiElement keyValue = value.getParent();
        if (!(keyValue instanceof YAMLKeyValue yamlKeyValue)) return false;
        PsiElement key = yamlKeyValue.getKey();
        if (key == null) return false;
        if (value instanceof YAMLBlockMappingImpl blockMapping) {
            YAMLKeyValue firstSubValue = blockMapping.getFirstKeyValue();
            if (YAMLUtil.psiAreAtTheSameLine(key, firstSubValue)) {
                reportAboutSameLine(holder, blockMapping);
                return true;
            }
        }
        if (value instanceof YAMLBlockSequenceImpl blockSequence) {
            List<YAMLSequenceItem> items = blockSequence.getItems();
            if (items.isEmpty()) {
                // a very strange situation: a sequence without any item
                return true;
            }
            YAMLSequenceItem firstItem = items.get(0);
            if (YAMLUtil.psiAreAtTheSameLine(key, firstItem)) {
                reportAboutSameLine(holder, blockSequence);
                return true;
            }
        }
        return false;
    }

    private void reportAboutSameLine(AnnotationHolder holder, YAMLValue value) {
        reportSubElementProblem(holder, YAMLLocalize.annotatorSameLineComposedValueMessage(), value);
    }

    @RequiredReadAction
    private void reportSubElementProblem(AnnotationHolder holder, LocalizeValue message, PsiElement subElement) {
        var firstLeafNode = TreeUtil.findFirstLeaf(subElement.getNode());
        if (firstLeafNode == null) return;
        PsiElement firstLeaf = firstLeafNode.getPsi();
        if (firstLeaf == null) return;
        holder.newAnnotation(HighlightSeverity.ERROR, message)
                .range(TextRange.create(subElement.getTextRange().getStartOffset(), endOfLine(firstLeaf, subElement))).create();
    }
}
