// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.formatter;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.codeStyle.*;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiUtilCore;
import jakarta.annotation.Nonnull;
import org.jetbrains.yaml.YAMLElementTypes;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.impl.YAMLBlockScalarImpl;

@ExtensionImpl
public class YAMLFormattingModelBuilder implements FormattingModelBuilder {
    @Override
    public @Nonnull FormattingModel createModel(@Nonnull FormattingContext formattingContext) {
        PsiFile file = formattingContext.getContainingFile();
        CodeStyleSettings settings = formattingContext.getCodeStyleSettings();
        Block rootBlock = createBlock(new YAMLFormattingContext(settings, file), formattingContext.getNode());
        return new DocumentBasedFormattingModel(rootBlock, settings, file);
    }

    public static @Nonnull Block createBlock(@Nonnull YAMLFormattingContext context,
                                             @Nonnull ASTNode node) {
        IElementType nodeType = PsiUtilCore.getElementType(node);
        if (YAMLElementTypes.BLOCK_SCALAR_ITEMS.contains(nodeType)) {
            ASTNode blockScalarNode = node.getTreeParent();
            assert (blockScalarNode.getPsi() instanceof YAMLBlockScalarImpl);
            YAMLBlockScalarImpl blockScalarImpl = (YAMLBlockScalarImpl) blockScalarNode.getPsi();

            if (blockScalarImpl.getNthContentTypeChild(0) != node) {
                // node is not block scalar header
                return YAMLBlockScalarItemBlock.createBlockScalarItem(context, node);
            }
        }

        assert nodeType != YAMLElementTypes.SEQUENCE : "Sequence should be inlined!";
        assert nodeType != YAMLElementTypes.MAPPING : "Mapping should be inlined!";
        assert nodeType != YAMLElementTypes.DOCUMENT : "Document should be inlined!";

        return new YAMLFormattingBlock(context, node);
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }
}
