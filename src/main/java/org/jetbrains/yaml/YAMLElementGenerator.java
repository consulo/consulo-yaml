// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.language.ast.ASTNode;
import consulo.language.ast.TokenType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.util.lang.LocalTimeCounter;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.yaml.psi.*;
import org.jetbrains.yaml.psi.impl.YAMLQuotedTextImpl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
public class YAMLElementGenerator {
    private final Project myProject;

    @Inject
    public YAMLElementGenerator(Project project) {
        myProject = project;
    }

    public static YAMLElementGenerator getInstance(Project project) {
        return project.getService(YAMLElementGenerator.class);
    }

    public static @Nonnull String createChainedKey(@Nonnull List<String> keyComponents, int indentAddition) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyComponents.size(); ++i) {
            if (i > 0) {
                sb.append(StringUtil.repeatSymbol(' ', indentAddition + 2 * i));
            }
            sb.append(keyComponents.get(i)).append(":");
            if (i + 1 < keyComponents.size()) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    public YAMLKeyValue createYamlKeyValueWithSequence(@Nonnull String keyName, @Nonnull Map<String, String> elementsMap) {
        String yamlString = elementsMap
            .entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> "%s: %s".formatted(entry.getKey(), entry.getValue()))
            .collect(Collectors.joining("\n"));
        return createYamlKeyValue(keyName, yamlString);
    }

    public @Nonnull YAMLKeyValue createYamlKeyValue(@Nonnull String keyName, @Nonnull String valueText) {
        final PsiFile tempValueFile = createDummyYamlWithText(valueText);
        Collection<YAMLValue> values = PsiTreeUtil.collectElementsOfType(tempValueFile, YAMLValue.class);

        String text;
        if (values.isEmpty()) {
            text = keyName + ":";
        }
        else if (values.iterator().next() instanceof YAMLScalar && !valueText.contains("\n")) {
            text = keyName + ": " + valueText;
        }
        else {
            text = keyName + ":\n" + YAMLTextUtil.indentText(valueText, 2);
        }

        final PsiFile tempFile = createDummyYamlWithText(text);
        return PsiTreeUtil.collectElementsOfType(tempFile, YAMLKeyValue.class).iterator().next();
    }

    public @Nonnull YAMLQuotedTextImpl createYamlDoubleQuotedString() {
        final YAMLFile tempFile = createDummyYamlWithText("\"foo\"");
        return PsiTreeUtil.collectElementsOfType(tempFile, YAMLQuotedTextImpl.class).iterator().next();
    }

    public @Nonnull YAMLFile createDummyYamlWithText(@Nonnull String text) {
        return (YAMLFile) PsiFileFactory.getInstance(myProject)
            .createFileFromText("temp." + YAMLFileType.YML.getDefaultExtension(), YAMLFileType.YML, text, LocalTimeCounter.currentTime(), false);
    }

    public @Nonnull PsiElement createEol() {
        final YAMLFile file = createDummyYamlWithText("\n");
        return PsiTreeUtil.getDeepestFirst(file);
    }

    public @Nonnull PsiElement createSpace() {
        final YAMLKeyValue keyValue = createYamlKeyValue("foo", "bar");
        final ASTNode whitespaceNode = keyValue.getNode().findChildByType(TokenType.WHITE_SPACE);
        assert whitespaceNode != null;
        return whitespaceNode.getPsi();
    }

    public @Nonnull PsiElement createIndent(int size) {
        final YAMLFile file = createDummyYamlWithText(StringUtil.repeatSymbol(' ', size));
        return PsiTreeUtil.getDeepestFirst(file);
    }

    public @Nonnull PsiElement createColon() {
        final YAMLFile file = createDummyYamlWithText("? foo : bar");
        final PsiElement at = file.findElementAt("? foo ".length());
        assert at != null && at.getNode().getElementType() == YAMLTokenTypes.COLON;
        return at;
    }

    public @Nonnull PsiElement createComma() {
        final YAMLFile file = createDummyYamlWithText("[1,2]");
        final PsiElement comma = file.findElementAt("[1".length());
        assert comma != null && comma.getNode().getElementType() == YAMLTokenTypes.COMMA;
        return comma;
    }

    public @Nonnull PsiElement createDocumentMarker() {
        final YAMLFile file = createDummyYamlWithText("---");
        PsiElement at = file.findElementAt(0);
        assert at != null && at.getNode().getElementType() == YAMLTokenTypes.DOCUMENT_MARKER;
        return at;
    }

    public @Nonnull YAMLSequence createEmptySequence() {
        YAMLSequence sequence = PsiTreeUtil.findChildOfType(createDummyYamlWithText("- dummy"), YAMLSequence.class);
        assert sequence != null;
        sequence.deleteChildRange(sequence.getFirstChild(), sequence.getLastChild());
        return sequence;
    }

    public @Nonnull YAMLSequence createEmptyArray() {
        YAMLSequence sequence = PsiTreeUtil.findChildOfType(createDummyYamlWithText("[]"), YAMLSequence.class);
        assert sequence != null;
        return sequence;
    }

    public @Nonnull YAMLSequenceItem createEmptySequenceItem() {
        YAMLSequenceItem sequenceItem = PsiTreeUtil.findChildOfType(createDummyYamlWithText("- dummy"), YAMLSequenceItem.class);
        assert sequenceItem != null;
        YAMLValue value = sequenceItem.getValue();
        assert value != null;
        value.deleteChildRange(value.getFirstChild(), value.getLastChild());
        return sequenceItem;
    }

    public @Nonnull YAMLSequenceItem createSequenceItem(String text) {
        YAMLSequenceItem sequenceItem = PsiTreeUtil.findChildOfType(createDummyYamlWithText("- " + text), YAMLSequenceItem.class);
        assert sequenceItem != null;
        YAMLValue value = sequenceItem.getValue();
        assert value != null;
        return sequenceItem;
    }

    public @Nonnull YAMLSequenceItem createArrayItem(String text) {
        YAMLSequenceItem sequenceItem = PsiTreeUtil.findChildOfType(createDummyYamlWithText("[" + text + "]"), YAMLSequenceItem.class);
        assert sequenceItem != null;
        YAMLValue value = sequenceItem.getValue();
        assert value != null;
        return sequenceItem;
    }
}
