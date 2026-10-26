package org.jetbrains.yaml.smart;

import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorEx;
import consulo.dataContext.DataContext;
import consulo.document.Document;
import consulo.document.RangeMarker;
import consulo.language.editor.inject.InjectionMeta;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.language.psi.PsiManager;
import consulo.project.Project;
import consulo.util.dataholder.Key;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.YAMLLanguage;

public final class YamlIndentPreservationUtils {
    public static final Key<RangeMarker> INJECTION_RANGE_BEFORE_ENTER = Key.create("NEXT_ELEMENT");
    public static final Key<String> INDENT_BEFORE_PROCESSING = Key.create("INDENT_BEFORE_PROCESSING");

    private YamlIndentPreservationUtils() {
    }

    public static void preserveIndentStateBeforeProcessing(@Nonnull PsiFile file, @Nonnull DataContext dataContext) {
        VirtualFile virtualFile = file.getVirtualFile();
        if (!isVirtualFileWindow(virtualFile)) return;

        Editor editor = dataContext.getData(Editor.KEY);
        if (!(editor instanceof EditorEx hostEditor)) return;

        Project project = hostEditor.getProject();
        if (project == null) return;

        VirtualFile hostVirtualFile = hostEditor.getVirtualFile();
        if (hostVirtualFile == null) return;

        PsiFile hostFile = PsiManager.getInstance(project).findFile(hostVirtualFile);
        if (hostFile == null) return;

        if (!hostFile.getViewProvider().hasLanguage(YAMLLanguage.INSTANCE)) return;

        PsiLanguageInjectionHost injectionHost = InjectedLanguageManager.getInstance(file.getProject()).getInjectionHost(file);
        if (injectionHost == null) return;

        String lineIndent = InjectionMeta.getInjectionIndent().get(injectionHost);
        file.putUserData(INDENT_BEFORE_PROCESSING, lineIndent);

        Document document = hostEditor.getDocument();
        RangeMarker rangeMarker = document.createRangeMarker(injectionHost.getTextRange());
        file.putUserData(INJECTION_RANGE_BEFORE_ENTER, rangeMarker);
    }

    private static boolean isVirtualFileWindow(@Nullable VirtualFile virtualFile) {
        // Check if file is an injected virtual file window
        // VirtualFileWindow is typically marked or has specific characteristics
        return virtualFile != null && virtualFile.getClass().getSimpleName().contains("Window");
    }
}
