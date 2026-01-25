// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.formatter;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationManager;
import consulo.codeEditor.Editor;
import consulo.codeEditor.action.RawTypedActionHandler;
import consulo.codeEditor.action.TypedAction;
import consulo.document.Document;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.editor.action.TypedHandlerDelegate;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiUtilCore;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import org.jetbrains.yaml.YAMLElementTypes;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLSequence;

@ExtensionImpl
public class YAMLHyphenTypedHandler extends TypedHandlerDelegate {
    @Override
    public @Nonnull Result charTyped(char c, @Nonnull Project project, @Nonnull Editor editor, @Nonnull PsiFile file) {
        autoIndentHyphen(c, project, editor, file);
        return Result.CONTINUE;
    }

    private static void autoIndentHyphen(char c,
                                         @Nonnull Project project,
                                         @Nonnull Editor editor,
                                         @Nonnull PsiFile file) {
        if (!(c == ' ' && file instanceof YAMLFile)) {
            return;
        }
        if (!file.isValid()) {
            return;
        }

        int curPosOffset = editor.getCaretModel().getOffset();
        if (curPosOffset < 2) {
            return;
        }

        int offset = curPosOffset - 2;
        Document document = editor.getDocument();

        if (document.getCharsSequence().charAt(offset) != '-') {
            return;
        }

        if (curPosOffset < document.getTextLength() && document.getCharsSequence().charAt(curPosOffset) != '\n') {
            return;
        }

        PsiDocumentManager.getInstance(project).commitDocument(document);

        PsiElement element = file.findElementAt(offset);
        if (PsiUtilCore.getElementType(element) != YAMLTokenTypes.SEQUENCE_MARKER) {
            return;
        }

        PsiElement item = element.getParent();
        if (PsiUtilCore.getElementType(item) != YAMLElementTypes.SEQUENCE_ITEM) {
            // Should not be possible now
            return;
        }

        PsiElement sequence = item.getParent();
        if (PsiUtilCore.getElementType(sequence) != YAMLElementTypes.SEQUENCE) {
            // It could be some composite component (with syntax error)
            return;
        }

        if (((YAMLSequence) sequence).getItems().size() != 1) {
            return;
        }

        RawTypedActionHandler handler = TypedAction.getInstance().getDefaultRawTypedHandler();
        handler.beginUndoablePostProcessing();

        ApplicationManager.getApplication().runWriteAction(() -> {
            int newOffset = CodeStyleManager.getInstance(project).adjustLineIndent(file, offset);
            editor.getCaretModel().moveToOffset(newOffset + 2);
        });
    }
}
