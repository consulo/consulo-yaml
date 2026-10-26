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
package org.jetbrains.yaml;

import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.editor.inject.InjectionMeta;
import consulo.language.inject.MultiHostRegistrar;
import consulo.language.psi.ElementManipulators;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiLanguageInjectionHost;
import org.jetbrains.yaml.psi.impl.YAMLBlockScalarImpl;
import org.jetbrains.yaml.psi.impl.YAMLScalarImpl;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class YamlLanguageInjectionPerformer {
    private YamlLanguageInjectionPerformer() {
    }

    public static List<TextRange> getYamlLiteralRanges(PsiElement context) {
        if (context instanceof YAMLScalarImpl scalar) {
            return scalar.getContentRanges();
        }
        return List.of(ElementManipulators.getValueTextRange(context));
    }

    public static void injectIntoYamlMultiRanges(MultiHostRegistrar registrar,
                                                 PsiLanguageInjectionHost context,
                                                 List<TextRange> ranges,
                                                 Language language,
                                                 @Nullable String prefix,
                                                 @Nullable String suffix) {
        if (ranges.isEmpty()) {
            return;
        }

        registrar.startInjecting(language);

        if (context instanceof YAMLBlockScalarImpl blockScalar) {
            blockScalar.putUserData(InjectionMeta.getInjectionIndent(), blockScalar.getIndentString());
        }

        if (ranges.size() == 1) {
            registrar.addPlace(prefix, suffix, context, ranges.getFirst());
        }
        else {
            registrar.addPlace(prefix, null, context, ranges.getFirst());
            for (TextRange textRange : ranges.subList(1, ranges.size() - 1)) {
                registrar.addPlace(null, null, context, textRange);
            }
            registrar.addPlace(null, suffix, context, ranges.getLast());
        }
        registrar.doneInjecting();
    }
}
