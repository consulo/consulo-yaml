// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.inspections;

import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.psi.YAMLQuotedText;

import java.util.Collection;

public class YAMLAddQuoteQuickFix implements LocalQuickFix {
    private final LocalizeValue quickFixText;
    private final boolean singleQuote;

    public YAMLAddQuoteQuickFix(@Nonnull LocalizeValue quickFixText) {
        this(quickFixText, false);
    }

    public YAMLAddQuoteQuickFix(@Nonnull LocalizeValue quickFixText, boolean singleQuote) {
        this.quickFixText = quickFixText;
        this.singleQuote = singleQuote;
    }

    @Nonnull
    @Override
    public LocalizeValue getName() {
        return quickFixText;
    }

    @Override
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
        PsiElement element = descriptor.getPsiElement();
        if (element != null) {
            wrapWithQuotes(element, singleQuote);
        }
    }

    public static void wrapWithQuotes(@Nonnull PsiElement startElement, boolean singleQuote) {
        char quote = singleQuote ? '\'' : '"';
        String text = "key: " + quote + startElement.getText() + quote;
        var tempFile = YAMLElementGenerator.getInstance(startElement.getProject()).createDummyYamlWithText(text);
        Collection<YAMLQuotedText> quotedTexts = PsiTreeUtil.collectElementsOfType(tempFile, YAMLQuotedText.class);
        if (quotedTexts.isEmpty()) return;
        YAMLQuotedText quoted = quotedTexts.iterator().next();
        startElement.replace(quoted);
    }
}
