package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.ast.TokenSet;
import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.PsiFileBase;
import consulo.language.psi.PsiElement;
import org.jetbrains.yaml.YAMLElementTypes;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLPsiElement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oleg
 */
public class YAMLFileImpl extends PsiFileBase implements YAMLFile {
    public YAMLFileImpl(FileViewProvider viewProvider) {
        super(viewProvider, YAMLLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "YAML file";
    }

    public List<YAMLDocument> getDocuments() {
        final ArrayList<YAMLDocument> result = new ArrayList<>();
        for (ASTNode node : getNode().getChildren(TokenSet.create(YAMLElementTypes.DOCUMENT))) {
            result.add((YAMLDocument)node.getPsi());
        }
        return result;
    }

    public List<YAMLPsiElement> getYAMLElements() {
        final ArrayList<YAMLPsiElement> result = new ArrayList<>();
        for (ASTNode node : getNode().getChildren(null)) {
            final PsiElement psi = node.getPsi();
            if (psi instanceof YAMLPsiElement psiElement) {
                result.add(psiElement);
            }
        }
        return result;
    }
}
