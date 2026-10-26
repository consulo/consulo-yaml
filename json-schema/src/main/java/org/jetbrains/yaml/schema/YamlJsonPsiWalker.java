// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.yaml.schema;

import com.intellij.json.pointer.JsonPointerPosition;
import com.jetbrains.jsonSchema.JsonSchemaType;
import com.jetbrains.jsonSchema.extension.adapter.JsonPropertyAdapter;
import com.jetbrains.jsonSchema.extension.adapter.JsonValueAdapter;
import com.jetbrains.jsonSchema.walker.JsonLikePsiWalker;
import com.jetbrains.jsonSchema.walker.JsonLikeSyntaxAdapter;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.codeStyle.CodeStyle;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.editor.completion.CompletionUtilCore;
import consulo.language.psi.LeafPsiElement;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.ThreeState;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLAnchor;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLPsiElement;
import org.jetbrains.yaml.psi.YAMLQuotedText;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLSequence;
import org.jetbrains.yaml.psi.YAMLSequenceItem;
import org.jetbrains.yaml.psi.YAMLValue;
import org.jetbrains.yaml.psi.YamlPsiUtil;
import org.jetbrains.yaml.psi.impl.YAMLArrayImpl;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;
import org.jetbrains.yaml.psi.impl.YAMLHashImpl;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class YamlJsonPsiWalker implements JsonLikePsiWalker {
    public static final YamlJsonPsiWalker INSTANCE = new YamlJsonPsiWalker();

    private YamlJsonPsiWalker() {
    }

    @Override
    public boolean isQuotedString(PsiElement element) {
        return element instanceof YAMLQuotedText;
    }

    @Override
    @RequiredReadAction
    public ThreeState isName(PsiElement element) {
        PsiElement parent = element.getParent();
        if (parent instanceof YAMLDocument || parent instanceof YAMLMapping) {
            return ThreeState.YES;
        }

        if (parent instanceof YAMLKeyValue && isFirstChild(element, parent)) {
            ASTNode prev = element.getNode().getTreePrev();
            return prev != null && prev.getElementType() == YAMLTokenTypes.INDENT ? ThreeState.YES : ThreeState.NO;
        }

        if (parent instanceof YAMLSequenceItem && isFirstChild(element, parent)) {
            return ThreeState.UNSURE;
        }
        return ThreeState.NO;
    }

    @RequiredReadAction
    private static boolean isFirstChild(PsiElement element, PsiElement parent) {
        PsiElement[] children = parent.getChildren();
        return children.length != 0 && children[0] == element;
    }

    @Override
    public boolean isPropertyWithValue(PsiElement element) {
        return element instanceof YAMLKeyValue && ((YAMLKeyValue) element).getValue() != null;
    }

    @Override
    public boolean isTopJsonElement(PsiElement element) {
        return element instanceof YAMLFile || element instanceof YAMLDocument;
    }

    @Override
    public boolean acceptsEmptyRoot() {
        return true;
    }

    @Override
    public @Nullable PsiElement findElementToCheck(PsiElement element) {
        PsiElement current = element;
        while (current != null && !(current instanceof PsiFile)) {
            // Typing inside a comment must not trigger schema-based completion or validation (IJPL-249582).
            if (current instanceof PsiComment) {
                return null;
            }
            if (current instanceof YAMLValue || current instanceof YAMLKeyValue) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }

    @Override
    public boolean requiresNameQuotes() {
        return false;
    }

    @Override
    @RequiredReadAction
    public @Nullable JsonValueAdapter createValueAdapter(PsiElement element) {
        if (element instanceof YAMLValue) {
            return YamlPropertyAdapter.createValueAdapterByType((YAMLValue) element);
        }
        if (element instanceof YAMLDocument) {
            if (element.getChildren().length == 0) {
                return new YamlEmptyObjectAdapter(element);
            }
            return createValueAdapter(Objects.requireNonNull(element.getFirstChild()));
        }
        if (element instanceof LeafPsiElement && PsiUtilCore.getElementType(element) == YAMLTokenTypes.INDENT) {
            return YamlPropertyAdapter.createEmptyValueAdapter(element, true);
        }
        if (element instanceof LeafPsiElement) {
            JsonPropertyAdapter parentPropertyAdapter = getParentPropertyAdapter(element);
            return parentPropertyAdapter == null ? null : parentPropertyAdapter.getNameValueAdapter();
        }
        return null;
    }

    @Override
    public boolean allowsSingleQuotes() {
        return true;
    }

    @Override
    public boolean hasMissingCommaAfter(PsiElement element) {
        return false;
    }

    @Override
    public @Nullable JsonPropertyAdapter getParentPropertyAdapter(PsiElement element) {
        YAMLMapping mapping = PsiTreeUtil.getParentOfType(element, YAMLMapping.class, true, YAMLKeyValue.class);
        if (mapping != null && (element instanceof YAMLScalar || element.getParent() instanceof YAMLScalar)) {
            // if we reach a mapping without reaching any key-value, this is a case like:
            // - foo: bar
            //   a
            // in such cases, we should create a property adapter for "a" for proper behavior of features
            return new YamlPropertyAdapter(Objects.requireNonNull(element.getParent()));
        }
        YAMLKeyValue property = PsiTreeUtil.getParentOfType(element, YAMLKeyValue.class, false);
        if (property == null) {
            return null;
        }
        // it is a parent property only if its value or its key contains the current property,
        //  or it is the property itself (we perform a non-strict parent search)
        YAMLValue value = property.getValue();
        boolean isSelf = element == property;
        PsiElement key = property.getKey();
        boolean isKey = key != null && PsiTreeUtil.isAncestor(key, element, false);
        // don't jump up from sequences
        if (!isSelf && !isKey && value instanceof YAMLSequence && value != element) {
            return null;
        }
        boolean isValue = value != null && PsiTreeUtil.isAncestor(value, element, false);
        if (!isKey && !isValue && !isSelf) {
            return null;
        }
        return new YamlPropertyAdapter(property);
    }

    @Override
    @RequiredReadAction
    public Set<String> getPropertyNamesOfParentObject(PsiElement originalPosition, PsiElement computedPosition) {
        YAMLMapping object = PsiTreeUtil.getParentOfType(originalPosition, YAMLMapping.class, false);
        YAMLMapping otherObject = PsiTreeUtil.getParentOfType(computedPosition, YAMLMapping.class, false);
        // the original position can be either a sound element or a whitespace; whitespaces can belong to the parent
        if (object == null || otherObject != null
            && PsiTreeUtil.isAncestor(CompletionUtilCore.getOriginalOrSelf(object),
            CompletionUtilCore.getOriginalOrSelf(otherObject), true)) {
            object = otherObject;
        }
        if (object == null) {
            return Collections.emptySet();
        }

        int mappingIndent = indentOf(object);
        int caretIndent = indentOf(computedPosition);

        // Find the first property to determine the "baseline" indent for properties in this mapping
        List<YAMLKeyValue> keyValues = object.getKeyValues().stream().toList();
        if (!keyValues.isEmpty()) {
            int firstPropIndent = indentOf(keyValues.getFirst());
            if (caretIndent > firstPropIndent) {
                // Caret is deeper than existing properties in this mapping -> it's a new nested object
                return Collections.emptySet();
            }
        }
        else {
            // Mapping is empty, check against the mapping's own indent
            // Usually, nested mapping keys are indented relative to the parent key.
            if (caretIndent > mappingIndent) {
                // Logically a new level
                return Collections.emptySet();
            }
        }

        // If we are at the same level as the mapping's properties, return the names to prevent duplicates
        return new YamlObjectAdapter(object).getPropertyList().stream()
            .map(p -> p.getName())
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    @Override
    @RequiredReadAction
    public int indentOf(PsiElement element) {
        return YAMLUtil.getIndentToThisElement(element);
    }

    @Override
    public int indentOf(PsiFile file) {
        return CodeStyle.getSettings(file).getIndentOptionsByFile(file).INDENT_SIZE;
    }

    @Override
    public @Nullable JsonPointerPosition findPosition(PsiElement element, boolean forceLastTransition) {
        JsonPointerPosition pos = new JsonPointerPosition();
        PsiElement current = element;
        while (!breakCondition(current)) {
            PsiElement position = current;
            current = current.getParent();
            if (current instanceof YAMLSequence array) {
                List<YAMLSequenceItem> expressions = array.getItems();
                int idx = -1;
                for (int i = 0; i < expressions.size(); i++) {
                    YAMLSequenceItem value = expressions.get(i);
                    if (position.equals(value)) {
                        idx = i;
                        break;
                    }
                }
                if (idx != -1) {
                    pos.addPrecedingStep(idx);
                }
            }
            else if (current instanceof YAMLSequenceItem) {
                // do nothing - handled by the upper condition
            }
            else if (current instanceof YAMLKeyValue) {
                String propertyName = StringUtil.notNullize(((YAMLKeyValue) current).getName());
                current = current.getParent();
                if (!(current instanceof YAMLMapping)) {
                    return null;//incorrect syntax?
                }
                pos.addPrecedingStep(propertyName);
            }
            else if (current instanceof YAMLMapping && position instanceof YAMLKeyValue) {
                // if either value or not first in the chain - needed for completion variant
                String propertyName = StringUtil.notNullize(((YAMLKeyValue) position).getName());
                if (propertyName.contains(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED)) {
                    continue;
                }
                if (position != element || forceLastTransition) {
                    pos.addPrecedingStep(propertyName);
                }
            }
            else if (breakCondition(current)) {
                break;
            }
            else {
                if (current instanceof YAMLMapping) {
                    List<YAMLPsiElement> elements = ((YAMLMapping) current).getYAMLElements();
                    if (elements.isEmpty()) {
                        return null;
                    }
                    if (position instanceof YAMLPsiElement && elements.contains(position)) {
                        continue;
                    }
                }
                return null;//something went wrong
            }
        }
        return pos;
    }

    private static boolean breakCondition(@Nullable PsiElement current) {
        return current instanceof PsiFile || current instanceof YAMLDocument ||
            current instanceof YAMLBlockMappingImpl && current.getParent() instanceof YAMLDocument;
    }

    @Override
    public boolean requiresValueQuotes() {
        return false;
    }

    @Override
    public String getDefaultObjectValue() {
        return "";
    }

    @Override
    public String getDefaultArrayValue() {
        return "- ";
    }

    @Override
    public boolean hasWhitespaceDelimitedCodeBlocks() {
        return true;
    }

    @Override
    @RequiredReadAction
    public String getNodeTextForValidation(PsiElement element) {
        String text = element.getText();
        if (!StringUtil.startsWith(text, "!!") && !StringUtil.startsWithChar(text, '&')) {
            return text;
        }
        // remove tags
        int spaceIndex = text.indexOf(' ');
        return spaceIndex > 0 ? text.substring(spaceIndex + 1) : text;
    }

    @Override
    @RequiredReadAction
    public TextRange adjustErrorHighlightingRange(PsiElement element) {
        YAMLAnchor[] anchors = PsiTreeUtil.getChildrenOfType(element, YAMLAnchor.class);
        if (anchors != null && anchors.length > 0) {
            YAMLAnchor lastAnchor = anchors[anchors.length - 1];
            PsiElement next = PsiTreeUtil.skipWhitespacesAndCommentsForward(lastAnchor);
            return next == null ? element.getTextRange() : next.getTextRange();
        }
        PsiElement parent = element.getParent();
        if (parent instanceof YAMLDocument) {
            PsiElement firstYamlChild = element.getFirstChild();
            while (firstYamlChild != null && !(firstYamlChild instanceof YAMLPsiElement)) {
                firstYamlChild = firstYamlChild.getNextSibling();
            }
            return firstYamlChild == null ? element.getTextRange() : firstYamlChild.getTextRange();
        }
        return element.getTextRange();
    }

    @Override
    public JsonLikeSyntaxAdapter getSyntaxAdapter(Project project) {
        return YamlJsonLikeSyntaxAdapter.INSTANCE;
    }

    @Override
    public @Nullable PsiElement getParentContainer(PsiElement element) {
        return PsiTreeUtil.getParentOfType(PsiTreeUtil.getParentOfType(element, YAMLKeyValue.class),
            YAMLMapping.class, YAMLSequence.class);
    }

    @Override
    public Collection<PsiElement> getRoots(PsiFile file) {
        if (!(file instanceof YAMLFile)) {
            return List.of();
        }
        Collection<PsiElement> roots = new HashSet<>();
        for (YAMLDocument document : ((YAMLFile) file).getDocuments()) {
            YAMLValue topLevelValue = document.getTopLevelValue();
            roots.add(topLevelValue == null ? document : topLevelValue);
        }
        return roots;
    }

    @Override
    @Deprecated(forRemoval = true)
    public boolean requiresReformatAfterArrayInsertion() {
        return false;
    }

    @Override
    public @Nullable PsiElement getPropertyNameElement(@Nullable PsiElement property) {
        return property instanceof YAMLKeyValue ? ((YAMLKeyValue) property).getKey() : null;
    }

    private static class YamlJsonLikeSyntaxAdapter implements JsonLikeSyntaxAdapter {
        private static final YamlJsonLikeSyntaxAdapter INSTANCE = new YamlJsonLikeSyntaxAdapter();

        @Override
        @RequiredWriteAction
        public PsiElement adjustValue(PsiElement value) {
            if (!(value instanceof YAMLValue)) {
                YAMLKeyValue keyValue = findPrecedingKeyValueWithNoValue(value);
                if (keyValue == null) {
                    keyValue = ObjectUtil.tryCast(value.getParent(), YAMLKeyValue.class);
                }
                if (keyValue != null) {
                    YAMLValue adjustedValue = keyValue.getValue();
                    if (adjustedValue != null) {
                        return adjustedValue;
                    }
                    YAMLElementGenerator generator = YAMLElementGenerator.getInstance(keyValue.getProject());
                    YAMLValue newValue = Objects.requireNonNull(generator.createYamlKeyValue("a", "\"\"").getValue());
                    return keyValue.add(newValue);
                }
                return value;
            }
            YAMLAnchor[] anchors = PsiTreeUtil.getChildrenOfType(value, YAMLAnchor.class);
            if (anchors == null || anchors.length == 0) {
                return value;
            }
            PsiElement next = PsiTreeUtil.skipWhitespacesAndCommentsForward(anchors[anchors.length - 1]);
            return next == null ? value : next;
        }

        private static @Nullable YAMLKeyValue findPrecedingKeyValueWithNoValue(PsiElement element) {
            PsiElement prev = PsiUtilCore.getElementType(element) == YAMLTokenTypes.INDENT ? element.getPrevSibling() : element;
            prev = PsiTreeUtil.skipWhitespacesAndCommentsBackward(prev);
            if (prev instanceof YAMLKeyValue keyValue && keyValue.getValue() == null) {
                return keyValue;
            }
            return null;
        }

        @Override
        public PsiElement createProperty(String name, String value, Project project) {
            YAMLElementGenerator generator = YAMLElementGenerator.getInstance(project);
            return generator.createYamlKeyValue(name, StringUtil.unquoteString(value));
        }

        @Override
        public PsiElement createEmptyArray(Project project, boolean preferInline) {
            YAMLElementGenerator generator = YAMLElementGenerator.getInstance(project);
            return preferInline ? generator.createEmptyArray() : generator.createEmptySequence();
        }

        private static @Nullable PsiElement skipWsBackward(@Nullable PsiElement item) {
            PsiElement current = item;
            while (current instanceof PsiWhiteSpace || current instanceof PsiComment) {
                current = PsiTreeUtil.prevLeaf(current);
            }
            return current;
        }

        private static @Nullable PsiElement skipWsForward(@Nullable PsiElement item) {
            PsiElement current = item;
            while (current instanceof PsiWhiteSpace || current instanceof PsiComment) {
                current = PsiTreeUtil.nextLeaf(current);
            }
            return current;
        }

        @Override
        @RequiredWriteAction
        public void removeArrayItem(PsiElement item) {
            PsiElement parent = item instanceof YAMLSequenceItem ? item : item.getParent();
            if (parent instanceof YAMLSequenceItem) {
                PsiElement grandParent = parent.getParent();
                PsiElement prev = skipWsBackward(PsiTreeUtil.prevLeaf(parent));
                PsiElement next = skipWsForward(PsiTreeUtil.nextLeaf(parent));
                parent.delete();
                if (grandParent instanceof YAMLArrayImpl && prev != null && isLeafOfType(prev, YAMLTokenTypes.COMMA)) {
                    prev.delete();
                }
                else if (grandParent instanceof YAMLArrayImpl && next != null && isLeafOfType(next, YAMLTokenTypes.COMMA)) {
                    next.delete();
                }
                if (!(grandParent instanceof YAMLArrayImpl) && prev != null && isLeafOfType(prev, YAMLTokenTypes.EOL)) {
                    prev.delete();
                }
                else if (!(grandParent instanceof YAMLArrayImpl) && next != null && isLeafOfType(next, YAMLTokenTypes.EOL)) {
                    next.delete();
                }
            }
            else {
                throw new IllegalArgumentException("Cannot remove item from a non-sequence element");
            }
        }

        private static boolean isLeafOfType(@Nullable PsiElement element, IElementType type) {
            return element instanceof LeafPsiElement && PsiUtilCore.getElementType(element) == type;
        }

        @Override
        @RequiredWriteAction
        public PsiElement addArrayItem(PsiElement array, String itemValue) {
            if (array instanceof YAMLArrayImpl) {
                return addInlineArrayItem((YAMLSequence) array, itemValue);
            }
            else if (array instanceof YAMLSequence) {
                return addSequenceItem((YAMLSequence) array, itemValue);
            }
            else {
                throw new IllegalArgumentException("Cannot add item to a non-sequence element");
            }
        }

        @RequiredWriteAction
        private static PsiElement addInlineArrayItem(YAMLSequence array, String itemValue) {
            YAMLElementGenerator generator = YAMLElementGenerator.getInstance(array.getProject());
            YAMLSequenceItem sequenceItem = generator.createArrayItem(itemValue);

            PsiElement addedItem = array.addBefore(sequenceItem, array.getLastChild()); // we insert before closing bracket ']'
            if (!(addedItem.getPrevSibling() instanceof PsiWhiteSpace)) {
                array.addBefore(generator.createSpace(), addedItem);
            }
            if (array.getItems().size() > 1) {
                array.addAfter(generator.createComma(), PsiTreeUtil.skipWhitespacesAndCommentsBackward(addedItem));
            }
            array.addAfter(generator.createSpace(), addedItem);
            return addedItem;
        }

        @RequiredWriteAction
        private static PsiElement addSequenceItem(YAMLSequence sequence, String itemValue) {
            YAMLElementGenerator generator = YAMLElementGenerator.getInstance(sequence.getProject());
            YAMLSequenceItem sequenceItem = generator.createSequenceItem(itemValue);

            PsiElement lastChild = sequence.getLastChild();
            if (lastChild != null && lastChild.getNode().getElementType() != YAMLTokenTypes.EOL) {
                sequence.add(generator.createEol());
            }
            List<YAMLSequenceItem> items = sequence.getItems();
            YAMLSequenceItem lastItem = items.isEmpty() ? null : items.get(items.size() - 1);

            int indent = lastChild != null && lastItem != null
                ? YAMLUtil.getIndentToThisElement(lastItem)
                : YAMLUtil.getIndentToThisElement(sequence) + 2;
            sequence.add(generator.createIndent(indent));
            return sequence.add(sequenceItem);
        }

        @Override
        @RequiredWriteAction
        public void ensureComma(@Nullable PsiElement self, @Nullable PsiElement newElement) {
            if (newElement instanceof YAMLKeyValue && self instanceof YAMLKeyValue) {
                PsiElement sibling = skipSiblingsForward(self);
                if (sibling != null && sibling.getText().equals("\n")) {
                    return;
                }
                PsiElement parent = Objects.requireNonNull(self.getParent());
                parent.addAfter(generateSeparator(parent), self);
                // two EOLs at the top level after a compound value
                if (parent.getParent() instanceof YAMLDocument && isCompoundValue(((YAMLKeyValue) self).getValue())) {
                    parent.addAfter(generateSeparator(parent), self);
                }
            }
        }

        private static boolean isCompoundValue(@Nullable YAMLValue value) {
            return value instanceof YAMLMapping || value instanceof YAMLSequence;
        }

        @SafeVarargs
        @RequiredReadAction
        private static @Nullable PsiElement skipSiblingsForward(@Nullable PsiElement element, Class<? extends PsiElement>... elementClasses) {
            if (element != null) {
                for (PsiElement e = element.getNextSibling(); e != null; e = e.getNextSibling()) {
                    if (!(e instanceof PsiComment) && (!(e instanceof PsiWhiteSpace) || e.textContains('\n'))) {
                        return e;
                    }
                }
            }
            return null;
        }

        private static PsiElement generateSeparator(PsiElement parent) {
            YAMLElementGenerator generator = YAMLElementGenerator.getInstance(parent.getProject());
            if (parent instanceof YAMLHashImpl) {
                return generator.createComma();
            }
            else {
                return generator.createEol();
            }
        }

        @Override
        @RequiredWriteAction
        public void removeIfComma(@Nullable PsiElement forward) {
            if (forward != null && isLeafOfType(forward, YAMLTokenTypes.EOL)) {
                PsiElement nextSibling;
                while ((nextSibling = forward.getNextSibling()) != null && isLeafOfType(nextSibling, YAMLTokenTypes.INDENT)) {
                    nextSibling.delete();
                }
                forward.delete();
            }
        }

        @Override
        public boolean fixWhitespaceBefore(@Nullable PsiElement initialElement, @Nullable PsiElement element) {
            return initialElement instanceof YAMLValue && initialElement != element;
        }

        @Override
        public String getDefaultValueFromType(@Nullable JsonSchemaType type) {
            if (type == null) {
                return "";
            }
            if (type == JsonSchemaType._object) {
                return " ";
            }
            if (type == JsonSchemaType._array) {
                return " - ";
            }
            return type.getDefaultValue();
        }

        @Override
        public PsiElement adjustNewProperty(PsiElement element) {
            if (element instanceof YAMLMapping) {
                return Objects.requireNonNull(element.getFirstChild());
            }
            return element;
        }

        @Override
        @RequiredWriteAction
        public PsiElement adjustPropertyAnchor(LeafPsiElement element) {
            YAMLElementGenerator generator = YAMLElementGenerator.getInstance(element.getProject());
            YAMLKeyValue keyValue = findPrecedingKeyValueWithNoValue(element);
            assert keyValue != null : "Should come here only for YAMLKeyValue with no value and a following indent";
            PsiComment nextComment = ObjectUtil.tryCast(skipNonNewlineSpaces(keyValue), PsiComment.class);
            if (nextComment != null) {
                keyValue.addBefore(generator.createSpace(), null);
                keyValue.addBefore(nextComment.copy(), null);
            }
            keyValue.addBefore(generator.createEol(), null);
            keyValue.addBefore(generator.createIndent(element.getTextLength()), null);
            PsiElement prev = element.getPrevSibling();
            if (prev != null) {
                prev.delete();
            }
            element.delete();
            if (nextComment != null) {
                nextComment.delete();
            }
            return keyValue;
        }

        @RequiredReadAction
        private static @Nullable PsiElement skipNonNewlineSpaces(YAMLKeyValue keyValue) {
            PsiElement sibling = keyValue.getNextSibling();
            while (sibling instanceof PsiWhiteSpace && !sibling.getText().contains("\n")) {
                sibling = sibling.getNextSibling();
            }
            return sibling;
        }

        @Override
        @RequiredWriteAction
        public PsiElement addProperty(PsiElement contextForInsertion, PsiElement newProperty) {
            // Sometimes, post-write-action formatting can break the YAML structure if the area was not indented properly initially.
            // This is why we pre-format it to avoid problems.
            preFormatAround(contextForInsertion);

            return JsonLikeSyntaxAdapter.super.addProperty(contextForInsertion, newProperty);
        }

        @RequiredWriteAction
        private static void preFormatAround(PsiElement element) {
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(element.getProject());
            Document document = documentManager.getDocument(Objects.requireNonNull(element.getContainingFile()));
            if (document == null) {
                return; // nothing to format if there is no document anyway
            }
            // We need to commit the pending PSI changes before triggering the formatting, otherwise it will fail.
            // This typically happens if we have several calls to addProperty in a row, but could be with any previous PSI change too.
            documentManager.doPostponedOperationsAndUnblockDocument(document);

            // If we try to format an element that is itself indented, the formatter will not take this base indent into account.
            // This is why we need to go up the tree to find the top-level Key-Value that contains our element.
            PsiElement elementToFormat = YamlPsiUtil.findClosestAncestorWithoutIndent(document, element);

            // The formatter doesn't support formatting YAMLDocument or YAMLMapping elements, but if we reach one of those, they represent the
            // whole file anyway (because they must have an indent of 0), so we can trigger the formatting on the containing file.
            if (elementToFormat instanceof YAMLDocument || elementToFormat instanceof YAMLMapping) {
                elementToFormat = elementToFormat.getContainingFile();
            }
            CodeStyleManager.getInstance(element.getProject()).reformat(elementToFormat, true);
        }
    }
}
