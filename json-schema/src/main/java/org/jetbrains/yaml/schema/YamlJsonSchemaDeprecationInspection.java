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

import com.intellij.json.pointer.JsonPointerPosition;
import com.jetbrains.jsonSchema.JsonSchemaObject;
import com.jetbrains.jsonSchema.extension.adapter.JsonValueAdapter;
import com.jetbrains.jsonSchema.impl.inspections.JsonSchemaDeprecationInspection;
import com.jetbrains.jsonSchema.impl.light.nodes.RootJsonSchemaObjectBackedByJackson;
import com.jetbrains.jsonSchema.internal.JsonSchemaResolver;
import com.jetbrains.jsonSchema.internal.MatchResult;
import com.jetbrains.jsonSchema.walker.JsonLikePsiWalker;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.yaml.localize.YAMLLocalize;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLValue;
import org.jetbrains.yaml.psi.YamlPsiElementVisitor;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

@ExtensionImpl
public class YamlJsonSchemaDeprecationInspection extends YamlJsonSchemaInspectionBase {
    @Override
    public String getShortName() {
        return "YAMLSchemaDeprecation";
    }

    @Override
    public LocalizeValue getDisplayName() {
        return YAMLLocalize.inspectionsSchemaDeprecationName();
    }

    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WEAK_WARNING;
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    protected PsiElementVisitor doBuildVisitor(ProblemsHolder holder,
                                               LocalInspectionToolSession session,
                                               Collection<PsiElement> roots,
                                               @Nullable JsonSchemaObject schema,
                                               Object state) {
        if (schema == null || (schema instanceof RootJsonSchemaObjectBackedByJackson rootSchema && !rootSchema.checkHasDeprecations())) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        PsiElement sampleElement = roots.iterator().next();
        JsonLikePsiWalker walker = JsonLikePsiWalker.getWalker(sampleElement, schema);
        if (walker == null) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }
        Project project = sampleElement.getProject();
        return new YamlPsiElementVisitor() {
            @Override
            @RequiredReadAction
            public void visitKeyValue(YAMLKeyValue keyValue) {
                annotate(keyValue);
                super.visitKeyValue(keyValue);
            }

            @RequiredReadAction
            private void annotate(YAMLKeyValue keyValue) {
                PsiElement key = keyValue.getKey();
                if (key == null) {
                    return;
                }
                JsonPointerPosition position = walker.findPosition(keyValue, true);
                if (position == null) {
                    return;
                }

                MatchResult result = new JsonSchemaResolver(project, schema, position, walker.createValueAdapter(key)).detailedResolve();
                YAMLValue value = keyValue.getValue();
                JsonValueAdapter valueAdapter = value == null
                    ? YamlPropertyAdapter.createEmptyValueAdapter(keyValue, false)
                    : walker.createValueAdapter(value);
                String message = JsonSchemaDeprecationInspection.getDeprecationMessage(result.mySchemas, valueAdapter);
                if (message != null) {
                    holder.newProblem(YAMLLocalize.inspectionsSchemaDeprecationText(keyValue.getName(), message))
                        .range(key)
                        .create();
                }
            }
        };
    }
}
