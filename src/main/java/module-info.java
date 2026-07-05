/**
 * @author VISTALL
 * @since 29-Jul-22
 */
module org.jetbrains.plugins.yaml {
    // TODO remove in future
    requires java.desktop;

    requires consulo.language.ui.api;
    requires consulo.language.code.style.ui.api;
    requires consulo.language.impl;
    requires consulo.language.spellchecker.api;

    requires it.unimi.dsi.fastutil;

    exports consulo.yaml;
    exports consulo.yaml.icon;
    exports consulo.yaml.localize;

    exports org.jetbrains.yaml;
    exports org.jetbrains.yaml.lexer;
    exports org.jetbrains.yaml.psi;

    opens org.jetbrains.yaml.resolve to consulo.application.impl;

    opens org.jetbrains.yaml.formatter to consulo.util.xml.serializer;
}