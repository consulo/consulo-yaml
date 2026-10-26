// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.document.Document;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.inspection.scheme.InspectionManager;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.meta.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utility functions for YAML PSI manipulation.
 */
public final class YamlPsiUtil {
    private static final List<YamlMetaType> TYPES = Arrays.asList(
        YamlBooleanType.getSharedInstance(),
        YamlNumberType.getInstance(false),
        YamlStringType.getInstance(),
        YamlAnything.getInstance()
    );

    private YamlPsiUtil() {
    }

    /**
     * Gets the key names between a value and its containing top-level sequence.
     */
    @Nonnull
    public static List<String> getKeysInBetween(@Nonnull YAMLValue value, @Nonnull YAMLSequence topSeq) {
        List<String> keys = new ArrayList<>();
        PsiElement current = value;
        while (current != null && current != topSeq) {
            if (current instanceof YAMLKeyValue keyValue) {
                keys.add(keyValue.getKeyText());
            }
            current = current.getParent();
        }
        Collections.reverse(keys);
        return keys;
    }

    /**
     * Finds structural siblings of a YAML value.
     */
    @Nonnull
    public static List<YAMLPsiElement> findStructuralSiblings(@Nonnull YAMLValue value) {
        YAMLSequence topSeq = findParentSequence(value);
        if (topSeq == null) {
            return Collections.emptyList();
        }
        return findStructuralSiblings(topSeq, getKeysInBetween(value, topSeq));
    }

    @Nullable
    public static YAMLSequence findParentSequence(@Nonnull PsiElement element) {
        PsiElement current = element.getParent();
        while (current != null) {
            if (current instanceof YAMLSequence sequence) {
                return sequence;
            }
            current = current.getParent();
        }
        return null;
    }

    /**
     * Finds structural siblings in a sequence based on a key path.
     */
    @Nonnull
    public static List<YAMLPsiElement> findStructuralSiblings(@Nonnull YAMLSequence topSeq, @Nonnull List<String> keys) {
        List<YAMLPsiElement> result = new ArrayList<>();
        if (keys.isEmpty()) {
            for (YAMLSequenceItem item : topSeq.getItems()) {
                YAMLValue itemValue = item.getValue();
                if (itemValue != null) {
                    result.add(itemValue);
                }
            }
            return result;
        }

        for (YAMLSequenceItem item : topSeq.getItems()) {
            for (YAMLKeyValue kv : item.getKeysValues()) {
                collectSiblings(kv, keys, 0, result);
            }
        }
        return result;
    }

    private static void collectSiblings(@Nonnull YAMLKeyValue root,
                                        @Nonnull List<String> keys,
                                        int keyIndex,
                                        @Nonnull List<YAMLPsiElement> result) {
        if (keyIndex >= keys.size()) {
            result.add(root);
            return;
        }

        String expectedKey = keys.get(keyIndex);
        if (!expectedKey.equals(root.getKeyText())) {
            return;
        }

        YAMLValue value = root.getValue();
        if (value == null) {
            return;
        }

        if (keyIndex == keys.size() - 1) {
            // Last key - return the value
            result.add(value);
            return;
        }

        // Continue traversing
        if (value instanceof YAMLMapping mapping) {
            for (YAMLKeyValue kv : mapping.getKeyValues()) {
                collectSiblings(kv, keys, keyIndex + 1, result);
            }
        }
        else if (value instanceof YAMLSequenceItem seqItem) {
            for (YAMLKeyValue kv : seqItem.getKeysValues()) {
                collectSiblings(kv, keys, keyIndex + 1, result);
            }
        }
        else if (value instanceof YAMLKeyValue kv) {
            collectSiblings(kv, keys, keyIndex + 1, result);
        }
    }

    /**
     * Estimates the type of a YAML scalar value.
     */
    @Nullable
    public static YamlMetaType estimatedType(@Nonnull YAMLScalar scalar) {
        for (YamlMetaType type : TYPES) {
            if (isValid(type, scalar)) {
                return type;
            }
        }
        return null;
    }

    @RequiredReadAction
    private static boolean isValid(@Nonnull YamlMetaType meta, @Nonnull YAMLValue value) {
        PsiElement tag = value.getTag();
        if (tag != null) {
            String tagText = tag.getText();
            if (tagText != null && !meta.isSupportedTag(tagText)) {
                return false;
            }
        }

        ProblemsHolder problemsHolder = InspectionManager.getInstance(value.getProject()).createProblemsHolder(
            value.getContainingFile(),
            false
        );
        meta.validateValue(value, problemsHolder);
        return !problemsHolder.hasResults();
    }

    /**
     * Returns the closest ancestor of element that has no indentation (is at the start of line).
     * In short, this helps to find the containing top-level Key-Value in non-empty documents.
     */
    @Nonnull
    public static PsiElement findClosestAncestorWithoutIndent(@Nonnull Document document, @Nonnull PsiElement element) {
        PsiElement current = element;
        while (!isAtStartOfLine(document, current)) {
            PsiElement parent = current.getParent();
            if (parent == null) {
                throw new IllegalStateException("the root of the PSI tree cannot be indented itself");
            }
            current = parent;
        }
        return current;
    }

    private static boolean isAtStartOfLine(@Nonnull Document document, @Nonnull PsiElement element) {
        int offset = element.getTextRange().getStartOffset();
        int lineNumber = document.getLineNumber(offset);
        int lineStartOffset = document.getLineStartOffset(lineNumber);
        return lineStartOffset == offset;
    }
}
