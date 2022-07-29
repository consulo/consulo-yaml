/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.yaml;

import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;

import javax.annotation.Nonnull;

@ExtensionImpl
public class YAMLSpellcheckerStrategy extends SpellcheckingStrategy {
  @RequiredReadAction
  @Nonnull
  @Override
  public Tokenizer getTokenizer(final PsiElement element) {
    final ASTNode node = element.getNode();
    if (node != null){
      final IElementType type = node.getElementType();
      if (type == YAMLTokenTypes.SCALAR_TEXT ||
          type == YAMLTokenTypes.SCALAR_STRING ||
          type == YAMLTokenTypes.SCALAR_DSTRING ||
          type == YAMLTokenTypes.COMMENT) {
        return TEXT_TOKENIZER;
      }
    }
    return super.getTokenizer(element);
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return YAMLLanguage.INSTANCE;
  }
}