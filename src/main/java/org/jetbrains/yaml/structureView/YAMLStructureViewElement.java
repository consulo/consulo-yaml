package org.jetbrains.yaml.structureView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLPsiElement;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLSequence;
import org.jetbrains.yaml.psi.YAMLSequenceItem;
import org.jetbrains.yaml.psi.YAMLValue;
import com.intellij.icons.AllIcons;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.navigation.ItemPresentation;
import consulo.ide.IconDescriptorUpdaters;
import consulo.ui.image.Image;

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

