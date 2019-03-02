package fr.omegaserv.tower;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MapManager
{
    public static void deleteWorld(File path)
    {
        if (path.isDirectory())
        {
            File[] arrayOfFile;
            int j = (arrayOfFile = path.listFiles()).length;
            for (int i = 0; i < j; i++)
            {
                File subfile = arrayOfFile[i];
                subfile.delete();
            }
            path.delete();
        }
        else
        {
            path.delete();
        }
    }

    public static void Copy(File source, File target)
            throws IOException
    {
        if (source.isDirectory())
        {
            if (!target.exists()) {
                target.mkdir();
            }
            String[] files = source.list();
            String[] arrayOfString1;
            int j = (arrayOfString1 = files).length;
            for (int i = 0; i < j; i++)
            {
                String file = arrayOfString1[i];
                File srcFile = new File(source, file);
                File destFile = new File(target, file);
                Copy(srcFile, destFile);
            }
        }
        else if (!source.getName().equalsIgnoreCase("uid.dat"))
        {
            InputStream in = new FileInputStream(source);
            OutputStream out = new FileOutputStream(target);
            byte[] buffer = new byte['?'];
            int length;
            while ((length = in.read(buffer)) > 0)
            {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        }
    }
}
