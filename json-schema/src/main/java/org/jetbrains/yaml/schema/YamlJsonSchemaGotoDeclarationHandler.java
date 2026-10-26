// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import com.jetbrains.jsonSchema.JsonSchemaService;
import com.jetbrains.jsonSchema.extension.JsonSchemaGotoDeclarationSuppressor;
import com.jetbrains.jsonSchema.internal.JsonSchemaResolver;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.codeEditor.Editor;
import consulo.language.ast.IElementType;
import consulo.language.editor.navigation.GotoDeclarationHandler;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@ExtensionImpl
public class YamlJsonSchemaGotoDeclarationHandler implements GotoDeclarationHandler {
    @Override
    @RequiredReadAction
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
        boolean shouldSuppressNavigation = Application.get().getExtensionPoint(JsonSchemaGotoDeclarationSuppressor.class)
            .anyMatchSafe(it -> sourceElement != null && it.shouldSuppressGtd(sourceElement));
        if (shouldSuppressNavigation) {
            return null;
        }

        IElementType elementType = PsiUtilCore.getElementType(sourceElement);
        if (elementType != YAMLTokenTypes.SCALAR_KEY) {
            return null;
        }
        YAMLKeyValue keyValue = PsiTreeUtil.getParentOfType(sourceElement, YAMLKeyValue.class);
        // do not override injected references
        if (keyValue == null || keyValue.getKey() != sourceElement || keyValue.getReferences().length != 0) {
            return null;
        }
        JsonSchemaService service = JsonSchemaService.get(keyValue.getProject());
        PsiFile containingFile = keyValue.getContainingFile();
        if (containingFile == null) {
            return null;
        }
        VirtualFile file = containingFile.getVirtualFile();
        if (file == null || !service.isApplicableToFile(file)) {
            return null;
        }
        JsonPointerPosition steps = YamlJsonPsiWalker.INSTANCE.findPosition(keyValue, true);
        if (steps == null) {
            return null;
        }
        JsonSchemaObject schemaObject = service.getSchemaObject(containingFile);
        if (schemaObject != null) {
            PsiElement target = new JsonSchemaResolver(keyValue.getProject(), schemaObject, steps,
                YamlJsonPsiWalker.INSTANCE.createValueAdapter(Objects.requireNonNull(keyValue.getParent())))
                //final PsiElement target = new JsonSchemaResolver(sourceElement.getProject(), schemaObject, steps, YamlJsonPsiWalker.INSTANCE.getParentPropertyAdapter(keyValue).getNameValueAdapter())
                .findNavigationTarget(keyValue.getValue());
            if (target != null) {
                return new PsiElement[]{target};
            }
        }
        return null;
    }
}
