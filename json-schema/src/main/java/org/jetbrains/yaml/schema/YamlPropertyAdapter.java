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

import com.jetbrains.jsonSchema.extension.adapter.JsonObjectValueAdapter;
import com.jetbrains.jsonSchema.extension.adapter.JsonPropertyAdapter;
import com.jetbrains.jsonSchema.extension.adapter.JsonValueAdapter;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.RecursionManager;
import consulo.language.ast.TokenType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.ObjectUtil;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLAlias;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLSequence;
import org.jetbrains.yaml.psi.YAMLValue;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class YamlPropertyAdapter implements JsonPropertyAdapter {

    private final PsiElement myProperty;

    public YamlPropertyAdapter(PsiElement property) {
        myProperty = property;
    }

    @Override
    @RequiredReadAction
    public @Nullable String getName() {
        return myProperty instanceof YAMLKeyValue ? ((YAMLKeyValue) myProperty).getKeyText() : myProperty.getText();
    }

    @Override
    public @Nullable JsonValueAdapter getNameValueAdapter() {
        if (!(myProperty instanceof YAMLKeyValue)) {
            return null;
        }
        PsiElement key = ((YAMLKeyValue) myProperty).getKey();
        if (key == null) {
            return null;
        }
        return new YamlPropertyKeyAdapter(key);
    }

    @Override
    @RequiredReadAction
    public Collection<JsonValueAdapter> getValues() {
        YAMLValue value = myProperty instanceof YAMLKeyValue ? ((YAMLKeyValue) myProperty).getValue() : null;
        return value != null
            ? Collections.singletonList(createValueAdapterByType(value))
            : ContainerUtil.createMaybeSingletonList(createEmptyValueAdapter(myProperty, false));
    }

    @Override
    public PsiElement getDelegate() {
        return myProperty;
    }

    @Override
    public @Nullable JsonObjectValueAdapter getParentObject() {
        YAMLMapping parentMapping = myProperty instanceof YAMLKeyValue ? ((YAMLKeyValue) myProperty).getParentMapping() :
            ObjectUtil.tryCast(myProperty.getParent(), YAMLMapping.class);
        return parentMapping != null ? new YamlObjectAdapter(parentMapping) : null;
    }

    public static JsonValueAdapter createValueAdapterByType(YAMLValue value) {
        if (value instanceof YAMLAlias) {
            PsiElement result = YamlObjectAdapter.resolveYamlAlias(value);
            if (result instanceof YAMLValue) {
                JsonValueAdapter adapter = RecursionManager.doPreventingRecursion(value, false, () -> createValueAdapterByType((YAMLValue) result));
                if (adapter != null) {
                    return adapter;
                }
            }
        }
        if (value instanceof YAMLMapping) {
            return new YamlObjectAdapter((YAMLMapping) value);
        }
        if (value instanceof YAMLSequence) {
            return new YamlArrayAdapter((YAMLSequence) value);
        }
        return new YamlGenericValueAdapter(value);
    }

    @RequiredReadAction
    public static @Nullable JsonValueAdapter createEmptyValueAdapter(@Nullable PsiElement context, boolean pinSelf) {
        if (context instanceof YAMLKeyValue && ((YAMLKeyValue) context).getValue() == null) {
            PsiElement next = PsiTreeUtil.skipWhitespacesAndCommentsForward(context);
            if (PsiUtilCore.getElementType(next) == YAMLTokenTypes.EOL) {
                next = PsiTreeUtil.skipWhitespacesAndCommentsForward(next);
                if (next != null && PsiUtilCore.getElementType(next) == YAMLTokenTypes.INDENT
                    && !(PsiTreeUtil.skipWhitespacesAndCommentsForward(next) instanceof YAMLKeyValue)) {
                    // potentially empty object after newline+indent
                    return new YamlEmptyObjectAdapter(next);
                }
            }
        }
        if (context == null) {
            return null;
        }
        PsiElement nextSibling = context.getNextSibling();
        PsiElement nodeToHighlight = PsiUtilCore.getElementType(nextSibling) == TokenType.WHITE_SPACE
            ? nextSibling
            : (pinSelf ? context : context.getLastChild());
        return nodeToHighlight == null ? null : new YamlEmptyValueAdapter(nodeToHighlight);
    }
}
