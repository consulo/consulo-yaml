// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import com.jetbrains.jsonSchema.impl.JsonSchemaCompletionContributor;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.completion.CompletionContributor;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.psi.PsiElement;
import org.jetbrains.yaml.YAMLLanguage;

@ExtensionImpl(id = "YamlJsonSchema")
public class YamlJsonSchemaCompletionContributor extends CompletionContributor {
    @Override
    @RequiredReadAction
    public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        JsonSchemaService jsonSchemaService = JsonSchemaService.get(position.getProject());
        JsonSchemaObject object = jsonSchemaService.getSchemaObject(parameters.getOriginalFile());
        if (object != null) {
            JsonSchemaCompletionContributor.doCompletion(parameters, result, object, false);
        }
    }

    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }
}
