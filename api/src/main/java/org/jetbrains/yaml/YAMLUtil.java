// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.impl.ast.LeafElement;
import consulo.language.impl.ast.TreeUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.logging.Logger;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.SmartList;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import consulo.yaml.localize.YAMLLocalize;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.psi.*;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public final class YAMLUtil {
    private static final TokenSet BLANK_LINE_ELEMENTS = TokenSet.andNot(YAMLElementTypes.BLANK_ELEMENTS, YAMLElementTypes.EOL_ELEMENTS);

    /**
     * This method return flattened key path (consist of ancestors until a document).
     * </p>
     * YAML is frequently used in configured files.
     * Dot separator preforms access to child keys.
     * <pre>{@code
     *  top:
     *    next:
     *      list:
     *        - needKey: value
     * }</pre>
     * Flattened {@code needKey} is {@code top.next.list[0].needKey}
     */
    @RequiredReadAction
    public static @Nonnull String getConfigFullName(@Nonnull YAMLPsiElement target) {
        return StringUtil.join(getConfigFullNameParts(target), ".");
    }

    @RequiredReadAction
    public static @Nonnull List<String> getConfigFullNameParts(@Nonnull YAMLPsiElement target) {
        List<String> result = new SmartList<>();
        PsiElement element = target;
        while (element != null) {
            String elementIndexSuffix = "";
            if (element instanceof YAMLSequenceItem sequenceItem) {
                elementIndexSuffix = "[" + sequenceItem.getItemIndex() + "]";
                element = PsiTreeUtil.getParentOfType(element, YAMLKeyValue.class);
            }

            if (element instanceof YAMLKeyValue keyValue) {
                String keyText = keyValue.getKeyText();
                result.add(keyText + elementIndexSuffix);
            }
            element = PsiTreeUtil.getParentOfType(element, YAMLKeyValue.class, YAMLSequenceItem.class);
        }
        return ContainerUtil.reverse(result);
    }

    @RequiredReadAction
    public static @Nonnull Collection<YAMLKeyValue> getTopLevelKeys(YAMLFile file) {
        YAMLValue topLevelValue = file.getDocuments().get(0).getTopLevelValue();
        if (topLevelValue instanceof YAMLMapping mapping) {
            return mapping.getKeyValues();
        }
        else {
            return Collections.emptyList();
        }
    }

    @RequiredReadAction
    public static @Nullable YAMLKeyValue getQualifiedKeyInFile(YAMLFile file, List<String> key) {
        return getQualifiedKeyInDocument(file.getDocuments().get(0), key);
    }

    @RequiredReadAction
    public static @Nullable YAMLKeyValue getQualifiedKeyInDocument(@Nonnull YAMLDocument document, @Nonnull List<String> key) {
        assert !key.isEmpty();

        YAMLMapping mapping = ObjectUtil.tryCast(document.getTopLevelValue(), YAMLMapping.class);
        for (int i = 0; i < key.size(); i++) {
            if (mapping == null) {
                return null;
            }

            YAMLKeyValue keyValue = mapping.getKeyValueByKey(String.join(".", key.subList(i, key.size())));
            if (keyValue != null) {
                return keyValue;
            }

            keyValue = mapping.getKeyValueByKey(key.get(i));
            if (keyValue == null || i + 1 == key.size()) {
                return keyValue;
            }

            mapping = ObjectUtil.tryCast(keyValue.getValue(), YAMLMapping.class);
        }
        throw new IllegalStateException("Should have returned from the loop");
    }

    @RequiredReadAction
    public static @Nullable YAMLKeyValue getQualifiedKeyInFile(YAMLFile file, String... key) {
        return getQualifiedKeyInFile(file, Arrays.asList(key));
    }

    @RequiredReadAction
    public static @Nullable YAMLKeyValue findKeyInProbablyMapping(@Nullable YAMLValue node, @Nonnull String keyText) {
        if (!(node instanceof YAMLMapping mapping)) {
            return null;
        }
        return mapping.getKeyValueByKey(keyText);
    }

    @RequiredReadAction
    public static @Nullable Pair<PsiElement, String> getValue(YAMLFile file, String... key) {
        YAMLKeyValue record = getQualifiedKeyInFile(file, key);
        if (record != null) {
            PsiElement psiValue = record.getValue();
            return Pair.create(psiValue, record.getValueText());
        }
        return null;
    }

    @RequiredWriteAction
    public static @Nonnull YAMLKeyValue createI18nRecord(YAMLFile file, String[] key, String text) {
        YAMLDocument root = file.getDocuments().get(0);
        assert root != null;
        assert key.length > 0;

        YAMLMapping rootMapping = PsiTreeUtil.findChildOfType(root, YAMLMapping.class);
        if (rootMapping == null) {
            YAMLFile yamlFile = YAMLElementGenerator.getInstance(file.getProject()).createDummyYamlWithText(key[0] + ":");
            YAMLMapping mapping = (YAMLMapping) yamlFile.getDocuments().get(0).getTopLevelValue();
            assert mapping != null;
            rootMapping = ((YAMLMapping) root.add(mapping));
        }

        YAMLMapping current = rootMapping;
        int keyLength = key.length;
        int i;
        for (i = 0; i < keyLength; i++) {
            YAMLKeyValue existingRec = current.getKeyValueByKey(key[i]);
            if (existingRec != null) {
                YAMLMapping nextMapping = ObjectUtil.tryCast(existingRec.getValue(), YAMLMapping.class);

                if (nextMapping != null) {
                    current = nextMapping;
                    continue;
                }
            }

            // Calc current key indent

            String indent = StringUtil.repeatSymbol(' ', getIndentInThisLine(current));

            // Generate items
            StringBuilder builder = new StringBuilder();
            builder.append("---");
            for (int j = i; j < keyLength; j++) {
                builder.append("\n").append(indent);
                builder.append(key[j]).append(":");
                indent += "  ";
            }
            builder.append(" ").append(text);

            // Create dummy mapping
            YAMLFile fileWithKey = YAMLElementGenerator.getInstance(file.getProject()).createDummyYamlWithText(builder.toString());
            YAMLMapping dummyMapping = PsiTreeUtil.findChildOfType(fileWithKey.getDocuments().get(0), YAMLMapping.class);
            assert dummyMapping != null && dummyMapping.getKeyValues().size() == 1;

            // Add or replace
            YAMLKeyValue dummyKeyValue = dummyMapping.getKeyValues().iterator().next();
            current.putKeyValue(dummyKeyValue);

            if (!(dummyKeyValue.getValue() instanceof YAMLMapping)) {
                return dummyKeyValue;
            }
            else {
                current = ((YAMLMapping) dummyKeyValue.getValue());
            }

        }

        // Conflict with existing value
        StringBuilder builder = new StringBuilder();
        int top = Math.min(i + 1, keyLength);
        for (int j = 0; j < top; j++) {
            if (!builder.isEmpty()) {
                builder.append('.');
            }
            builder.append(key[j]);
        }
        throw new IncorrectOperationException(YAMLLocalize.newNameConflictsWith(builder.toString()));
    }

    @RequiredWriteAction
    public static PsiElement rename(YAMLKeyValue element, String newName) {
        if (newName.equals(element.getName())) {
            throw new IncorrectOperationException(YAMLLocalize.renameSameName());
        }
        YAMLKeyValue topKeyValue = YAMLElementGenerator.getInstance(element.getProject()).createYamlKeyValue(newName, "Foo");

        PsiElement key = element.getKey();
        if (key == null || topKeyValue.getKey() == null) {
            throw new IllegalStateException();
        }
        key.replace(topKeyValue.getKey());
        return element;
    }

    @RequiredReadAction
    public static int getIndentInThisLine(@Nonnull PsiElement elementInLine) {
        PsiElement currentElement = elementInLine;
        while (currentElement != null) {
            IElementType type = currentElement.getNode().getElementType();
            if (type == YAMLTokenTypes.EOL) {
                return 0;
            }
            if (type == YAMLTokenTypes.INDENT) {
                return currentElement.getTextLength();
            }

            currentElement = PsiTreeUtil.prevLeaf(currentElement);
        }
        return 0;
    }

    @RequiredReadAction
    public static int getIndentToThisElement(@Nonnull PsiElement element) {
        if (element instanceof YAMLBlockMappingImpl blockMapping) {
            try {
                element = blockMapping.getFirstKeyValue();
            }
            catch (IllegalStateException e) {
                // Spring Boot plug-in modifies PSI-tree into invalid state
                // This is a workaround over EA-133507 IDEA-210113
                if (!e.getMessage().equals(YAMLBlockMappingImpl.EMPTY_MAP_MESSAGE)) {
                    throw e;
                }
                else {
                    Logger.getInstance(YAMLUtil.class).error(YAMLBlockMappingImpl.EMPTY_MAP_MESSAGE);
                }
            }
        }
        int offset = element.getTextOffset();

        PsiElement currentElement = element;
        while (currentElement != null) {
            IElementType type = currentElement.getNode().getElementType();
            if (YAMLElementTypes.EOL_ELEMENTS.contains(type)) {
                return offset - currentElement.getTextOffset() - currentElement.getTextLength();
            }

            currentElement = PsiTreeUtil.prevLeaf(currentElement);
        }
        return offset;
    }

    @RequiredReadAction
    public static boolean psiAreAtTheSameLine(@Nonnull PsiElement psi1, @Nonnull PsiElement psi2) {
        PsiElement leaf = firstLeaf(psi1);
        PsiElement lastLeaf = firstLeaf(psi2);
        while (leaf != null) {
            if (PsiUtilCore.getElementType(leaf) == YAMLTokenTypes.EOL) {
                return false;
            }
            if (leaf == lastLeaf) {
                return true;
            }
            leaf = PsiTreeUtil.nextLeaf(leaf);
        }
        // It is a kind of magic, normally we should return from the `while` above
        return false;
    }

    private static @Nullable PsiElement firstLeaf(PsiElement psi1) {
        LeafElement leaf = TreeUtil.findFirstLeaf(psi1.getNode());
        if (leaf != null) {
            return leaf.getPsi();
        }
        else {
            return null;
        }
    }

    /**
     * Deletes surrounding whitespace contextually. First attempts to delete {@link YAMLTokenTypes#COMMENT}s on the same line and
     * {@link YAMLElementTypes#SPACE_ELEMENTS} forward, otherwise it will delete {@link YAMLElementTypes#SPACE_ELEMENTS} backward.
     * <p>
     * This is useful for maintaining consistent formatting.
     * <p>
     * E.g.,
     * <pre>{@code
     * foo:
     *   bar: value1 # Same line comment
     *   # Next line comment
     *   baz: value2
     * }</pre>
     * becomes
     * <pre>{@code
     * foo:
     *   bar: value1 # Next line comment
     *   baz: value2
     * }</pre>
     */
    @RequiredWriteAction
    public static void deleteSurroundingWhitespace(@Nonnull PsiElement element) {
        if (element.getNextSibling() != null) {
            deleteElementsOfType(element::getNextSibling, BLANK_LINE_ELEMENTS);
            deleteElementsOfType(element::getNextSibling, YAMLElementTypes.SPACE_ELEMENTS);
        }
        else {
            deleteElementsOfType(element::getPrevSibling, YAMLElementTypes.SPACE_ELEMENTS);
        }
    }

    @RequiredWriteAction
    private static void deleteElementsOfType(
        @Nonnull @RequiredReadAction Supplier<? extends PsiElement> element,
        @Nonnull TokenSet types
    ) {
        while (element.get() != null && types.contains(PsiUtilCore.getElementType(element.get()))) {
            element.get().delete();
        }
    }
}
