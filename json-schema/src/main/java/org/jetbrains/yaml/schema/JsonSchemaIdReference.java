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

import com.jetbrains.jsonSchema.JsonSchemaService;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaInfo;
import consulo.annotation.access.RequiredReadAction;
import consulo.document.util.TextRange;
import consulo.json.icon.JsonIconGroup;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceBase;
import consulo.project.Project;

import java.util.ArrayList;
import java.util.List;

public class JsonSchemaIdReference extends PsiReferenceBase.Immediate<PsiElement> {
    public JsonSchemaIdReference(PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement, true, element);
    }

    @Override
    @RequiredReadAction
    public Object[] getVariants() {
        Project project = getElement().getProject();
        JsonSchemaService schemaService = JsonSchemaService.get(project);
        List<Object> variants = new ArrayList<>();
        for (JsonSchemaInfo jsonSchemaInfo : schemaService.getAllUserVisibleSchemas()) {
            JsonSchemaFileProvider provider = jsonSchemaInfo.getProvider();
            if (provider == null || provider.getRemoteSource() != null) {
                String url = jsonSchemaInfo.getUrl(project);
                variants.add(LookupElementBuilder.create(url).withIcon(JsonIconGroup.jsonschema()));
            }
        }
        return variants.toArray();
    }
}
