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
import com.jetbrains.jsonSchema.walker.JsonLikePsiWalker;
import com.jetbrains.jsonSchema.walker.JsonLikePsiWalkerFactory;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLPsiElement;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public class YamlJsonLikePsiWalkerFactory implements JsonLikePsiWalkerFactory {
    @Override
    @RequiredReadAction
    public boolean handles(PsiElement element) {
        return element.getContainingFile() instanceof YAMLFile || element instanceof YAMLPsiElement;
    }

    @Override
    public JsonLikePsiWalker create(@Nullable JsonSchemaObject schemaObject) {
        return YamlJsonPsiWalker.INSTANCE;
    }
}
