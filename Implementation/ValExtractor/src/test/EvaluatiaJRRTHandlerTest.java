package test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import valextractor.handlers.EvaluatiaJRRTHandler;

public class EvaluatiaJRRTHandlerTest {

	@Test
	public void test() {
		try {
//			new EvaluatiaJRRTHandler("Chart").run();
//			new EvaluatiaJRRTHandler("Closure").run();
//			new EvaluatiaJRRTHandler("Lang").run();
//			new EvaluatiaJRRTHandler("Time").run();
//			new EvaluatiaJRRTHandler("Math").run();
//			new EvaluatiaJRRTHandler("Mockito").run();
			new EvaluatiaJRRTHandler("Math").runDiff();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
	}

}
