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
import com.jetbrains.jsonSchema.extension.adapter.JsonPropertyAdapter;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.Application;
import consulo.application.util.NotNullLazyValue;
import consulo.application.util.RecursionManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.ObjectUtil;
import org.jetbrains.yaml.psi.YAMLAlias;
import org.jetbrains.yaml.psi.YAMLAnchor;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;
import org.jetbrains.yaml.psi.YamlTagRecogniser;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class YamlObjectAdapter implements JsonObjectValueAdapter {
    private final YAMLMapping myObject;
    private final NotNullLazyValue<List<JsonPropertyAdapter>> myChildAdapters = NotNullLazyValue.createValue(this::computeChildAdapters);

    public YamlObjectAdapter(YAMLMapping object) {
        myObject = object;
    }

    @Override
    @RequiredReadAction
    public boolean isObject() {
        PsiElement tag = myObject.getTag();

        if (tag == null) {
            return true;
        }
        String tagText = tag.getText();
        return "!!map".equals(tagText)
            || Application.get().getExtensionPoint(YamlTagRecogniser.class).anyMatchSafe(extension -> extension.isRecognizedTag(tagText));
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
    public PsiElement getDelegate() {
        return myObject;
    }

    @Override
    public JsonObjectValueAdapter getAsObject() {
        return this;
    }

    @Override
    public @Nullable JsonArrayValueAdapter getAsArray() {
        return null;
    }

    @Override
    public List<JsonPropertyAdapter> getPropertyList() {
        return myChildAdapters.getValue();
    }

    private List<JsonPropertyAdapter> computeChildAdapters() {
        Collection<YAMLKeyValue> keyValues = myObject.getKeyValues();
        List<JsonPropertyAdapter> adapters = new ArrayList<>(keyValues.size());
        for (YAMLKeyValue value : keyValues) {
            if (addPropertiesFromReferencedObject(adapters, value)) {
                continue;
            }
            adapters.add(new YamlPropertyAdapter(value));
        }
        return adapters;
    }

    private boolean addPropertiesFromReferencedObject(List<JsonPropertyAdapter> adapters, YAMLKeyValue value) {
        String keyText = value.getKeyText();
        if (!"<<".equals(keyText)) {
            return false;
        }
        YAMLValue yamlValue = value.getValue();
        PsiElement resolved = resolveYamlAlias(yamlValue);
        if (resolved != null) {
            YAMLMapping mapping = ObjectUtil.tryCast(resolved, YAMLMapping.class);
            if (mapping == null) {
                return false;
            }
            List<JsonPropertyAdapter> propertyAdapters =
                RecursionManager.doPreventingRecursion(myObject, false, () -> new YamlObjectAdapter(mapping).getPropertyList());
            if (propertyAdapters != null) {
                adapters.addAll(propertyAdapters);
                return true;
            }
        }
        if (yamlValue instanceof YAMLMapping) {
            if (PsiTreeUtil.getChildOfType(yamlValue, YAMLAnchor.class) == null) {
                return false;
            }
            adapters.addAll(new YamlObjectAdapter((YAMLMapping) yamlValue).getPropertyList());
            return true;
        }
        return false;
    }

    static @Nullable PsiElement resolveYamlAlias(@Nullable YAMLValue yamlValue) {
        PsiReference reference = yamlValue instanceof YAMLAlias ? yamlValue.getReference() : null;
        PsiElement resolved = reference == null ? null : reference.resolve();
        resolved = resolved == null ? null : resolved.getParent();
        return resolved;
    }
}
