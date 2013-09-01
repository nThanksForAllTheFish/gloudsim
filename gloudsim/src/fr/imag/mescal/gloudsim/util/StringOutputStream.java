package fr.imag.mescal.gloudsim.util;
import java.io.IOException;
import java.io.OutputStream;

public class StringOutputStream extends OutputStream
{

    public void write(int b)
        throws IOException
    {
        mBuf.append((char)b);
    }

    public String getString()
    {
        return mBuf.toString();
    }

    StringBuilder mBuf;

    public StringOutputStream()
    {
        mBuf = new StringBuilder();
    }
}