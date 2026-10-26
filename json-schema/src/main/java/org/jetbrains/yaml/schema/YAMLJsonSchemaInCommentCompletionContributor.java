// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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

import com.jetbrains.jsonSchema.impl.JsonSchemaByCommentProvider;
import consulo.annotation.component.ExtensionImpl;
import consulo.json.icon.JsonIconGroup;
import consulo.language.Language;
import consulo.language.editor.AutoPopupController;
import consulo.language.editor.completion.CompletionContributor;
import consulo.language.editor.completion.CompletionType;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiComment;
import consulo.util.lang.StringUtil;
import consulo.yaml.localize.YAMLLocalize;
import org.jetbrains.yaml.YAMLLanguage;

@ExtensionImpl
public class YAMLJsonSchemaInCommentCompletionContributor extends CompletionContributor {
    public YAMLJsonSchemaInCommentCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiComment().with(TopDocumentComment.INSTANCE), (parameters, context, result) -> {
            if (!(parameters.getOriginalPosition() instanceof PsiComment psiComment)) {
                return;
            }
            String trimmedBody = StringUtil.trimStart(psiComment.getText(), "#").stripLeading();

            for (String schemaComment : JsonSchemaByCommentProvider.schemaCommentsForCompletion) {
                if (trimmedBody.isBlank() ||
                    trimmedBody.length() != schemaComment.length() && StringUtil.startsWithIgnoreCase(schemaComment, trimmedBody)) {
                    result.addElement(
                        LookupElementBuilder.create(schemaComment)
                            .withIcon(JsonIconGroup.jsonschema())
                            .withTypeText(YAMLLocalize.jsonSchemaSpecifyingComment().get(), true)
                            .withInsertHandler((insertionContext, item) -> insertionContext.setLaterRunnable(
                                () -> AutoPopupController.getInstance(insertionContext.getProject())
                                    .showCompletionPopup(insertionContext.getEditor(), CompletionType.BASIC, true, false, true)
                            ))
                    );
                }
            }
        });
    }

    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }
}
