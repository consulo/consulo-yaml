package consulo.yaml.navbar;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.ui.navigationBar.StructureAwareNavBarModelExtension;
import consulo.language.psi.PsiElement;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.*;

import java.util.List;

/**
 * @author VISTALL
 * @since 17/01/2021
 */
@ExtensionImpl
public class YAMLNavBarExtension extends StructureAwareNavBarModelExtension {
    private final static int SCALAR_MAX_LENGTH = 20;

    @Override
    protected Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }

    @Nullable
    @Override
    public String getPresentableText(Object e) {
        if (e instanceof YAMLDocument) {
            final YAMLFile file = (YAMLFile) ((YAMLDocument) e).getContainingFile();
            if (file == null) {
                return "Document";
            }
            final List<YAMLDocument> documents = file.getDocuments();
            return "Document " + getIndexOf(documents, e);
        }
        if (e instanceof YAMLKeyValue) {
            return ((YAMLKeyValue) e).getKeyText() + ':';
        }
        if (e instanceof YAMLSequenceItem) {
            final PsiElement parent = ((YAMLSequenceItem) e).getParent();
            if (!(parent instanceof YAMLSequence)) {
                return "Item";
            }
            final List<YAMLSequenceItem> items = ((YAMLSequence) parent).getItems();
            return "Item " + getIndexOf(items, e);
        }
        if (e instanceof YAMLScalar) {
            return StringUtil.first(((YAMLScalar) e).getTextValue(), SCALAR_MAX_LENGTH, true);
        }
        return null;
    }

    @Nonnull
    private static String getIndexOf(@Nonnull List<?> list, Object o) {
        return String.valueOf(1 + list.indexOf(o)) + '/' + list.size();
    }
}
