package consulo.yaml.editor;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.action.SmartBackspaceDisabler;
import consulo.language.Language;
import consulo.language.editor.action.LanguageBackspaceModeOverride;
import org.jetbrains.yaml.YAMLLanguage;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 28-Jul-22
 */
@ExtensionImpl
public class YamlSmartBackspaceDisabler extends SmartBackspaceDisabler implements LanguageBackspaceModeOverride {
    @Nonnull
    @Override
    public Language getLanguage() {
        return YAMLLanguage.INSTANCE;
    }
}
