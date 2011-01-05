package org.infinispan.api.tree;

import org.infinispan.Cache;
import org.infinispan.config.Configuration;
import org.infinispan.config.GlobalConfiguration;
import org.infinispan.context.Flag;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.test.MultipleCacheManagersTest;
import org.infinispan.transaction.tm.DummyTransactionManager;
import org.infinispan.tree.Fqn;
import org.infinispan.tree.TreeCache;
import org.infinispan.tree.TreeCacheFactory;
import org.infinispan.tree.TreeCacheImpl;
import org.infinispan.util.concurrent.IsolationLevel;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.transaction.Status;
import javax.transaction.TransactionManager;
import java.util.Properties;

/**
 * @author <a href="mailto:konstantin.kuzmin@db.com">Konstantin Kuzmin</a>
 * @author Galder Zamarreño
 * @since 4.2
 */
@Test(groups = "functional", testName = "api.tree.FlagTest")
public class FlagTest extends MultipleCacheManagersTest {
   private Cache cache1, cache2;
   private TreeCache treeCache1, treeCache2;
   private static final String KEY = "key";

   @Override
   protected void createCacheManagers() throws Throwable {
      Configuration c = getDefaultClusteredConfig(Configuration.CacheMode.INVALIDATION_SYNC, true);
      c.setInvocationBatchingEnabled(true);
      createClusteredCaches(2, "invalidatedFlagCache", c);
      cache1 = cache(0, "invalidatedFlagCache");
      cache2 = cache(1, "invalidatedFlagCache");
      TreeCacheFactory tcf = new TreeCacheFactory();
      treeCache1 = tcf.createTreeCache(cache1);
      treeCache2 = tcf.createTreeCache(cache2);
   }

   public void testTreeCacheLocalPut() throws Exception {
      final Fqn fqn = Fqn.fromElements("TEST");
      treeCache1.put(fqn, KEY, "1", Flag.CACHE_MODE_LOCAL);
      treeCache2.put(fqn, KEY, "2", Flag.CACHE_MODE_LOCAL);
      assert "2".equals(treeCache2.get(fqn, KEY)) : "treeCache2 was updated locally";
      assert "1".equals(treeCache1.get(fqn, KEY)) : "treeCache1 should not be invalidated in case of LOCAL put in treeCache2";

      String fqnString = "fqnAsString";
      treeCache1.put(fqnString, KEY, "3", Flag.CACHE_MODE_LOCAL);
      treeCache2.put(fqnString, KEY, "4", Flag.CACHE_MODE_LOCAL);
      assert "4".equals(treeCache2.get(fqnString, KEY)) : "treeCache2 was updated locally";
      assert "3".equals(treeCache1.get(fqnString, KEY)) : "treeCache1 should not be invalidated in case of LOCAL put in treeCache2";
   }

}
