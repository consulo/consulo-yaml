// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.meta.model;

import consulo.codeEditor.Editor;
import consulo.codeEditor.util.EditorModificationUtil;
import consulo.language.codeStyle.CodeStyle;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.logging.Logger;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import consulo.util.lang.StringUtil;
import consulo.yaml.localize.YAMLLocalize;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.formatter.YAMLCodeStyleSettings;
import org.jetbrains.yaml.psi.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class YamlMetaType {
    private final @Nonnull String myTypeName;
    private @Nullable String myDisplayName;

    protected YamlMetaType(@NonNls @Nonnull String typeName, @NonNls @Nonnull String displayName) {
        myTypeName = typeName;
        myDisplayName = displayName;
    }

    protected YamlMetaType(@NonNls @Nonnull String typeName) {
        myTypeName = typeName;
        myDisplayName = null;
    }

    @Contract(pure = true)
    public final @Nonnull String getTypeName() {
        return myTypeName;
    }

    @Contract(pure = true)
    public @Nonnull String getDisplayName() {
        return myDisplayName == null ? myTypeName : myDisplayName;
    }

    @Contract(pure = true)
    public @Nonnull Image getIcon() {
        return PlatformIconGroup.nodesOperatorclass();
    }

    /**
     * @deprecated initialise the {@code displayName} via constructor instead.
     */
    @Deprecated(forRemoval = true)
    public void setDisplayName(@NonNls @Nonnull String displayName) {
        if (myDisplayName != null) {
            Logger.getInstance(YamlMetaType.class)
                .error("reinitialising 'myDisplayName' with value '" + displayName + "', previous value: '" + myDisplayName + "'. " +
                    "Please avoid calling the `setDisplayName` method out of initialisation step, or better switch to constructor " +
                    "'YamlMetaType(String typeName, String displayName)' for initialisation");
        }
        myDisplayName = displayName;
    }

    public abstract @Nullable Field findFeatureByName(@Nonnull String name);

    /**
     * Computes the set of {@link Field#getName()}s which are missing in the given set of the existing keys.
     *
     * @see org.jetbrains.yaml.meta.impl.YamlMissingKeysInspectionBase
     */
    public abstract @Nonnull List<String> computeMissingFields(@Nonnull Set<String> existingFields);

    /**
     * Computes the list of fields that should be included into the completion list for the key completion inside the given mapping,
     * which is guaranteed to be typed by <code>this<code/> type.
     * <p/>
     * It is assumed that the list does not depend on the insertion position inside the <code>existingMapping</code>.
     * As an optimisation, the result list may include fields which are already present in the <code>existingMapping</code>, the additional
     * filtering will be done by the caller.
     *
     * @see org.jetbrains.yaml.meta.impl.YamlMetaTypeCompletionProviderBase
     */
    public abstract @Nonnull List<Field> computeKeyCompletions(@Nullable YAMLMapping existingMapping);

    public void validateKey(@Nonnull YAMLKeyValue keyValue, @Nonnull ProblemsHolder problemsHolder) {
        //
    }

    public void validateValue(@Nonnull YAMLValue value, @Nonnull ProblemsHolder problemsHolder) {
        //
    }

    public boolean isSupportedTag(@Nonnull String tag) {
        return true;
    }

    /**
     * Validates the value not only at current level but also goes recursively through its children if it's a compound YAML value
     * TODO: unfinished experimental feature to support JSON Schema like features (anyOf, oneOf, allOf, not). Used in Kubernetes plugin. WIP
     */
    public void validateDeep(@Nonnull YAMLValue value, @Nonnull ProblemsHolder problemsHolder) {
        validateValue(value, problemsHolder);

        // TODO a case for sequence

        if (value instanceof YAMLMapping mapping) {

            Collection<YAMLKeyValue> keyValues = mapping.getKeyValues();

            // check for missing keys
            // TODO reuse with YamlMissingKeysInspectionBase
            final Collection<String> missingKeys =
                computeMissingFields(keyValues.stream().map(it -> it.getKeyText().trim()).collect(Collectors.toSet()));

            if (!missingKeys.isEmpty()) {
                String msg = YAMLLocalize.yamlmissingkeysinspectionbaseMissingKeys(String.join(", ", missingKeys)).get();
                problemsHolder.registerProblem(mapping, msg, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }

            for (YAMLKeyValue keyValue : keyValues) {
                String featureName = keyValue.getKeyText().trim();

                if (featureName.isEmpty()) {
                    continue;
                }

                Field feature = findFeatureByName(featureName);
                if (feature == null) {
                    String msg = YAMLLocalize.yamlunknownkeysinspectionbaseUnknownKey(keyValue.getKeyText()).get();
                    final PsiElement key = keyValue.getKey();
                    assert key != null;
                    problemsHolder.registerProblem(key, msg, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
                    continue;
                }

                YAMLValue subValue = keyValue.getValue();

                if (subValue == null) {
                    if (!feature.isEmptyValueAllowed()) {
                        // TODO report problem
                    }

                    continue;
                }

                final Field.Relation relation;
                if (subValue instanceof YAMLScalar) {
                    relation = Field.Relation.SCALAR_VALUE;
                }
                else if (subValue instanceof YAMLSequence) {
                    relation = Field.Relation.SEQUENCE_ITEM;
                }
                else {
                    relation = Field.Relation.OBJECT_CONTENTS;
                }

                YamlMetaType subType = feature.getType(relation);

                if (!(subValue instanceof YAMLSequence)) {
                    subType.validateDeep(subValue, problemsHolder);
                }
                else {
                    List<YAMLSequenceItem> sequenceItems = ((YAMLSequence) subValue).getItems();
                    for (YAMLSequenceItem item : sequenceItems) {
                        YAMLValue itemValue = item.getValue();

                        if (itemValue == null) {
                            if (!feature.isEmptyValueAllowed()) {
                                // TODO report problem
                            }

                            continue;
                        }

                        subType.validateDeep(itemValue, problemsHolder);
                    }
                }
            }
        }
    }


    public @Nonnull List<? extends LookupElement> getValueLookups(@Nonnull YAMLScalar insertedScalar, @Nullable CompletionContext completionContext) {
        return Collections.emptyList();
    }

    /**
     * Builds the insertion markup after the feature name, that is, the part starting from ":".
     * <p>
     * E.g for an integer feature with default value, the insertion suffix markup may look like ": 42&lt;crlf&gt;></>", representing the
     * fill insertion markup of "theAnswer: 42&lt;crlf&gt;"
     */
    public abstract void buildInsertionSuffixMarkup(@Nonnull YamlInsertionMarkup markup,
                                                    @Nonnull Field.Relation relation,
                                                    @Nonnull ForcedCompletionPath.Iteration iteration);

    protected static void buildCompleteKeyMarkup(@Nonnull YamlInsertionMarkup markup,
                                                 @Nonnull Field feature,
                                                 @Nonnull ForcedCompletionPath.Iteration iteration) {
        markup.append(feature.getName());
        Field.Relation defaultRelation = feature.getDefaultRelation();
        YamlMetaType defaultType = feature.getType(defaultRelation);
        defaultType.buildInsertionSuffixMarkup(markup, defaultRelation, iteration);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + myTypeName + "@" + Integer.toHexString(hashCode());
    }

    public static class YamlInsertionMarkup {
        public static final String CRLF_MARKUP = "<crlf>";
        public static final String CARET_MARKUP = "<caret>";
        public static final String SEQUENCE_ITEM_MARKUP = "- ";

        private final StringBuilder myOutput = new StringBuilder();
        private final String myTabSymbol;
        private int myLevel;
        private boolean myCaretAppended;
        private final YAMLCodeStyleSettings mySettings;

        public YamlInsertionMarkup(@Nonnull InsertionContext context) {
            this(getTabSymbol(context), CodeStyle.getCustomSettings(context.getFile(), YAMLCodeStyleSettings.class));
        }

        public YamlInsertionMarkup(@Nonnull String tabSymbol, YAMLCodeStyleSettings settings) {
            myTabSymbol = tabSymbol;
            mySettings = settings;
        }

        public void append(@Nonnull String text) {
            myOutput.append(text);
        }

        public void newLineAndTabs() {
            newLineAndTabs(false);
        }

        public void newLineAndTabs(boolean withSequenceItemMark) {
            if (withSequenceItemMark) {
                assert myLevel > 0;
            }

            append(CRLF_MARKUP);
            if (withSequenceItemMark) {
                append(tabs(myLevel - 1));
                append(sequenceItemPrefix());
            }
            else {
                append(tabs(myLevel));
            }
        }

        private @Nonnull String sequenceItemPrefix() {
            String result = SEQUENCE_ITEM_MARKUP;
            if (myTabSymbol.length() > result.length()) {
                result += myTabSymbol.substring(result.length());
            }
            return result;
        }

        public void doTabbedBlockForSequenceItem(Runnable doWhenTabbed) {
            var indent = mySettings.INDENT_SEQUENCE_VALUE ? 2 : 1;

            doTabbedBlock(indent, () -> {
                newLineAndTabs(true);
                doWhenTabbed.run();
            });
        }

        public void doTabbedBlockForSequenceItem() {
            doTabbedBlockForSequenceItem(() -> {
            });
        }

        public void appendCaret() {
            if (!myCaretAppended) {
                append(CARET_MARKUP);
            }
            myCaretAppended = true;
        }

        public String getMarkup() {
            return myOutput.toString();
        }

        public void increaseTabs(final int indent) {
            assert indent > 0;
            myLevel += indent;
        }

        public void doTabbedBlock(final int indent, final @Nonnull Runnable doWhenTabbed) {
            increaseTabs(indent);
            try {
                doWhenTabbed.run();
            }
            finally {
                decreaseTabs(indent);
            }
        }

        public @Nonnull String getTabSymbol() {
            return myTabSymbol;
        }

        public void decreaseTabs(int indent) {
            assert indent <= myLevel;
            myLevel -= indent;
        }

        private String tabs(int level) {
            return StringUtil.repeat(myTabSymbol, level);
        }

        private static @Nonnull String getTabSymbol(@Nonnull InsertionContext context) {
            return StringUtil.repeatSymbol(' ', CodeStyle.getIndentSize(context.getFile()));
        }

        public void insertStringAndCaret(Editor editor, String commonPadding) {
            String insertionMarkup = getMarkup();
            String suffixWithCaret = insertionMarkup.replace(CRLF_MARKUP, "\n" + commonPadding);
            int caretIndex = suffixWithCaret.indexOf(CARET_MARKUP);
            String suffix = suffixWithCaret.replace(CARET_MARKUP, "");

            EditorModificationUtil.insertStringAtCaret(editor, suffix, false, true, caretIndex);
        }
    }

    public static final class ForcedCompletionPath {
        private static final Iteration OFF_PATH_ITERATION = new OffPathIteration();
        private static final Iteration NULL_ITERATION = new NullIteration();
        private static final ForcedCompletionPath NULL_PATH = new ForcedCompletionPath(null);

        private final @Nullable List<Field> myCompletionPath;

        public static @Nonnull ForcedCompletionPath forDeepCompletion(final @Nonnull List<Field> completionPath) {
            return new ForcedCompletionPath(completionPath);
        }

        public static @Nonnull ForcedCompletionPath nullPath() {
            return NULL_PATH;
        }

        private ForcedCompletionPath(final @Nullable List<Field> completionPath) {
            myCompletionPath = completionPath;
        }

        public @Nonnull String getName() {
            if (myCompletionPath == null) {
                return "<null>";
            }

            return myCompletionPath.stream().map(Field::getName).collect(Collectors.joining("."));
        }

        public @Nullable YamlMetaType getFinalizingType() {
            if (myCompletionPath == null || myCompletionPath.size() < 2) {
                return null;
            }

            return myCompletionPath.get(myCompletionPath.size() - 2).getDefaultType();
        }

        public @Nullable Field getFinalizingField() {
            if (myCompletionPath == null || myCompletionPath.isEmpty()) {
                return null;
            }

            return myCompletionPath.get(myCompletionPath.size() - 1);
        }

        public Iteration start() {
            return myCompletionPath != null ? new OnPathIteration(1) : NULL_ITERATION;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "(" + getName() + ")";
        }

        public interface Iteration {
            boolean isEndOfPathReached();

            boolean isNextOnPath(@Nonnull Field field);

            @Nonnull
            Iteration nextIterationFor(@Nonnull Field field);
        }

        private static class OffPathIteration implements Iteration {
            @Override
            public boolean isNextOnPath(@Nonnull Field field) {
                return false;
            }

            @Override
            public boolean isEndOfPathReached() {
                return false;
            }

            @Override
            public @Nonnull Iteration nextIterationFor(@Nonnull Field field) {
                return this;
            }
        }

        private static class NullIteration implements Iteration {
            @Override
            public boolean isNextOnPath(@Nonnull Field field) {
                return false;
            }

            @Override
            public boolean isEndOfPathReached() {
                return true;
            }

            @Override
            public @Nonnull Iteration nextIterationFor(@Nonnull Field field) {
                return this;
            }
        }

        private final class OnPathIteration implements Iteration {
            private final int myPosition;

            private OnPathIteration(int position) {
                myPosition = position;
            }

            @Override
            public boolean isNextOnPath(@Nonnull Field field) {
                assert myCompletionPath != null;
                return !isEndOfPathReached() && field.equals(myCompletionPath.get(myPosition));
            }

            @Override
            public boolean isEndOfPathReached() {
                assert myCompletionPath != null;
                return myPosition >= myCompletionPath.size();
            }

            @Override
            public @Nonnull Iteration nextIterationFor(@Nonnull Field field) {
                assert myCompletionPath != null;
                return isEndOfPathReached() || field.equals(myCompletionPath.get(myPosition)) ?
                    new OnPathIteration(myPosition + 1) :
                    OFF_PATH_ITERATION;
            }
        }
    }
}
