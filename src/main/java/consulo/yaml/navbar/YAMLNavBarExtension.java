package consulo.yaml.navbar;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ui.UISettings;
import consulo.codeEditor.Editor;
import consulo.dataContext.DataContext;
import consulo.ide.navigationToolbar.AbstractNavBarModelExtension;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.StringUtil;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 17/01/2021
 */
@ExtensionImpl
public class YAMLNavBarExtension extends AbstractNavBarModelExtension {
    private final static int SCALAR_MAX_LENGTH = 20;

    @Override
    public boolean normalizeChildren() {
        return false;
    }

    @Override
    @RequiredReadAction
    public PsiElement getLeafElement(@Nonnull DataContext dataContext) {
        if (UISettings.getInstance().getShowMembersInNavigationBar()) {
            PsiFile psiFile = dataContext.getData(PsiFile.KEY);
            Editor editor = dataContext.getData(Editor.KEY);
            if (psiFile == null || editor == null) {
                return null;
            }
            PsiElement psiElement = psiFile.findElementAt(editor.getCaretModel().getOffset());
            if (psiElement != null && psiElement.getLanguage() instanceof YAMLLanguage) {
                return PsiTreeUtil.getParentOfType(psiElement, YAMLPsiElement.class);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public PsiElement getParent(@Nonnull PsiElement psiElement) {
        if (psiElement instanceof YAMLPsiElement) {
            PsiElement parent = psiElement.getParent();
            if (parent instanceof YAMLMapping) {
                return parent.getParent();
            }
            return parent;
        }
        return super.getParent(psiElement);
    }

    @Nullable
    @Override
    public String getPresentableText(Object e) {
        if (e instanceof YAMLDocument) {
            final YAMLFile file = (YAMLFile)((YAMLDocument)e).getContainingFile();
            if (file == null) {
                return "Document";
            }
            final List<YAMLDocument> documents = file.getDocuments();
            return "Document " + getIndexOf(documents, e);
        }
        if (e instanceof YAMLKeyValue) {
            return ((YAMLKeyValue)e).getKeyText() + ':';
        }
        if (e instanceof YAMLSequenceItem) {
            final PsiElement parent = ((YAMLSequenceItem)e).getParent();
            if (!(parent instanceof YAMLSequence)) {
                return "Item";
            }
            final List<YAMLSequenceItem> items = ((YAMLSequence)parent).getItems();
            return "Item " + getIndexOf(items, e);
        }
        if (e instanceof YAMLScalar) {
            return StringUtil.first(((YAMLScalar)e).getTextValue(), SCALAR_MAX_LENGTH, true);
        }
        return null;
    }

    @Nonnull
    private static String getIndexOf(@Nonnull List<?> list, Object o) {
        return String.valueOf(1 + list.indexOf(o)) + '/' + list.size();
    }
}
