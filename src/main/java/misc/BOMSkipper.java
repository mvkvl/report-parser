package misc;

import java.io.IOException;
import java.io.Reader;

public class BOMSkipper
{
    public static void skip(Reader reader) throws IOException
    {
        reader.mark(1);
        char[] possibleBOM = new char[1];
        if (reader.ready()){ 
        	reader.read(possibleBOM); 
        }
        if (possibleBOM[0] != '\ufeff') {
            reader.reset();
        }
    }
}