package org.jetbrains.yaml.psi.impl;

import consulo.annotation.access.RequiredWriteAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.psi.AbstractElementManipulator;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.util.lang.Pair;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLScalar;

import javax.annotation.Nonnull;
import java.util.List;

@ExtensionImpl
public class YAMLScalarElementManipulator extends AbstractElementManipulator<YAMLScalarImpl> {
    @Nonnull
    @Override
    public TextRange getRangeInElement(@Nonnull YAMLScalarImpl element) {
        final List<TextRange> ranges = element.getContentRanges();
        if (ranges.isEmpty()) {
            return TextRange.EMPTY_RANGE;
        }

        return TextRange.create(ranges.get(0).getStartOffset(), ranges.get(ranges.size() - 1).getEndOffset());
    }

    @Nonnull
    @Override
    public Class<YAMLScalarImpl> getElementClass() {
        return YAMLScalarImpl.class;
    }

    @Override
    @RequiredWriteAction
    @SuppressWarnings("unchecked")
    public YAMLScalarImpl handleContentChange(@Nonnull YAMLScalarImpl element, @Nonnull TextRange range, String newContent)
        throws IncorrectOperationException {

        try {
            final List<Pair<TextRange, String>> encodeReplacements = element.getEncodeReplacements(newContent);
            final StringBuilder builder = new StringBuilder();
            final String oldText = element.getText();

            builder.append(oldText.subSequence(0, range.getStartOffset()));
            builder.append(YAMLScalarImpl.processReplacements(newContent, encodeReplacements));
            builder.append(oldText.subSequence(range.getEndOffset(), oldText.length()));

            final YAMLFile dummyYamlFile =
                YAMLElementGenerator.getInstance(element.getProject()).createDummyYamlWithText(builder.toString());
            final YAMLScalar newScalar = PsiTreeUtil.collectElementsOfType(dummyYamlFile, YAMLScalar.class).iterator().next();

            final PsiElement result = element.replace(newScalar);
            if (result instanceof YAMLScalarImpl scalar) {
                return scalar;
            }
            throw new AssertionError("Inserted YAML scalar, but it isn't a scalar after insertion :(");
        }
        catch (IllegalArgumentException e) {
            final PsiElement newElement =
                element.replace(YAMLElementGenerator.getInstance(element.getProject()).createYamlDoubleQuotedString());
            if (newElement instanceof YAMLQuotedTextImpl quotedText) {
                return handleContentChange(quotedText, newContent);
            }
            throw new AssertionError("Could not replace with dummy scalar");
        }
    }
}
