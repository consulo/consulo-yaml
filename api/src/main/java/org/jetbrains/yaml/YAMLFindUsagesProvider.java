// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.cacheBuilder.WordsScanner;
import consulo.language.findUsage.FindUsagesProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import consulo.util.lang.StringUtil;
import consulo.yaml.localize.YAMLLocalize;
import jakarta.annotation.Nonnull;
import org.jetbrains.yaml.psi.*;

/**
 * @author shalupov
 */
@ExtensionImpl
public class YAMLFindUsagesProvider implements FindUsagesProvider {
    @Override
    public @Nonnull WordsScanner getWordsScanner() {
        return new YAMLWordsScanner();
    }

    @Override
    public boolean canFindUsagesFor(@Nonnull PsiElement psiElement) {
        return psiElement instanceof PsiNamedElement || psiElement instanceof YAMLScalar;
    }

    @Override
    public @Nonnull String getType(@Nonnull PsiElement element) {
        if (element instanceof YAMLScalarText) {
            return YAMLLocalize.findUsagesScalar().get();
        }
        else if (element instanceof YAMLSequence) {
            return YAMLLocalize.findUsagesSequence().get();
        }
        else if (element instanceof YAMLMapping) {
            return YAMLLocalize.findUsagesMapping().get();
        }
        else if (element instanceof YAMLKeyValue) {
            return YAMLLocalize.findUsagesKeyValue().get();
        }
        else if (element instanceof YAMLDocument) {
            return YAMLLocalize.findUsagesDocument().get();
        }
        else {
            return "";
        }
    }

    @Override
    public @Nonnull String getDescriptiveName(@Nonnull PsiElement element) {
        if (element instanceof PsiNamedElement) {
            return StringUtil.notNullize(((PsiNamedElement) element).getName(), YAMLLocalize.findUsagesUnnamed().get());
        }

        if (element instanceof YAMLScalar) {
            return ((YAMLScalar) element).getTextValue();
        }

        return YAMLLocalize.findUsagesUnnamed().get();
    }

    @Override
    public @Nonnull String getNodeText(@Nonnull PsiElement element, boolean useFullName) {
        return getDescriptiveName(element);
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }
}