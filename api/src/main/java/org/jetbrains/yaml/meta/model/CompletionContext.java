// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.yaml.meta.model;

import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.CompletionType;
import jakarta.annotation.Nonnull;

public interface CompletionContext {
  @Nonnull
  CompletionType getCompletionType();

  int getInvocationCount();

  @Nonnull
  String getCompletionPrefix();

  @Nonnull
  CompletionResultSet getCompletionResultSet();
}
