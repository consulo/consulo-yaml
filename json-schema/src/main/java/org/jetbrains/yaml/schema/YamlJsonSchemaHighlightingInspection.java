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

import com.jetbrains.jsonSchema.JsonComplianceCheckerOptions;
import com.jetbrains.jsonSchema.JsonSchemaObject;
import com.jetbrains.jsonSchema.impl.JsonSchemaComplianceChecker;
import com.jetbrains.jsonSchema.impl.inspections.JsonSchemaComplianceInspectionState;
import com.jetbrains.jsonSchema.walker.JsonLikePsiWalker;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.InspectionToolState;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.localize.LocalizeValue;
import consulo.yaml.localize.YAMLLocalize;
import org.jetbrains.yaml.psi.YamlPsiElementVisitor;

import java.util.Collection;

@ExtensionImpl
public class YamlJsonSchemaHighlightingInspection extends YamlJsonSchemaInspectionBase {
    @Override
    public String getShortName() {
        return "YAMLSchemaValidation";
    }

    @Override
    public LocalizeValue getDisplayName() {
        return YAMLLocalize.inspectionsSchemaValidationName();
    }

    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    public InspectionToolState<?> createStateProvider() {
        return new JsonSchemaComplianceInspectionState();
    }

    @Override
    protected PsiElementVisitor doBuildVisitor(ProblemsHolder holder,
                                               LocalInspectionToolSession session,
                                               Collection<PsiElement> roots,
                                               JsonSchemaObject object,
                                               Object state) {
        JsonSchemaComplianceInspectionState inspectionState = (JsonSchemaComplianceInspectionState) state;
        JsonComplianceCheckerOptions options = new JsonComplianceCheckerOptions(inspectionState.myCaseInsensitiveEnum);
        return new YamlPsiElementVisitor() {
            @Override
            @RequiredReadAction
            public void visitElement(PsiElement element) {
                if (!roots.contains(element)) {
                    return;
                }
                JsonLikePsiWalker walker = JsonLikePsiWalker.getWalker(element, object);
                if (walker == null) {
                    return;
                }
                String prefix = YAMLLocalize.inspectionsSchemaValidationPrefix().get() + " ";
                new JsonSchemaComplianceChecker(object, holder, walker, session, options, prefix).annotate(element);
            }
        };
    }
}
