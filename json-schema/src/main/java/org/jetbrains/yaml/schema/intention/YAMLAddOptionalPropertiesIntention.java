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
package org.jetbrains.yaml.schema.intention;

import com.jetbrains.jsonSchema.impl.fixes.AddOptionalPropertiesIntention;
import com.jetbrains.jsonSchema.walker.JsonLikeSyntaxAdapter;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.schema.YamlJsonPsiWalker;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@ExtensionImpl
@IntentionMetaData(ignoreId = "yaml.AddOptionalPropertiesIntention", fileExtensions = "yaml", categories = "YAML")
public class YAMLAddOptionalPropertiesIntention extends AddOptionalPropertiesIntention {
    @Override
    @RequiredReadAction
    protected @Nullable PsiElement findContainingObjectNode(Editor editor, PsiElement element) {
        YAMLMapping mapping = PsiTreeUtil.getParentOfType(element, YAMLMapping.class, false);
        if (mapping != null) {
            return mapping;
        }
        if (!(element.getContainingFile() instanceof YAMLFile yamlFile)) {
            return null;
        }
        return findSingleEmptyDocument(yamlFile);
    }

    @Override
    @RequiredReadAction
    protected @Nullable PsiElement findPhysicalObjectNode(Editor editor, PsiElement element) {
        PsiFile file = Objects.requireNonNull(element.getContainingFile());
        PsiElement physLeaf = file.findElementAt(editor.getCaretModel().getOffset());
        if (physLeaf == null) {
            return null;
        }
        YAMLMapping mapping = PsiTreeUtil.getParentOfType(physLeaf, YAMLMapping.class, false);
        if (mapping != null) {
            return mapping;
        }
        return file instanceof YAMLFile yamlFile ? findSingleEmptyDocument(yamlFile) : null;
    }

    @Override
    protected JsonLikeSyntaxAdapter getSyntaxAdapter(Project project) {
        return YamlJsonPsiWalker.INSTANCE.getSyntaxAdapter(project);
    }

    @RequiredReadAction
    private static @Nullable YAMLDocument findSingleEmptyDocument(YAMLFile yamlFile) {
        List<YAMLDocument> documents = yamlFile.getDocuments();
        if (documents.size() != 1) {
            return null;
        }
        YAMLDocument document = documents.getFirst();
        return document.getChildren().length == 0 ? document : null;
    }
}
