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

import com.jetbrains.jsonSchema.extension.adapter.JsonArrayValueAdapter;
import com.jetbrains.jsonSchema.extension.adapter.JsonObjectValueAdapter;
import com.jetbrains.jsonSchema.extension.adapter.JsonValueAdapter;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

public class YamlPropertyKeyAdapter implements JsonValueAdapter {
    private final PsiElement myDelegate;

    public YamlPropertyKeyAdapter(PsiElement delegate) {
        myDelegate = delegate;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isStringLiteral() {
        return true;
    }

    @Override
    public boolean isNumberLiteral() {
        return false;
    }

    @Override
    public boolean isBooleanLiteral() {
        return false;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public PsiElement getDelegate() {
        return myDelegate;
    }

    @Override
    public @Nullable JsonObjectValueAdapter getAsObject() {
        return null;
    }

    @Override
    public @Nullable JsonArrayValueAdapter getAsArray() {
        return null;
    }
}
