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

import com.jetbrains.jsonSchema.extension.JsonSchemaEnabler;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.scratch.ScratchUtil;
import consulo.language.file.LanguageFileType;
import consulo.language.scratch.RootType;
import consulo.language.scratch.ScratchFileService;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import org.jetbrains.yaml.YAMLLanguage;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public class YamlJsonEnabler implements JsonSchemaEnabler {
    @Override
    public boolean isEnabledForFile(VirtualFile file, @Nullable Project project) {
        FileType type = file.getFileType();
        if (type instanceof LanguageFileType languageFileType && languageFileType.getLanguage() instanceof YAMLLanguage) {
            return true;
        }
        if (project == null || !ScratchUtil.isScratch(file)) {
            return false;
        }
        RootType rootType = ScratchFileService.findRootType(file);
        return rootType != null && rootType.substituteLanguage(project, file) instanceof YAMLLanguage;
    }

    @Override
    public boolean canBeSchemaFile(VirtualFile file) {
        return isEnabledForFile(file, null /*avoid checking scratches*/);
    }
}
