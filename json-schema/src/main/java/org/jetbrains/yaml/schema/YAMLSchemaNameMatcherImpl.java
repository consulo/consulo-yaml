// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
import com.jetbrains.jsonSchema.JsonSchemaSpellcheckerClient;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import org.jetbrains.yaml.YAMLSchemaNameMatcher;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public final class YAMLSchemaNameMatcherImpl implements YAMLSchemaNameMatcher {
    @Override
    public boolean matches(PsiElement element) {
        return new JsonSchemaSpellcheckerClientForYaml(element).matchesNameFromSchema();
    }

    private static final class JsonSchemaSpellcheckerClientForYaml extends JsonSchemaSpellcheckerClient {
        private final PsiElement myElement;

        private JsonSchemaSpellcheckerClientForYaml(PsiElement element) {
            myElement = element;
        }

        @Override
        protected PsiElement getElement() {
            return myElement;
        }

        @Override
        protected @Nullable String getValue() {
            PsiElement parent = myElement.getParent();
            if (myElement.getNode().getElementType() == YAMLTokenTypes.SCALAR_KEY && parent instanceof YAMLKeyValue keyValue) {
                return keyValue.getKeyText();
            }
            if (parent instanceof YAMLKeyValue keyValue) {
                return keyValue.getValueText();
            }
            if (parent instanceof YAMLScalar scalar) {
                return scalar.getTextValue();
            }
            return null;
        }

        @Override
        protected boolean isXIntellijInjection(JsonSchemaService service, JsonSchemaObject rootSchema) {
            return false;
        }
    }
}
