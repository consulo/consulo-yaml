/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.TextAttributesKey;
import consulo.colorScheme.setting.AttributesDescriptor;
import consulo.colorScheme.setting.ColorDescriptor;
import consulo.language.editor.colorScheme.setting.ColorSettingsPage;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.localize.LocalizeValue;
import consulo.yaml.localize.YAMLLocalize;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author oleg
 */
@ExtensionImpl
public class YAMLColorsPage implements ColorSettingsPage {

    private static final String DEMO_TEXT = "---\n" +
        "# Read about fixtures at http://ar.rubyonrails.org/classes/Fixtures.html\n" +
        "static_sidebar:\n" +
        "  id: \"foo\"\n" +
        "  name: 'side_bar'\n" +
        "  staged_position: 1\n" +
        "  blog_id: 1\n" +
        "  config: |+\n" +
        "    --- !map:HashWithIndifferentAccess\n" +
        "      title: Static Sidebar\n" +
        "      body: The body of a static sidebar\n" +
        "  type: StaticSidebar\n" +
        "  type: > some_type_here";

    private static final AttributesDescriptor[] ATTRS = new AttributesDescriptor[]{
        new AttributesDescriptor(YAMLLocalize.colorSettingsYamlKey(), YAMLHighlighter.SCALAR_KEY),
        new AttributesDescriptor(YAMLLocalize.colorSettingsYamlString(), YAMLHighlighter.SCALAR_STRING),
        new AttributesDescriptor(YAMLLocalize.colorSettingsYamlDstring(), YAMLHighlighter.SCALAR_DSTRING),
        new AttributesDescriptor(YAMLLocalize.colorSettingsYamlScalarList(), YAMLHighlighter.SCALAR_LIST),
        new AttributesDescriptor(YAMLLocalize.colorSettingsYamlScalarText(), YAMLHighlighter.SCALAR_TEXT),
        new AttributesDescriptor(YAMLLocalize.colorSettingsYamlText(), YAMLHighlighter.TEXT),
        new AttributesDescriptor(YAMLLocalize.colorSettingsYamlSign(), YAMLHighlighter.SIGN),
        new AttributesDescriptor(YAMLLocalize.colorSettingsYamlComment(), YAMLHighlighter.COMMENT)
    };

    // Empty still
    private static final Map<String, TextAttributesKey> ADDITIONAL_HIGHLIGHT_DESCRIPTORS = new HashMap<>();

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return ADDITIONAL_HIGHLIGHT_DESCRIPTORS;
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return YAMLLocalize.colorSettingsYamlName();
    }

    @Nonnull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return ATTRS;
    }

    @Nonnull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Nonnull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new YAMLSyntaxHighlighter();
    }

    @Nonnull
    @Override
    public String getDemoText() {
        return DEMO_TEXT;
    }
}
