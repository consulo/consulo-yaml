package org.jetbrains.yaml.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import consulo.annotation.access.RequiredReadAction;
import org.jetbrains.yaml.psi.*;
import org.jetbrains.yaml.psi.impl.YAMLArrayImpl;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;
import org.jetbrains.yaml.psi.impl.YAMLBlockSequenceImpl;
import org.jetbrains.yaml.psi.impl.YAMLHashImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oleg
 */
public class YAMLFoldingBuilder extends FoldingBuilderEx implements DumbAware
{
	private static final int PLACEHOLDER_LEN = 20;

	@RequiredReadAction
	@Nonnull
	@Override
	public FoldingDescriptor[] buildFoldRegions(@Nonnull PsiElement root, @Nonnull Document document, boolean quick)
	{
		List<FoldingDescriptor> descriptors = new ArrayList<>();
		collectDescriptors(root, descriptors);
		return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
	}

	@RequiredReadAction
	private static void collectDescriptors(@Nonnull final PsiElement element, @Nonnull final List<FoldingDescriptor> descriptors)
	{
		final TextRange nodeTextRange = element.getTextRange();
		if(nodeTextRange.getLength() < 2)
		{
			return;
		}

		if(element instanceof YAMLDocument)
		{
			int i = 0;
			PsiElement parent = element.getParent();
			for(PsiElement child = parent.getFirstChild(); child != null; child = child.getNextSibling())
			{
				if(child instanceof YAMLDocument)
				{
					i++;
					if(i > 1)
					{
						break;
					}
				}
			}

			if(i > 1)
			{
				descriptors.add(new FoldingDescriptor(element, nodeTextRange));
			}
		}
		else if(element instanceof YAMLScalar || element instanceof YAMLKeyValue && ((YAMLKeyValue) element).getValue() instanceof YAMLCompoundValue)
		{
			descriptors.add(new FoldingDescriptor(element, nodeTextRange));
		}

		for(PsiElement child : element.getChildren())
		{
			collectDescriptors(child, descriptors);
		}
	}

	@RequiredReadAction
	@Nullable
	public String getPlaceholderText(@Nonnull ASTNode node)
	{
		return getPlaceholderText(SourceTreeToPsiMap.treeElementToPsi(node));
	}

	@Nonnull
	private static String getPlaceholderText(@Nullable PsiElement psiElement)
	{
		if(psiElement instanceof YAMLDocument)
		{
			return "---";
		}
		else if(psiElement instanceof YAMLScalar)
		{
			return normalizePlaceHolderText(((YAMLScalar) psiElement).getTextValue());
		}
		else if(psiElement instanceof YAMLSequence)
		{
			final int size = ((YAMLSequence) psiElement).getItems().size();
			final String placeholder = size + " " + StringUtil.pluralize("item", size);
			if(psiElement instanceof YAMLArrayImpl)
			{
				return "[" + placeholder + "]";
			}
			else if(psiElement instanceof YAMLBlockSequenceImpl)
			{
				return "<" + placeholder + ">";
			}
		}
		else if(psiElement instanceof YAMLMapping)
		{
			final int size = ((YAMLMapping) psiElement).getKeyValues().size();
			final String placeholder = size + " " + StringUtil.pluralize("key", size);
			if(psiElement instanceof YAMLHashImpl)
			{
				return "{" + placeholder + "}";
			}
			else if(psiElement instanceof YAMLBlockMappingImpl)
			{
				return "<" + placeholder + ">";
			}
		}
		else if(psiElement instanceof YAMLKeyValue)
		{
			return normalizePlaceHolderText(((YAMLKeyValue) psiElement).getKeyText()) + ": " + getPlaceholderText(((YAMLKeyValue) psiElement).getValue());
		}
		return "...";
	}

	@RequiredReadAction
	public boolean isCollapsedByDefault(@Nonnull ASTNode node)
	{
		return false;
	}

	private static String normalizePlaceHolderText(@Nullable String text)
	{
		if(text == null)
		{
			return null;
		}

		if(text.length() <= PLACEHOLDER_LEN)
		{
			return text;
		}
		return StringUtil.trimMiddle(text, PLACEHOLDER_LEN);
	}
}
