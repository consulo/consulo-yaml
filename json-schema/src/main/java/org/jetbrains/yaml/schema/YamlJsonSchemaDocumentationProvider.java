// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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

import com.jetbrains.jsonSchema.JsonSchemaObject;
import com.jetbrains.jsonSchema.JsonSchemaService;
import com.jetbrains.jsonSchema.impl.JsonSchemaDocumentationProvider;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.Language;
import consulo.language.editor.documentation.DocumentationProviderEx;
import consulo.language.editor.documentation.LanguageDocumentationProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import org.jetbrains.yaml.YAMLLanguage;
import org.jspecify.annotations.Nullable;

@ExtensionImpl(id = "YamlJsonSchemaDocumentationProvider")
public class YamlJsonSchemaDocumentationProvider extends DocumentationProviderEx implements LanguageDocumentationProvider {
    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }

    @Override
    @RequiredReadAction
    public @Nullable String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        return findSchemaAndGenerateDoc(element, true);
    }

    @Override
    @RequiredReadAction
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        return findSchemaAndGenerateDoc(element, false);
    }

    @RequiredReadAction
    private static @Nullable String findSchemaAndGenerateDoc(PsiElement element, boolean preferShort) {
        JsonSchemaService jsonSchemaService = JsonSchemaService.get(element.getProject());
        PsiFile containingFile = element.getContainingFile();
        if (containingFile == null) {
            return null;
        }
        JsonSchemaObject schemaObject = jsonSchemaService.getSchemaObject(containingFile);
        if (schemaObject == null) {
            return null;
        }
        return JsonSchemaDocumentationProvider.generateDoc(element, schemaObject, preferShort, null);
    }

    @Override
    public @Nullable PsiElement getCustomDocumentationElement(Editor editor, PsiFile file, @Nullable PsiElement contextElement) {
        JsonSchemaService service = JsonSchemaService.get(file.getProject());
        if (service == null || service.getSchemaObject(file) == null) {
            return null;
        }
        return contextElement;
    }
}
