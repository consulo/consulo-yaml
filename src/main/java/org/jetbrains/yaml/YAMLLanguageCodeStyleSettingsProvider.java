package org.jetbrains.yaml;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.localize.ApplicationLocalize;
import consulo.language.Language;
import consulo.language.codeStyle.CommonCodeStyleSettings;
import consulo.language.codeStyle.setting.IndentOptionsEditor;
import consulo.language.codeStyle.setting.LanguageCodeStyleSettingsProvider;

import jakarta.annotation.Nonnull;
import javax.swing.*;

/**
 * @author oleg
 */
@ExtensionImpl
public class YAMLLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
    @Override
    public CommonCodeStyleSettings getDefaultCommonSettings() {
        CommonCodeStyleSettings defaultSettings = new CommonCodeStyleSettings(YAMLLanguage.INSTANCE);
        CommonCodeStyleSettings.IndentOptions indentOptions = defaultSettings.initIndentOptions();
        indentOptions.INDENT_SIZE = 2;
        indentOptions.USE_TAB_CHARACTER = false;
        return defaultSettings;
    }

    @Override
    public IndentOptionsEditor getIndentOptionsEditor() {
        return new YAMLIndentOptionsEditor();
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }

    @Override
    public String getCodeSample(@Nonnull SettingsType settingsType) {
        return "product: \n" +
            "  name: RubyMine\n" +
            "  version: 8\n" +
            "  vendor: JetBrains\n" +
            "  url: \"https://www.jetbrains.com/ruby\"";
    }

    private class YAMLIndentOptionsEditor extends IndentOptionsEditor {
        @Override
        protected void addComponents() {
            addTabOptions();
            // Tabs in YAML are not allowed
            myCbUseTab.setEnabled(false);

            myTabSizeField = createIndentTextField();
            myTabSizeLabel = new JLabel(ApplicationLocalize.editboxIndentTabSize().get());
            // Do not add
            //add(myTabSizeLabel, myTabSizeField);

            myIndentField = createIndentTextField();
            myIndentLabel = new JLabel(ApplicationLocalize.editboxIndentIndent().get());
            add(myIndentLabel, myIndentField);
        }

        @Override
        public void setEnabled(boolean enabled) {
            // Do nothing
        }
    }
}
