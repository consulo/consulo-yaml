// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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

import com.jetbrains.jsonSchema.impl.JsonSchemaBasedLanguageInjector;
import com.jetbrains.jsonSchema.impl.JsonSchemaInjectorBase;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.inject.MultiHostInjector;
import consulo.language.inject.MultiHostRegistrar;
import consulo.language.psi.PsiElement;
import org.jetbrains.yaml.YamlLanguageInjectionPerformer;
import org.jetbrains.yaml.psi.YAMLScalar;

@ExtensionImpl(id = "yamlJsonSchemaLanguageInjector")
public class YamlJsonSchemaLanguageInjector implements MultiHostInjector {
    @Override
    public Class<? extends PsiElement> getElementClass() {
        return YAMLScalar.class;
    }

    @Override
    @RequiredReadAction
    public void injectLanguages(MultiHostRegistrar registrar, PsiElement context) {
        if (!(context instanceof YAMLScalar scalar)) {
            return;
        }
        JsonSchemaInjectorBase.InjectedLanguageData injection = JsonSchemaBasedLanguageInjector.getLanguageToInject(context, true);
        if (injection == null) {
            return;
        }
        YamlLanguageInjectionPerformer.injectIntoYamlMultiRanges(
            registrar,
            scalar,
            YamlLanguageInjectionPerformer.getYamlLiteralRanges(scalar),
            injection.getInjectedLanguage(),
            injection.prefix,
            injection.postfix
        );
    }
}
