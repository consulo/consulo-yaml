// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.yaml.schema;

import com.jetbrains.jsonSchema.impl.JsonSchemaByCommentProvider;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.pattern.StringPattern;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceContributor;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.psi.PsiReferenceRegistrar;
import consulo.language.util.ProcessingContext;
import org.jetbrains.yaml.YAMLLanguage;

@ExtensionImpl
public class YAMLJsonSchemaIdReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiComment().with(TopDocumentComment.INSTANCE),
            new PsiReferenceProvider() {
                @Override
                @RequiredReadAction
                public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
                    CharSequence charSequence = StringPattern.newBombedCharSequence(element.getNode().getChars());
                    TextRange inComment = JsonSchemaByCommentProvider.detectInComment(charSequence);
                    if (inComment == null) {
                        return PsiReference.EMPTY_ARRAY;
                    }
                    return new PsiReference[]{new JsonSchemaIdReference(element, inComment)};
                }
            }
        );
    }

    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }
}
