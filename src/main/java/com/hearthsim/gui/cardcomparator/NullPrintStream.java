package com.hearthsim.gui.cardcomparator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class NullPrintStream extends PrintStream
{

	public NullPrintStream()
	{
		super(new NullByteArrayOutputStream());
	}

	private static class NullByteArrayOutputStream extends
	ByteArrayOutputStream
	{

		@Override
		public void write(final int b)
		{
			// do nothing
		}

		@Override
		public void write(final byte[] b, final int off, final int len)
		{
			// do nothing
		}

		@Override
		public void writeTo(final OutputStream out) throws IOException
		{
			// do nothing
		}

	}

}