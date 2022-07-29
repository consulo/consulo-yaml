/**
 * @author VISTALL
 * @since 29-Jul-22
 */
module org.jetbrains.plugins.yaml {
    // TODO remove in future
    requires java.desktop;

    requires consulo.ide.api;
    requires consulo.language.code.style.ui.api;
    requires consulo.language.impl;

    requires static com.intellij.spellchecker;

    exports consulo.yaml;
    exports consulo.yaml.icon;
    exports consulo.yaml.localize;

    exports org.jetbrains.yaml;
    exports org.jetbrains.yaml.lexer;
    exports org.jetbrains.yaml.psi;
}