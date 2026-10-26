/**
 * @author VISTALL
 * @since 29-Jul-22
 */
module org.jetbrains.plugins.yaml {
    requires transitive org.jetbrains.plugins.yaml.api;

    requires consulo.language.ui.api;
    requires consulo.language.code.style.ui.api;
    requires consulo.language.impl;
    requires consulo.language.spellchecker.api;
    requires consulo.language.editor.ui.api;
}
