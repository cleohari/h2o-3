package water.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import water.Scope;
import water.TestUtil;
import water.fvec.Frame;
import water.runner.H2ORunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * This demonstrates a Sort issue when executed concurrently - concurrent sort is used by Isotonic Regression in CV
 * See <a href="https://h2oai.atlassian.net/browse/PUBDEV-8830">https://h2oai.atlassian.net/browse/PUBDEV-8830</a>
 */
@RunWith(H2ORunner.class)
public class SortConcurrentTest extends TestUtil {

    @After
    public void cleanUpDKV() {
        new TestUtil.DKVCleaner().doAllNodes(); // leaked keys are inevitable in this kind of test
    }

    @Test
    public void testSequentialCVSort() {
        try {
            Scope.enter();
            Frame f = parseAndTrackTestFile("smalldata/logreg/prostate.csv");
            SortModel.SortParameters sp = new SortModel.SortParameters();
            sp._train = f._key;
            sp._nfolds = 5;
            sp._nModelsInParallel = 1;
            SortModel m = new Sort(sp).trainModel().get();
            Assert.assertNotNull(m);
            m.delete();
        } finally {
            Scope.exit();
        }
    }

    @Test
    public void testParallelCVSort() {
        try {
            Scope.enter();
            Frame f = parseAndTrackTestFile("smalldata/logreg/prostate.csv");
            SortModel.SortParameters sp = new SortModel.SortParameters();
            sp._train = f._key;
            sp._nfolds = 5;
            sp._nModelsInParallel = 5;
            SortModel m = new Sort(sp).trainModel().get();
            Assert.assertNotNull(m);
            m.delete();
        } catch (Exception e) {
            if (TestUtil.isCI()) {
                e.printStackTrace();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(os);
                e.printStackTrace(ps);
                Assert.assertTrue(os.toString().contains("water.rapids.")); // clunky but effective way to prove the exception originated from Merge
            } else {
                throw e; // in local development just fail
            }
        } finally {
            Scope.exit();
        }
    }

}
