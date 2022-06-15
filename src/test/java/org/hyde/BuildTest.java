package org.hyde;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

public class BuildTest {
   /*
    * lineToConfig tests
    */

   @Test
   public void lineToConfigShouldReturnKV_withCorrectLine() throws IllegalAccessException, IllegalArgumentException,
         InvocationTargetException, NoSuchMethodException, SecurityException {
      String[] expected = new String[] { "key", "value" };

      Build buildObj = new Build();
      String[] res = (String[]) getLineToConfigMethod().invoke(buildObj, "key:value");
      assertArrayEquals(expected, res);
   }

   @Test
   public void lineToConfigShouldReturnNull_withEmptyLine() throws IllegalAccessException, IllegalArgumentException,
         InvocationTargetException, NoSuchMethodException, SecurityException {
      Build buildObj = new Build();
      assertEquals(null, getLineToConfigMethod().invoke(buildObj, ""));
   }

   @Test
   public void lineToConfigShouldReturnNull_withCommentLine() throws IllegalAccessException, IllegalArgumentException,
         InvocationTargetException, NoSuchMethodException, SecurityException {
      Build buildObj = new Build();
      assertEquals(null, getLineToConfigMethod().invoke(buildObj, "# Comment"));
   }

   @Test
   public void lineToConfigShouldReturnNull_withNoSemiColon() throws IllegalAccessException, IllegalArgumentException,
         InvocationTargetException, NoSuchMethodException, SecurityException {
      Build buildObj = new Build();
      assertEquals(null, getLineToConfigMethod().invoke(buildObj, "key"));
   }

   @Test
   public void lineToConfigShouldTrim_withSpaceAfterKey() throws IllegalAccessException, IllegalArgumentException,
         InvocationTargetException, NoSuchMethodException, SecurityException {
      Build buildObj = new Build();
      String[] expected = new String[] { "key", "value" };
      assertArrayEquals(expected, (String[]) getLineToConfigMethod().invoke(buildObj, "key :value"));
   }

   @Test
   public void lineToConfigShouldTrim_withSpaceBeforeValue() throws IllegalAccessException, IllegalArgumentException,
         InvocationTargetException, NoSuchMethodException, SecurityException {
      Build buildObj = new Build();
      String[] expected = new String[] { "key", "value" };
      assertArrayEquals(expected, (String[]) getLineToConfigMethod().invoke(buildObj, "key: value"));
   }

   @Test
   public void lineToConfigShouldTrim_withSpaceAroundSemiColon() throws IllegalAccessException,
         IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
      Build buildObj = new Build();
      String[] expected = new String[] { "key", "value" };
      assertArrayEquals(expected, (String[]) getLineToConfigMethod().invoke(buildObj, "key : value"));
   }

   /*
    * varReplacement tests
    */

   private Method getLineToConfigMethod() throws NoSuchMethodException, SecurityException {
      Method method = Build.class.getDeclaredMethod("lineToConfig", String.class);
      method.setAccessible(true);
      return method;
   }
}
