package org.jetbrains.yaml;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
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
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.impl.YAMLQuotedTextImpl;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author traff
 */
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
        return project.getInstance(YAMLElementGenerator.class);
    }

    @Nonnull
    public static String createChainedKey(@Nonnull List<String> keyComponents, int indentAddition) {
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

    @Nonnull
    @RequiredReadAction
    @SuppressWarnings("unchecked")
    public YAMLKeyValue createYamlKeyValue(@Nonnull String keyName, @Nonnull String valueText) {
        final PsiFile tempFile = createDummyYamlWithText(keyName + ": " + valueText);
        return PsiTreeUtil.collectElementsOfType(tempFile, YAMLKeyValue.class).iterator().next();
    }

    @Nonnull
    @RequiredReadAction
    @SuppressWarnings("unchecked")
    public YAMLQuotedTextImpl createYamlDoubleQuotedString() {
        final YAMLFile tempFile = createDummyYamlWithText("\"foo\"");
        return PsiTreeUtil.collectElementsOfType(tempFile, YAMLQuotedTextImpl.class).iterator().next();
    }

    @Nonnull
    @RequiredReadAction
    public YAMLFile createDummyYamlWithText(@Nonnull String text) {
        return (YAMLFile)PsiFileFactory.getInstance(myProject).createFileFromText(
            "temp." + YAMLFileType.YML.getDefaultExtension(),
            YAMLFileType.YML,
            text,
            LocalTimeCounter.currentTime(),
            true
        );
    }

    @Nonnull
    @RequiredReadAction
    public PsiElement createEol() {
        final YAMLFile file = createDummyYamlWithText("\n");
        return PsiTreeUtil.getDeepestFirst(file);
    }

    @Nonnull
    @RequiredWriteAction
    public PsiElement createSpace() {
        final YAMLKeyValue keyValue = createYamlKeyValue("foo", "bar");
        final ASTNode whitespaceNode = keyValue.getNode().findChildByType(TokenType.WHITE_SPACE);
        assert whitespaceNode != null;
        return whitespaceNode.getPsi();
    }

    @Nonnull
    @RequiredReadAction
    public PsiElement createIndent(int size) {
        final YAMLFile file = createDummyYamlWithText(StringUtil.repeatSymbol(' ', size));
        return PsiTreeUtil.getDeepestFirst(file);
    }

    @Nonnull
    @RequiredReadAction
    public PsiElement createColon() {
        final YAMLFile file = createDummyYamlWithText("? foo : bar");
        final PsiElement at = file.findElementAt("? foo ".length());
        assert at != null && at.getNode().getElementType() == YAMLTokenTypes.COLON;
        return at;
    }
}
