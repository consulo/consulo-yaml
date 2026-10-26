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
package org.jetbrains.yaml.psi;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;

/**
 * Interface for recognising known domain-specific YAML tags.
 * <p>
 * This interface allows implementations to inform the IDE about YAML tags that are expected in a particular YAML-based framework.
 * If a tag is recognised by any implementation, no warnings will be reported in the editor.
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface YamlTagRecogniser {
    /**
     * Checks if the provided tag text is recognised as a known domain-specific YAML tag.
     */
    boolean isRecognizedTag(String tagText);
}
