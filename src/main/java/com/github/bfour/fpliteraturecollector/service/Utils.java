package com.github.bfour.fpliteraturecollector.service;

import java.io.IOException;
import java.io.StringWriter;

import org.python.util.PythonInterpreter;

public class Utils {

	@Deprecated
	public static boolean pythonIsInstalled() throws IOException {
		
		PythonInterpreter interpreter = new PythonInterpreter();
		PythonInterpreter.initialize(System.getProperties(),
				System.getProperties(), new String[] { "-c 1",
						"--author \"albert einstein\"",
						"--phrase \"quantum theory\"" });
		String scriptname = "scholar.py-master/scholar.py";

		StringWriter out = new StringWriter();
		interpreter.setOut(out);
		interpreter.execfile(scriptname);
		
		System.out.println(out.toString());
		
		return false;
		
//		CommandLine cmdLine = new CommandLine("python");
//		cmdLine.addArgument("--version");
//		
//		DefaultExecutor executor = new DefaultExecutor();
//		executor.setExitValue(1);
//		
//		ExecuteWatchdog watchdog = new ExecuteWatchdog(4861);
//		executor.setWatchdog(watchdog);
//		
//	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//	    PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
//	    executor.setStreamHandler(streamHandler);
//		
//		try {
//			int exitValue = executor.execute(cmdLine);
//			System.out.println(outputStream.toString());
//			return true; // TODO
//		} catch (ExecuteException e) {
//			return false;
//		}
		
	}
}
