package consulo.yaml;

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.AttributesFlyweightBuilder;
import consulo.colorScheme.EditorColorSchemeExtender;
import consulo.colorScheme.EditorColorsScheme;
import consulo.ui.color.RGBColor;
import jakarta.annotation.Nonnull;
import org.jetbrains.yaml.YAMLHighlighter;

/**
 * @author VISTALL
 * @since 2026-01-25
 */
@ExtensionImpl
public class YAMLDarkEditorColorSchemeExtender implements EditorColorSchemeExtender {
    @Override
    public void extend(Builder builder) {
        builder.add(YAMLHighlighter.ANCHOR, AttributesFlyweightBuilder.create().withForeground(new RGBColor(0, 0, 0xE6)).build());
    }

    @Nonnull
    @Override
    public String getColorSchemeId() {
        return EditorColorsScheme.DARCULA_SCHEME_NAME;
    }
}
