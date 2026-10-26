// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.smart;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.codeEditor.action.EditorActionHandler;
import consulo.dataContext.DataContext;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenType;
import consulo.language.codeStyle.CodeStyle;
import consulo.language.editor.action.EditorBackspaceUtil;
import consulo.language.editor.action.EnterHandlerDelegate;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.ref.SimpleReference;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.Contract;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.YAMLElementTypes;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.formatter.YAMLCodeStyleSettings;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLSequenceItem;

import java.util.Objects;

@ExtensionImpl
public class YAMLEnterAtIndentHandler implements EnterHandlerDelegate {
    @Override
    public Result preprocessEnter(@Nonnull PsiFile file,
                                  @Nonnull Editor editor,
                                  @Nonnull SimpleReference<Integer> caretOffset,
                                  @Nonnull SimpleReference<Integer> caretAdvance,
                                  @Nonnull DataContext dataContext,
                                  EditorActionHandler originalHandler) {
        // this call is not related to YAMLEnterAtIndentHandler, but is needed for `YAMLInjectedElementEnterHandler`
        // this call is placed here to avoid creating another `EnterHandlerDelegate` with `order="first"`
        YamlIndentPreservationUtils.preserveIndentStateBeforeProcessing(file, dataContext);

        if (!(file instanceof YAMLFile)) {
            return Result.Continue;
        }

        if (caretOffset.get() > 0 && shouldInsertAutomaticHyphen(file)) {
            PsiElement element = file.findElementAt(caretOffset.get() - 1);
            if (PsiUtilCore.getElementType(element) == TokenType.WHITE_SPACE &&
                element.getTextLength() == 1 &&
                PsiUtilCore.getElementType(PsiTreeUtil.prevLeaf(element)) == YAMLTokenTypes.SEQUENCE_MARKER) {
                int indentSize = Objects.requireNonNull(CodeStyle.getLanguageSettings(file, YAMLLanguage.INSTANCE).getIndentOptions()).INDENT_SIZE;
                editor.getDocument().replaceString(caretOffset.get() - 2, caretOffset.get(), StringUtil.repeat(" ", indentSize));
                return Result.Stop;
            }
        }

        // honor dedent (RUBY-21803)
        // The solutions is copied from com.jetbrains.python.editor.PyEnterAtIndentHandler
        if (EditorBackspaceUtil.isWhitespaceBeforeCaret(editor)) {
            return Result.DefaultSkipIndent;
        }
        return Result.Continue;
    }

    @Override
    public Result postProcessEnter(@Nonnull PsiFile file, @Nonnull Editor editor, @Nonnull DataContext dataContext) {
        if (!(file instanceof YAMLFile) || !file.isValid()) {
            return Result.Continue;
        }
        if (!shouldInsertAutomaticHyphen(file)) {
            return Result.Continue;
        }

        int caretOffset = editor.getCaretModel().getOffset();

        if (caretOffset <= 0) {
            // Actually it is magic situation
            return Result.Continue;
        }

        PsiElement element = file.findElementAt(caretOffset - 1);
        if (!YAMLElementTypes.BLANK_ELEMENTS.contains(PsiUtilCore.getElementType(element))) {
            return Result.Continue;
        }
        PsiElement parent = element.getParent();
        IElementType parentType = PsiUtilCore.getElementType(parent);
        if (parentType != YAMLElementTypes.SEQUENCE) {
            if (parentType != YAMLElementTypes.MAPPING) {
                return Result.Continue;
            }
            PsiElement prevElem = element.getPrevSibling();
            while (YAMLElementTypes.BLANK_ELEMENTS.contains(PsiUtilCore.getElementType(prevElem))) {
                prevElem = prevElem.getPrevSibling();
            }
            if (PsiUtilCore.getElementType(prevElem) != YAMLElementTypes.KEY_VALUE_PAIR) {
                return Result.Continue;
            }
            if (PsiUtilCore.getElementType(prevElem.getLastChild()) != YAMLElementTypes.SEQUENCE) {
                return Result.Continue;
            }
            if (YAMLUtil.getIndentToThisElement(prevElem.getLastChild()) != YAMLUtil.getIndentToThisElement(parent)) {
                return Result.Continue;
            }
        }
        else {
            // don't insert a second '-' before already existing '-'
            PsiElement prevSibling = element.getPrevSibling();
            if (isEmptySequenceItem(prevSibling)) {
                return Result.Continue;
            }
            while (YAMLElementTypes.BLANK_ELEMENTS.contains(PsiUtilCore.getElementType(prevSibling))) {
                prevSibling = prevSibling.getPrevSibling();
            }
            if (PsiUtilCore.getElementType(prevSibling) == YAMLElementTypes.SEQUENCE_ITEM &&
                PsiUtilCore.getElementType(prevSibling.getLastChild()) == YAMLElementTypes.MAPPING) {
                return Result.Continue;
            }
        }
        editor.getDocument().insertString(caretOffset, "- ");
        editor.getCaretModel().moveToOffset(caretOffset + 2);

        return Result.Stop;
    }

    @Contract("null -> false")
    private static boolean isEmptySequenceItem(@Nullable PsiElement prevSibling) {
        return prevSibling instanceof YAMLSequenceItem && "-".equals(prevSibling.getText());
    }

    private static boolean shouldInsertAutomaticHyphen(@Nonnull PsiFile file) {
        return CodeStyle.getCustomSettings(file, YAMLCodeStyleSettings.class).AUTOINSERT_SEQUENCE_MARKER;
    }
}
