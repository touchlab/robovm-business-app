package org.robovm.samples.contractr.core.common;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by kgalligan on 2/14/16.
 */
public class IOUtils
{

    /**
     * Close the closeable if not null and ignore any exceptions.
     */
    public static void closeQuietly(Closeable closeable)
    {
        if (closeable != null)
        {
            try
            {
                closeable.close();
            }
            catch (IOException e)
            {
                // ignored
            }
        }
    }
}
