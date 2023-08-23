package test;

import static org.junit.Assert.*;

import org.junit.Test;

import valextractor.handlers.ComparePatchHandler;
import valextractor.handlers.ComparePreAndNowPatch;

public class ComparePreAndNowPatchTest {

	@Test
	public void test() {
		new ComparePreAndNowPatch("",
				"",
				"Closure","Chart","Lang","Math","Mockito","Time").run("ours");
	}

}
