package org.jetbrains.yaml.psi;

import consulo.language.ast.ASTNode;
import consulo.language.psi.NavigatablePsiElement;
import consulo.language.psi.PsiElement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oleg
 */
public interface YAMLPsiElement extends NavigatablePsiElement {
    default List<YAMLPsiElement> getYAMLElements() {
        final ArrayList<YAMLPsiElement> result = new ArrayList<>();
        for (ASTNode node : getNode().getChildren(null)) {
            final PsiElement psi = node.getPsi();
            if (psi instanceof YAMLPsiElement) {
                result.add((YAMLPsiElement) psi);
            }
        }
        return result;
    }
}
