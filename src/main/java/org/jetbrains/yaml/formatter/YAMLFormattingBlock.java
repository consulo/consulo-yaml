// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.formatter;

import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.codeStyle.AbstractBlock;
import consulo.language.codeStyle.Block;
import consulo.language.codeStyle.Indent;
import consulo.language.codeStyle.Spacing;
import consulo.language.psi.PsiUtilCore;
import consulo.util.collection.SmartList;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.YAMLElementTypes;

import java.util.List;

class YAMLFormattingBlock extends AbstractBlock {
  private final @Nonnull YAMLFormattingContext myContext;
  private final @Nullable Indent myIndent;
  private final @Nullable Indent myNewChildIndent;

  private final boolean myIsIncomplete;

  private final @Nonnull TextRange myTextRange;

  YAMLFormattingBlock(@Nonnull YAMLFormattingContext context, @Nonnull ASTNode node) {
    super(node, null, context.computeAlignment(node));
    myContext = context;

    myIndent = myContext.computeBlockIndent(myNode);
    myIsIncomplete = myContext.isIncomplete(myNode);
    myNewChildIndent = myContext.computeNewChildIndent(myNode);
    myTextRange = myNode.getTextRange();
  }

  @Override
  public @Nullable Spacing getSpacing(@Nullable Block child1, @Nonnull Block child2) {
    return myContext.computeSpacing(this, child1, child2);
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public boolean isIncomplete() {
    return myIsIncomplete;
  }

  @Override
  public @Nullable Indent getIndent() {
    return myIndent;
  }

  @Override
  public @Nonnull TextRange getTextRange() {
    return myTextRange;
  }

  @Override
  protected @Nullable Indent getChildIndent() {
    return myNewChildIndent;
  }

  @Override
  protected List<Block> buildChildren() {
    return buildSubBlocks(myContext, myNode);
  }

  private @Nonnull List<Block> buildSubBlocks(@Nonnull YAMLFormattingContext context, @Nonnull ASTNode node) {
    List<Block> res = new SmartList<>();
    for (ASTNode subNode = node.getFirstChildNode(); subNode != null; subNode = subNode.getTreeNext()) {
      IElementType subNodeType = PsiUtilCore.getElementType(subNode);
      if (YAMLElementTypes.SPACE_ELEMENTS.contains(subNodeType)) {
        // just skip them (comment processed above)
      }
      else if (YAMLElementTypes.SCALAR_QUOTED_STRING == subNodeType) {
        res.addAll(buildSubBlocks(context, subNode));
      }
      else if (YAMLElementTypes.CONTAINERS.contains(subNodeType)) {
        res.addAll(YamlInjectedBlockFactory.substituteInjectedBlocks(
          context.mySettings,
          buildSubBlocks(context, subNode),
          subNode, getWrap(), context.computeAlignment(subNode)
        ));
      }
      else {
        res.add(YAMLFormattingModelBuilder.createBlock(context, subNode));
      }
    }
    return res;
  }
}
