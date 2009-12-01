package test.thread;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.internal.annotations.Maps;

import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Test that classes with dependent methods are still run in different threads
 * and in the correct order.
 */
public class MultiThreadedDependentTest extends SimpleBaseTest {

  /**
   * Make sure that the topological order is preserved and that if
   * the TestNG runner is configured to run n threads, the dependent
   * methods are using these n threads.
   */
  private void assertOrder(List<String> methods) {
    List<String> expectedMethods = Arrays.asList(new String[] {
      "a1", "a2", "b1", "b2", "b3", "b4", "c1", "d", "x", "y", "z", "t"
    });
    int size = expectedMethods.size();
    Assert.assertEquals(methods.size(), size);
    for (String em : expectedMethods) {
      Assert.assertTrue(methods.contains(em));
    }
    Map<String, Boolean> map = Maps.newHashMap();
    for (String m : methods) {
      map.put(m, Boolean.TRUE);
      if ("b1".equals(m) || "b2".equals(m) || "b3".equals(m) || "b4".equals(m)) {
        Assert.assertTrue(map.get("a1"));
        Assert.assertTrue(map.get("a2"));
      }
      if ("d".equals(m)) {
        Assert.assertTrue(map.get("a1"));
        Assert.assertTrue(map.get("a2"));
      }
      if ("c1".equals(m)) {
        Assert.assertTrue(map.get("b1"));
        Assert.assertTrue(map.get("b2"));
      }
    }
    Assert.assertEquals(map.size(), size);
    for (String m : map.keySet()) {
      Assert.assertTrue(map.get(m));
    }
  }

  @Test
  public void test2Threads() {
    test(2);
  }
  
  @Test
  public void test3Threads() {
    test(3);
  }

  private void test(int threadCount) {
    Helper.reset();
    MultiThreadedDependentSampleTest.m_methods = Lists.newArrayList();
    TestNG tng = create(MultiThreadedDependentSampleTest.class);
    tng.setThreadCount(threadCount);
    tng.setParallel("methods");
    Map<Long, Long> map = Helper.getMap(MultiThreadedDependentSampleTest.class.getName());
    synchronized(map) {
      tng.run();
      Assert.assertEquals(map.size(), threadCount);
      assertOrder(MultiThreadedDependentSampleTest.m_methods);
    }
  }
}