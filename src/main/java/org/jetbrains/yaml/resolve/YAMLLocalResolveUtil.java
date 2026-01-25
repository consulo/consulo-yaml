// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.yaml.resolve;

import consulo.application.util.CachedValueProvider;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.LanguageCachedValueUtil;
import jakarta.annotation.Nonnull;
import org.jetbrains.yaml.psi.YAMLAlias;
import org.jetbrains.yaml.psi.YAMLAnchor;
import org.jetbrains.yaml.psi.YamlRecursivePsiElementVisitor;

import java.util.*;

public final class YAMLLocalResolveUtil {
    private YAMLLocalResolveUtil() {
    }

    /**
     * Calculates reference map for a file.
     *
     * @return A map: alias → referenced anchor.
     */
    public static @Nonnull Map<YAMLAlias, YAMLAnchor> getResolveAliasMap(@Nonnull PsiFile file) {
        return getResolveData(file).myResolveMap;
    }

    /**
     * This method is useful for completion. It calculates a special collection of anchors in a given file.
     * For every anchor name the result will contain only the first anchor with that name.
     */
    public static @Nonnull Collection<YAMLAnchor> getFirstAnchorDefs(@Nonnull PsiFile file) {
        return getResolveData(file).myFirstDefs;
    }

    private static @Nonnull YAMLAliasResolveResult getResolveData(@Nonnull PsiFile file) {
        return LanguageCachedValueUtil.getCachedValue(file, () -> {
            Map<YAMLAlias, YAMLAnchor> resolveMap = new HashMap<>();
            Map<String, YAMLAnchor> defMap = new HashMap<>();

            // store first definitions: need for completion
            Map<String, YAMLAnchor> firstDefMap = new HashMap<>();

            file.accept(new YamlRecursivePsiElementVisitor() {
                @Override
                public void visitAnchor(@Nonnull YAMLAnchor anchor) {
                    defMap.put(anchor.getName(), anchor);
                    firstDefMap.putIfAbsent(anchor.getName(), anchor);
                }

                @Override
                public void visitAlias(@Nonnull YAMLAlias alias) {
                    String name = alias.getAliasName();
                    YAMLAnchor anchor = defMap.get(name);
                    if (anchor != null) {
                        resolveMap.put(alias, anchor);
                    }
                }
            });
            Set<YAMLAnchor> firstDefs = new HashSet<>(firstDefMap.values());
            YAMLAliasResolveResult result = new YAMLAliasResolveResult(resolveMap, firstDefs);
            return CachedValueProvider.Result.create(result, file);
        });
    }

    private static class YAMLAliasResolveResult {
        final @Nonnull Map<YAMLAlias, YAMLAnchor> myResolveMap;
        final @Nonnull Set<YAMLAnchor> myFirstDefs;

        YAMLAliasResolveResult(@Nonnull Map<YAMLAlias, YAMLAnchor> map, @Nonnull Set<YAMLAnchor> firstDefs) {
            myResolveMap = Collections.unmodifiableMap(map);
            myFirstDefs = Collections.unmodifiableSet(firstDefs);
        }
    }
}
