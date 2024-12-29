package org.jetbrains.yaml;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.Configurable;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.setting.CodeStyleSettingsProvider;
import consulo.language.codeStyle.ui.setting.CodeStyleAbstractConfigurable;
import consulo.language.codeStyle.ui.setting.CodeStyleAbstractPanel;
import consulo.language.codeStyle.ui.setting.TabbedLanguageCodeStylePanel;

import jakarta.annotation.Nonnull;

/**
 * @author oleg
 */
@ExtensionImpl
public class YAMLCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
    @Nonnull
    @Override
    public Configurable createSettingsPage(final CodeStyleSettings settings, final CodeStyleSettings originalSettings) {
        return new CodeStyleAbstractConfigurable(settings, originalSettings, YAMLLanguage.INSTANCE.getDisplayName()) {
            @Override
            protected CodeStyleAbstractPanel createPanel(final CodeStyleSettings settings) {
                final CodeStyleSettings currentSettings = getCurrentSettings();
                final CodeStyleSettings settings1 = settings;
                return new TabbedLanguageCodeStylePanel(YAMLLanguage.INSTANCE, currentSettings, settings1) {
                    @Override
                    protected void initTabs(final CodeStyleSettings settings) {
                        addIndentOptionsTab(settings);
                    }
                };
            }

            @Override
            public String getHelpTopic() {
                return "reference.settingsdialog.codestyle.yaml";
            }
        };
    }

    @Override
    public String getConfigurableDisplayName() {
        return YAMLLanguage.INSTANCE.getDisplayName();
    }
}
