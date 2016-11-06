package org.jetbrains.yaml.psi;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.impl.PsiTreeChangePreprocessorBase;

/**
 * @author oleg
 */
final class YAMLPsiManager extends PsiTreeChangePreprocessorBase
{
	public YAMLPsiManager(@NotNull Project project)
	{
		super(project);
	}

	@Override
	protected boolean isInsideCodeBlock(PsiElement element)
	{
		if(element instanceof PsiFileSystemItem)
		{
			return false;
		}

		if(element == null || element.getParent() == null)
		{
			return true;
		}

		while(true)
		{
			if(element instanceof YAMLFile)
			{
				return false;
			}
			if(element instanceof PsiFile || element instanceof PsiDirectory)
			{
				return true;
			}
			PsiElement parent = element.getParent();
			if(!(parent instanceof YAMLFile ||
					parent instanceof YAMLKeyValue ||
					parent instanceof YAMLCompoundValue ||
					parent instanceof YAMLDocument))
			{
				return true;
			}
			element = parent;
		}
	}

	@Override
	protected boolean isMyFile(@NotNull PsiFile psiFile)
	{
		return psiFile instanceof YAMLFile;
	}
}
