package consulo.yaml.navbar;

import com.intellij.ide.navigationToolbar.AbstractNavBarModelExtension;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import consulo.annotation.access.RequiredReadAction;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 17/01/2021
 */
public class YAMLNavBarExtension extends AbstractNavBarModelExtension
{
	private final static int SCALAR_MAX_LENGTH = 20;

	@Override
	public boolean normalizeChildren()
	{
		return false;
	}

	@Override
	@RequiredReadAction
	public PsiElement getLeafElement(@Nonnull DataContext dataContext)
	{
		if(UISettings.getInstance().getShowMembersInNavigationBar())
		{
			PsiFile psiFile = dataContext.getData(CommonDataKeys.PSI_FILE);
			Editor editor = dataContext.getData(CommonDataKeys.EDITOR);
			if(psiFile == null || editor == null)
			{
				return null;
			}
			PsiElement psiElement = psiFile.findElementAt(editor.getCaretModel().getOffset());
			if(psiElement != null && psiElement.getLanguage() instanceof YAMLLanguage)
			{
				return PsiTreeUtil.getParentOfType(psiElement, YAMLPsiElement.class);
			}
		}
		return null;
	}

	@Nullable
	@Override
	public PsiElement getParent(@Nonnull PsiElement psiElement)
	{
		if(psiElement instanceof YAMLPsiElement)
		{
			PsiElement parent = psiElement.getParent();
			if(parent instanceof YAMLMapping)
			{
				return parent.getParent();
			}
			return parent;
		}
		return super.getParent(psiElement);
	}

	@Nullable
	@Override
	public String getPresentableText(Object e)
	{
		if(e instanceof YAMLDocument)
		{
			final YAMLFile file = (YAMLFile) ((YAMLDocument) e).getContainingFile();
			if(file == null)
			{
				return "Document";
			}
			final List<YAMLDocument> documents = file.getDocuments();
			return "Document " + getIndexOf(documents, e);
		}
		if(e instanceof YAMLKeyValue)
		{
			return ((YAMLKeyValue) e).getKeyText() + ':';
		}
		if(e instanceof YAMLSequenceItem)
		{
			final PsiElement parent = ((YAMLSequenceItem) e).getParent();
			if(!(parent instanceof YAMLSequence))
			{
				return "Item";
			}
			final List<YAMLSequenceItem> items = ((YAMLSequence) parent).getItems();
			return "Item " + getIndexOf(items, e);
		}
		if(e instanceof YAMLScalar)
		{
			return StringUtil.first(((YAMLScalar) e).getTextValue(), SCALAR_MAX_LENGTH, true);
		}
		return null;
	}

	@Nonnull
	private static String getIndexOf(@Nonnull List<?> list, Object o)
	{
		return String.valueOf(1 + list.indexOf(o)) + '/' + list.size();
	}
}
