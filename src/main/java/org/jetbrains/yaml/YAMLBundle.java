package org.jetbrains.yaml;

import consulo.application.CommonBundle;
import consulo.util.lang.ref.SoftReference;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import javax.annotation.Nonnull;
import java.lang.ref.Reference;
import java.util.ResourceBundle;

/**
 * @author oleg
 */
public class YAMLBundle {

  public static String message(@Nonnull @PropertyKey(resourceBundle = BUNDLE) String key, @Nonnull Object... params) {
    return CommonBundle.message(getBundle(), key, params);
  }

  private static Reference<ResourceBundle> ourBundle;
  @NonNls
  private static final String BUNDLE = "messages.YAMLBundle";

  private YAMLBundle() {
  }

  /*
     * This method added for jruby access
     */
  public static String message(@PropertyKey(resourceBundle = BUNDLE) String key) {
    return CommonBundle.message(getBundle(), key);
  }

  private static ResourceBundle getBundle() {
    ResourceBundle bundle = SoftReference.dereference(ourBundle);
    if (bundle == null) {
      bundle = ResourceBundle.getBundle(BUNDLE);
      ourBundle = new SoftReference<>(bundle);
    }
    return bundle;
  }
}
