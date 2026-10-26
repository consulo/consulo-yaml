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
package org.jetbrains.yaml;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.application.Application;
import consulo.language.psi.PsiElement;

/**
 * Tells if the JSON schema of the file defines a YAML key or scalar, for example as a property name or an enum value.
 * The spellchecker and Grazie do not check such an element.
 * <p>
 * The JSON schema implementation is in the optional JSON schema module.
 * Without it, no element matches.
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface YAMLSchemaNameMatcher {
    /**
     * @param element a {@link YAMLTokenTypes#SCALAR_KEY} token, a scalar token, or a {@link org.jetbrains.yaml.psi.YAMLScalar}
     */
    boolean matches(PsiElement element);

    static boolean matchesNameFromSchema(PsiElement element) {
        return Application.get().getExtensionPoint(YAMLSchemaNameMatcher.class).anyMatchSafe(it -> it.matches(element));
    }
}
