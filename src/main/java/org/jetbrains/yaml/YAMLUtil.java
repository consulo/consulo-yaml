package org.jetbrains.yaml;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import consulo.yaml.localize.YAMLLocalize;
import org.jetbrains.yaml.psi.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author oleg
 */
public class YAMLUtil {
    @Nonnull
    @SuppressWarnings("unchecked")
    public static String getFullKey(final YAMLKeyValue yamlKeyValue) {
        final StringBuilder builder = new StringBuilder();
        YAMLKeyValue element = yamlKeyValue;
        PsiElement parent;
        while (element != null &&
            (parent = PsiTreeUtil.getParentOfType(element, YAMLKeyValue.class, YAMLDocument.class)) instanceof YAMLKeyValue) {
            if (builder.length() > 0) {
                builder.insert(0, '.');
            }
            builder.insert(0, element.getKeyText());
            element = (YAMLKeyValue)parent;
        }
        return builder.toString();
    }

    @Nonnull
    public static Collection<YAMLKeyValue> getTopLevelKeys(final YAMLFile file) {
        final YAMLValue topLevelValue = file.getDocuments().get(0).getTopLevelValue();
        return topLevelValue instanceof YAMLMapping mapping ? mapping.getKeyValues() : Collections.emptyList();
    }

    @Nullable
    public static YAMLKeyValue getQualifiedKeyInFile(final YAMLFile file, List<String> key) {
        return getQualifiedKeyInDocument(file.getDocuments().get(0), key);
    }

    @Nullable
    public static YAMLKeyValue getQualifiedKeyInDocument(@Nonnull YAMLDocument document, @Nonnull List<String> key) {
        assert key.size() != 0;

        YAMLMapping mapping = ObjectUtil.tryCast(document.getTopLevelValue(), YAMLMapping.class);
        for (int i = 0; i < key.size(); i++) {
            if (mapping == null) {
                return null;
            }

            final YAMLKeyValue keyValue = mapping.getKeyValueByKey(key.get(i));
            if (keyValue == null || i + 1 == key.size()) {
                return keyValue;
            }

            mapping = ObjectUtil.tryCast(keyValue.getValue(), YAMLMapping.class);
        }
        throw new IllegalStateException("Should have returned from the loop");
    }

    @Nullable
    public static YAMLKeyValue getQualifiedKeyInFile(final YAMLFile file, String... key) {
        return getQualifiedKeyInFile(file, Arrays.asList(key));
    }

    @Nullable
    public static YAMLKeyValue findKeyInProbablyMapping(@Nullable YAMLValue node, @Nonnull String keyText) {
        return node instanceof YAMLMapping mapping ? mapping.getKeyValueByKey(keyText) : null;
    }

    @Nullable
    public static Pair<PsiElement, String> getValue(final YAMLFile file, String... key) {
        final YAMLKeyValue record = getQualifiedKeyInFile(file, key);
        if (record != null) {
            final PsiElement psiValue = record.getValue();
            return Pair.create(psiValue, record.getValueText());
        }
        return null;
    }

    //public List<String> getAllKeys(final YAMLFile file){
    //  return getAllKeys(file, ArrayUtil.EMPTY_STRING_ARRAY);
    //}
    //
    //public List<String> getAllKeys(final YAMLFile file, final String[] key){
    //  final YAMLPsiElement record = getQualifiedKeyInFile(file, key);
    //  if (record == null){
    //    return Collections.emptyList();
    //  }
    //  PsiElement psiValue = ((YAMLKeyValue)record).getValue();
    //
    //  final StringBuilder builder = new StringBuilder();
    //  for (String keyPart : key) {
    //    if (builder.length() != 0){
    //      builder.append(".");
    //    }
    //    builder.append(keyPart);
    //  }
    //
    //  final ArrayList<String> list = new ArrayList<String>();
    //
    //  addKeysRec(builder.toString(), psiValue, list);
    //  return list;
    //}

    //private static void addKeysRec(final String prefix, final PsiElement element, final List<String> list) {
    //  if (element instanceof YAMLCompoundValue){
    //    for (YAMLPsiElement child : ((YAMLCompoundValue)element).getYAMLElements()) {
    //      addKeysRec(prefix, child, list);
    //    }
    //  }
    //  if (element instanceof YAMLKeyValue){
    //    final YAMLKeyValue yamlKeyValue = (YAMLKeyValue)element;
    //    final PsiElement psiValue = yamlKeyValue.getValue();
    //    String key = yamlKeyValue.getKeyText();
    //    if (prefix.length() > 0){
    //      key = prefix + "." + key;
    //    }
    //    if (YAMLUtil.isScalarOrEmptyCompoundValue(psiValue)) {
    //      list.add(key);
    //    } else {
    //      addKeysRec(key, psiValue, list);
    //    }
    //  }
    //}

    @RequiredWriteAction
    public YAMLKeyValue createI18nRecord(final YAMLFile file, final String key, final String text) {
        return createI18nRecord(file, key.split("\\."), text);
    }

    @Nullable
    @RequiredWriteAction
    public static YAMLKeyValue createI18nRecord(final YAMLFile file, final String[] key, final String text) {
        final YAMLDocument root = file.getDocuments().get(0);
        assert root != null;
        assert key.length > 0;

        YAMLMapping rootMapping = PsiTreeUtil.findChildOfType(root, YAMLMapping.class);
        if (rootMapping == null) {
            final YAMLFile yamlFile = YAMLElementGenerator.getInstance(file.getProject()).createDummyYamlWithText(key[0] + ":");
            final YAMLMapping mapping = (YAMLMapping)yamlFile.getDocuments().get(0).getTopLevelValue();
            assert mapping != null;
            rootMapping = ((YAMLMapping)root.add(mapping));
        }

        YAMLMapping current = rootMapping;
        final int keyLength = key.length;
        int i;
        for (i = 0; i < keyLength; i++) {
            final YAMLKeyValue existingRec = current.getKeyValueByKey(key[i]);
            if (existingRec != null) {
                final YAMLMapping nextMapping = ObjectUtil.tryCast(existingRec.getValue(), YAMLMapping.class);

                if (nextMapping != null) {
                    current = nextMapping;
                    continue;
                }
            }

            // Calc current key indent

            String indent = StringUtil.repeatSymbol(' ', getIndentInThisLine(current));

            // Generate items
            final StringBuilder builder = new StringBuilder();
            builder.append("---");
            for (int j = i; j < keyLength; j++) {
                builder.append("\n").append(indent);
                builder.append(key[j]).append(":");
                indent += "  ";
            }
            builder.append(" ").append(text);

            // Create dummy mapping
            final YAMLFile fileWithKey = YAMLElementGenerator.getInstance(file.getProject()).createDummyYamlWithText(builder.toString());
            final YAMLMapping dummyMapping = PsiTreeUtil.findChildOfType(fileWithKey.getDocuments().get(0), YAMLMapping.class);
            assert dummyMapping != null && dummyMapping.getKeyValues().size() == 1;

            // Add or replace
            final YAMLKeyValue dummyKeyValue = dummyMapping.getKeyValues().iterator().next();
            current.putKeyValue(dummyKeyValue);

            if (dummyKeyValue.getValue() instanceof YAMLMapping mapping) {
                current = mapping;
            }
            else {
                return dummyKeyValue;
            }
        }

        // Conflict with existing value
        final StringBuilder builder = new StringBuilder();
        final int top = Math.min(i + 1, keyLength);
        for (int j = 0; j < top; j++) {
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(key[j]);
        }
        throw new IncorrectOperationException(YAMLLocalize.newNameConflictsWith(builder.toString()).get());
    }

    //public static void removeI18nRecord(final YAMLFile file, final String[] key){
    //  PsiElement element = getQualifiedKeyInFile(file, key);
    //  while (element != null){
    //    final PsiElement parent = element.getParent();
    //    if (parent instanceof YAMLDocument) {
    //      ((YAMLKeyValue)element).getValue().delete();
    //      return;
    //    }
    //    if (parent instanceof YAMLCompoundValue) {
    //      if (((YAMLCompoundValue)parent).getYAMLElements().size() > 1) {
    //        element.delete();
    //        return;
    //      }
    //    }
    //    element = parent;
    //  }
    //}

    @RequiredWriteAction
    public static PsiElement rename(final YAMLKeyValue element, final String newName) {
        if (newName.contains(".")) {
            throw new IncorrectOperationException(YAMLLocalize.renameWrongName().get());
        }
        if (newName.equals(element.getName())) {
            throw new IncorrectOperationException(YAMLLocalize.renameSameName().get());
        }
        final YAMLKeyValue topKeyValue = YAMLElementGenerator.getInstance(element.getProject()).createYamlKeyValue(newName, "Foo");

        final PsiElement key = element.getKey();
        if (key == null || topKeyValue.getKey() == null) {
            throw new IllegalStateException();
        }
        key.replace(topKeyValue.getKey());
        return element;
    }

    @RequiredReadAction
    public static int getIndentInThisLine(@Nonnull final PsiElement elementInLine) {
        PsiElement currentElement = elementInLine;
        while (currentElement != null) {
            final IElementType type = currentElement.getNode().getElementType();
            if (type == YAMLTokenTypes.EOL) {
                return 0;
            }
            if (type == YAMLTokenTypes.INDENT) {
                return currentElement.getTextLength();
            }

            currentElement = PsiTreeUtil.prevLeaf(currentElement);
        }
        return 0;
    }

    public static int getIndentToThisElement(@Nonnull final PsiElement element) {
        int offset = element.getTextOffset();

        PsiElement currentElement = element;
        while (currentElement != null) {
            final IElementType type = currentElement.getNode().getElementType();
            if (type == YAMLTokenTypes.EOL) {
                return offset - currentElement.getTextOffset() - 1;
            }

            currentElement = PsiTreeUtil.prevLeaf(currentElement);
        }
        return offset;
    }
}
