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

import com.jetbrains.jsonSchema.JsonSchemaType;
import com.jetbrains.jsonSchema.extension.adapter.JsonArrayValueAdapter;
import com.jetbrains.jsonSchema.extension.adapter.JsonObjectValueAdapter;
import com.jetbrains.jsonSchema.extension.adapter.JsonPropertyAdapter;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class YamlEmptyObjectAdapter implements JsonObjectValueAdapter {
    private final PsiElement myElement;

    public YamlEmptyObjectAdapter(PsiElement element) {
        myElement = element;
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isStringLiteral() {
        return false;
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
    public boolean isEmptyAdapter() {
        return true;
    }

    @Override
    public List<JsonPropertyAdapter> getPropertyList() {
        return List.of();
    }

    @Override
    public PsiElement getDelegate() {
        return myElement;
    }

    @Override
    public @Nullable JsonObjectValueAdapter getAsObject() {
        return this;
    }

    @Override
    public @Nullable JsonArrayValueAdapter getAsArray() {
        return null;
    }

    @Override
    public @Nullable JsonSchemaType getAlternateType(@Nullable JsonSchemaType type) {
        return type == JsonSchemaType._object ? JsonSchemaType._null : type;
    }
}
