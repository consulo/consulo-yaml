package org.jetbrains.yaml;

import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import javax.annotation.Nonnull;

/**
 * @author yole
 */
public class YAMLSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
  @Nonnull
  public SyntaxHighlighter getSyntaxHighlighter(final Project project, final VirtualFile virtualFile) {
    return new YAMLSyntaxHighlighter();
  }
}
