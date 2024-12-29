package org.jetbrains.yaml.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.psi.util.PsiTreeUtil;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;

public abstract class YAMLMappingImpl extends YAMLCompoundValueImpl implements YAMLMapping {
    public YAMLMappingImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Nonnull
    @Override
    public Collection<YAMLKeyValue> getKeyValues() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, YAMLKeyValue.class);
    }

    @Nullable
    @Override
    public YAMLKeyValue getKeyValueByKey(@Nonnull String keyText) {
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
    @RequiredReadAction
    public void deleteKeyValue(@Nonnull YAMLKeyValue keyValueToDelete) {
        if (keyValueToDelete.getParent() != this) {
            throw new IllegalArgumentException("KeyValue should be the child of this");
        }

        if (keyValueToDelete.getPrevSibling() != null) {
            while (keyValueToDelete.getPrevSibling() != null && !(keyValueToDelete.getPrevSibling() instanceof YAMLKeyValue)) {
                keyValueToDelete.getPrevSibling().delete();
            }
        }
        else {
            while (keyValueToDelete.getNextSibling() != null && !(keyValueToDelete.getNextSibling() instanceof YAMLKeyValue)) {
                keyValueToDelete.getNextSibling().delete();
            }
        }
        keyValueToDelete.delete();
    }

    protected abstract void addNewKey(@Nonnull YAMLKeyValue key);

    @Override
    public String toString() {
        return "YAML mapping";
    }

    @Nonnull
    @Override
    @RequiredReadAction
    public String getTextValue() {
        return "<mapping:" + Integer.toHexString(getText().hashCode()) + ">";
    }
}
