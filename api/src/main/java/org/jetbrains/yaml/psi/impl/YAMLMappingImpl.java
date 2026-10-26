// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.util.PsiTreeUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YamlPsiElementVisitor;

import java.util.Collection;

public abstract class YAMLMappingImpl extends YAMLCompoundValueImpl implements YAMLMapping {
    public YAMLMappingImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Override
    public @Nonnull Collection<YAMLKeyValue> getKeyValues() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, YAMLKeyValue.class);
    }

    @Override
    public @Nullable YAMLKeyValue getKeyValueByKey(@Nonnull String keyText) {
        for (YAMLKeyValue keyValue : getKeyValues()) {
            if (keyText.equals(keyValue.getKeyText())) {
                return keyValue;
            }
        }
        return null;
    }

    @Override
    public void putKeyValue(@Nonnull YAMLKeyValue keyValueToAdd) {
        final YAMLKeyValue existingKey = getKeyValueByKey(keyValueToAdd.getKeyText());
        if (existingKey == null) {
            addNewKey(keyValueToAdd);
        }
        else {
            existingKey.replace(keyValueToAdd);
        }
    }

    @Override
    public void deleteKeyValue(@Nonnull YAMLKeyValue keyValueToDelete) {
        if (keyValueToDelete.getParent() != this) {
            throw new IllegalArgumentException("KeyValue should be the child of this");
        }

        YAMLUtil.deleteSurroundingWhitespace(keyValueToDelete);

        keyValueToDelete.delete();
    }

    protected abstract void addNewKey(@Nonnull YAMLKeyValue key);

    @Override
    public String toString() {
        return "YAML mapping";
    }

    @Override
    public @Nonnull String getTextValue() {
        return "<mapping:" + Integer.toHexString(getText().hashCode()) + ">";
    }

    @Override
    public void accept(@Nonnull PsiElementVisitor visitor) {
        if (visitor instanceof YamlPsiElementVisitor) {
            ((YamlPsiElementVisitor) visitor).visitMapping(this);
        }
        else {
            super.accept(visitor);
        }
    }
}
