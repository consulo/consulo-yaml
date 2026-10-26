import org.jspecify.annotations.NullMarked;

/**
 * @author VISTALL
 * @since 2026-10-26
 */
@NullMarked
module org.jetbrains.plugins.yaml.json.schema {
    requires org.jetbrains.plugins.yaml.api;

    requires consulo.json.api;
    requires consulo.json.scheme.api;
    requires consulo.json.scheme.impl;
}
