package com.hearthsim.gui.cardcomparator;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public class JavaProcess
{
	private JavaProcess()
	{
	}

	private static int processCount = 0;
	private static boolean shutdown = false;

	public static void shutDownNow()
	{
		JavaProcess.shutdown = true;
	}

	@SuppressWarnings("rawtypes")
	public static int exec(final Class klass, final String args)
			throws IOException, InterruptedException
	{
		final String javaHome = System.getProperty("java.home");
		final String javaBin = javaHome + File.separator + "bin"
				+ File.separator + "java";
		final String classpath = System.getProperty("java.class.path");
		final String className = klass.getCanonicalName();

		final ProcessBuilder builder = new ProcessBuilder(javaBin, "-Xms3g",
				"-Xmx5g", "-cp", classpath, className, args);
		final File output = new File("process" + JavaProcess.processCount
				+ ".txt");
		output.createNewFile();
		builder.redirectOutput(output);
		JavaProcess.processCount++;
		final Process process = builder.start();
		final OutputStream stdin = process.getOutputStream();
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		while (!JavaProcess.shutdown)
			process.waitFor(100, TimeUnit.MILLISECONDS);
		if (JavaProcess.shutdown)
		{
			try
			{
				stdin.write(3);
				stdin.flush();
				stdin.close();
			} catch (final IOException e1)
			{
				e1.printStackTrace();
			}
			return 1;
		}

		return process.exitValue();
	}

	public static class ExecuteProcess implements Runnable
	{
		@SuppressWarnings("rawtypes")
		private final Class classToExecute;
		private final String args;

		@SuppressWarnings("rawtypes")
		public ExecuteProcess(final Class klass, final String args)
		{
			this.args = args;
			this.classToExecute = klass;
		}

		@Override
		public void run()
		{
			try
			{
				JavaProcess.exec(this.classToExecute, this.args);
			} catch (final IOException e)
			{
				e.printStackTrace();
			} catch (final InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
