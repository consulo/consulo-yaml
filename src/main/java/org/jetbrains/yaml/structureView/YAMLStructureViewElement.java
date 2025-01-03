package org.jetbrains.yaml.structureView;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.AllIcons;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.navigation.ItemPresentation;
import consulo.ui.image.Image;
import org.jetbrains.yaml.psi.*;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author oleg
 */
public class YAMLStructureViewElement implements StructureViewTreeElement {
    private final YAMLPsiElement myElement;

    public YAMLStructureViewElement(final YAMLPsiElement element) {
        myElement = element;
    }

    @Nonnull
    @Override
    public StructureViewTreeElement[] getChildren() {
        final Collection<? extends YAMLPsiElement> children;
        if (myElement instanceof YAMLFile file) {
            children = file.getDocuments();
        }
        else if (myElement instanceof YAMLDocument document) {
            children = getChildrenForValue(document.getTopLevelValue());
        }
        else if (myElement instanceof YAMLSequenceItem sequenceItem) {
            children = getChildrenForValue(sequenceItem.getValue());
        }
        else if (myElement instanceof YAMLKeyValue keyValue) {
            children = getChildrenForValue(keyValue.getValue());
        }
        else {
            children = Collections.emptyList();
        }

        final List<StructureViewTreeElement> structureElements = new ArrayList<>();
        for (YAMLPsiElement child : children) {
            structureElements.add(new YAMLStructureViewElement(child));
        }
        return structureElements.toArray(new StructureViewTreeElement[structureElements.size()]);
    }

    @Nonnull
    private static Collection<? extends YAMLPsiElement> getChildrenForValue(@Nullable YAMLPsiElement element) {
        if (element instanceof YAMLMapping mapping) {
            return mapping.getKeyValues();
        }
        if (element instanceof YAMLSequence sequence) {
            return sequence.getItems();
        }
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public ItemPresentation getPresentation() {
        if (myElement instanceof YAMLKeyValue kv) {
            return new ItemPresentation() {
                @Override
                public String getPresentableText() {
                    return kv.getKeyText();
                }

                @Override
                public String getLocationString() {
                    return kv.getValue() instanceof YAMLScalar ? kv.getValueText() : null;
                }

                @Override
                @RequiredReadAction
                public Image getIcon() {
                    final YAMLValue value = kv.getValue();
                    return value instanceof YAMLScalar ? IconDescriptorUpdaters.getIcon(kv, 0) : AllIcons.Nodes.Tag;
                }
            };
        }
        if (myElement instanceof YAMLDocument) {
            return new ItemPresentation() {
                @Override
                public String getPresentableText() {
                    return "YAML document";
                }

                @Override
                public String getLocationString() {
                    return null;
                }

                @Override
                public Image getIcon() {
                    return AllIcons.Nodes.Tag;
                }
            };
        }
        if (myElement instanceof YAMLSequenceItem item) {
            return new ItemPresentation() {
                @Nullable
                @Override
                public String getPresentableText() {
                    return item.getValue() instanceof YAMLScalar scalar ? scalar.getTextValue() : "Sequence Item";
                }

                @Nullable
                @Override
                public String getLocationString() {
                    return null;
                }

                @Nullable
                @Override
                public Image getIcon() {
                    return item.getValue() instanceof YAMLScalar ? AllIcons.Nodes.Property : AllIcons.Nodes.Tag;
                }
            };
        }
        return myElement.getPresentation();
    }

    @Override
    public YAMLPsiElement getValue() {
        return myElement;
    }

    @Override
    public void navigate(boolean requestFocus) {
        myElement.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return myElement.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return myElement.canNavigateToSource();
    }
}
