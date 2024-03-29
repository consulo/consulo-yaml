package org.jetbrains.yaml.structureView;

import consulo.application.AllIcons;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.navigation.ItemPresentation;
import consulo.ui.image.Image;
import org.jetbrains.yaml.psi.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
  public StructureViewTreeElement[] getChildren() {
    final Collection<? extends YAMLPsiElement> children;
    if (myElement instanceof YAMLFile) {
      children = ((YAMLFile)myElement).getDocuments();
    }
    else if (myElement instanceof YAMLDocument) {
      children = getChildrenForValue(((YAMLDocument)myElement).getTopLevelValue());
    }
    else if (myElement instanceof YAMLSequenceItem) {
      children = getChildrenForValue(((YAMLSequenceItem)myElement).getValue());
    }
    else if (myElement instanceof YAMLKeyValue) {
      children = getChildrenForValue(((YAMLKeyValue)myElement).getValue());
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
    if (element instanceof YAMLMapping) {
      return ((YAMLMapping)element).getKeyValues();
    }
    if (element instanceof YAMLSequence) {
      return ((YAMLSequence)element).getItems();
    }
    return Collections.emptyList();
  }
  


  @Nonnull
  public ItemPresentation getPresentation() {
    if (myElement instanceof YAMLKeyValue){
      final YAMLKeyValue kv = (YAMLKeyValue)myElement;
      return new ItemPresentation() {
        public String getPresentableText() {
          return kv.getKeyText();
        }

        public String getLocationString() {
          if (kv.getValue() instanceof YAMLScalar) {
            return kv.getValueText();
          }
          else {
            return null;
          }
        }

        public Image getIcon() {
          final YAMLValue value = kv.getValue();
          return value instanceof YAMLScalar ? IconDescriptorUpdaters.getIcon(kv, 0) : AllIcons.Nodes.Tag;
        }
      };
    }
    if (myElement instanceof YAMLDocument){
      return new ItemPresentation() {
        public String getPresentableText() {
          return "YAML document";
        }

        public String getLocationString() {
          return null;
        }

        public Image getIcon() {
          return AllIcons.Nodes.Tag;
        }
      };
    }
    if (myElement instanceof YAMLSequenceItem) {
      final YAMLSequenceItem item = ((YAMLSequenceItem)myElement);
      return new ItemPresentation() {
        @Nullable
        @Override
        public String getPresentableText() {
          if (item.getValue() instanceof YAMLScalar) {
            return ((YAMLScalar)item.getValue()).getTextValue();
          }
          else {
            return "Sequence Item";
          }
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

  public YAMLPsiElement getValue() {
    return myElement;
  }

  public void navigate(boolean requestFocus) {
    myElement.navigate(requestFocus);
  }

  public boolean canNavigate() {
    return myElement.canNavigate();
  }

  public boolean canNavigateToSource() {
    return myElement.canNavigateToSource();
  }
}

