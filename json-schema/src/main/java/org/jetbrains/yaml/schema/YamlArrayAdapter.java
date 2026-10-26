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
import consulo.annotation.access.RequiredReadAction;
import consulo.application.Application;
import consulo.application.util.NotNullLazyValue;
import consulo.language.psi.PsiElement;
import org.jetbrains.yaml.psi.YAMLSequence;
import org.jetbrains.yaml.psi.YAMLSequenceItem;
import org.jetbrains.yaml.psi.YAMLValue;
import org.jetbrains.yaml.psi.YamlTagRecogniser;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class YamlArrayAdapter implements JsonArrayValueAdapter {
    private final YAMLSequence myArray;
    private final NotNullLazyValue<List<JsonValueAdapter>> myChildAdapters = NotNullLazyValue.createValue(this::computeChildAdapters);

    public YamlArrayAdapter(YAMLSequence array) {
        myArray = array;
    }

    @Override
    public List<JsonValueAdapter> getElements() {
        return myChildAdapters.getValue();
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    @RequiredReadAction
    public boolean isArray() {
        PsiElement tag = myArray.getTag();
        if (tag == null) {
            return true;
        }

        String tagText = tag.getText();
        return "!!seq".equals(tagText)
            || Application.get().getExtensionPoint(YamlTagRecogniser.class).anyMatchSafe(extension -> extension.isRecognizedTag(tagText));
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
    public PsiElement getDelegate() {
        return myArray;
    }

    @Override
    public @Nullable JsonObjectValueAdapter getAsObject() {
        return null;
    }

    @Override
    public JsonArrayValueAdapter getAsArray() {
        return this;
    }

    private List<JsonValueAdapter> computeChildAdapters() {
        List<YAMLSequenceItem> items = myArray.getItems();
        List<JsonValueAdapter> adapters = new ArrayList<>(items.size());
        for (YAMLSequenceItem item : items) {
            YAMLValue value = item.getValue();
            if (value == null) {
                JsonValueAdapter emptyAdapter = YamlPropertyAdapter.createEmptyValueAdapter(item.getFirstChild(), true);
                if (emptyAdapter != null) {
                    adapters.add(emptyAdapter);
                }
                continue;
            }
            adapters.add(YamlPropertyAdapter.createValueAdapterByType(value));
        }
        return adapters;
    }
}
