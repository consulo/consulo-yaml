package org.jetbrains.yaml.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import javax.annotation.Nonnull;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLScalarList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oleg
 * @see <http://www.yaml.org/spec/1.2/spec.html#id2795688>
 */
public class YAMLScalarListImpl extends YAMLBlockScalarImpl implements YAMLScalarList {
  public YAMLScalarListImpl(@Nonnull final ASTNode node) {
    super(node);
  }

  @Nonnull
  @Override
  protected IElementType getContentType() {
    return YAMLTokenTypes.SCALAR_LIST;
  }

  @Nonnull
  @Override
  public String getTextValue() {
    return super.getTextValue() + "\n";
  }

  @Nonnull
  @Override
  protected String getRangesJoiner(@Nonnull CharSequence text, @Nonnull List<TextRange> contentRanges, int indexBefore) {
    return "\n";
  }

  @Override
  protected List<Pair<TextRange, String>> getEncodeReplacements(@Nonnull CharSequence input) throws IllegalArgumentException {
    if (!StringUtil.endsWithChar(input, '\n')) {
      throw new IllegalArgumentException("Should end with a line break");
    }

    int indent = locateIndent();
    if (indent == 0) {
      indent = YAMLUtil.getIndentToThisElement(this) + DEFAULT_CONTENT_INDENT;
    }
    final String indentString = StringUtil.repeatSymbol(' ', indent);

    final List<Pair<TextRange, String>> result = new ArrayList<>();
    for (int i = 0; i < input.length(); ++i) {
      if (input.charAt(i) == '\n') {
        result.add(Pair.create(TextRange.from(i, 1), "\n" + indentString));
      }
    }

    return result;
  }

  @Override
  public String toString() {
    return "YAML scalar list";
  }
}