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

import com.jetbrains.jsonSchema.JsonSchemaObject;
import com.jetbrains.jsonSchema.JsonSchemaService;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.Language;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public abstract class YamlJsonSchemaInspectionBase extends LocalInspectionTool {
    @Override
    public @Nullable Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }

    @Override
    public LocalizeValue getGroupDisplayName() {
        return LocalizeValue.empty();
    }

    @Override
    @RequiredReadAction
    public PsiElementVisitor buildVisitor(ProblemsHolder holder,
                                          boolean isOnTheFly,
                                          LocalInspectionToolSession session,
                                          Object state) {
        PsiFile file = holder.getFile();
        if (!(file instanceof YAMLFile)) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        Collection<PsiElement> roots = YamlJsonPsiWalker.INSTANCE.getRoots(file);
        if (roots.isEmpty()) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }
        JsonSchemaService service = JsonSchemaService.get(file.getProject());
        VirtualFile virtualFile = file.getViewProvider().getVirtualFile();
        if (!service.isApplicableToFile(virtualFile)) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }
        JsonSchemaObject rootSchema = service.getSchemaObject(file);
        if (rootSchema == null) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }
        return doBuildVisitor(holder, session, roots, rootSchema, state);
    }

    protected abstract PsiElementVisitor doBuildVisitor(ProblemsHolder holder,
                                                        LocalInspectionToolSession session,
                                                        Collection<PsiElement> roots,
                                                        JsonSchemaObject object,
                                                        Object state);
}
