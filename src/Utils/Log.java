package Utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
    public Logger logger;
    FileHandler handler;

    public Log(String fileName) throws SecurityException, IOException {
        try{
            File file = new File(fileName);
            if(!file.exists()){
                file.createNewFile();
            }
            handler = new FileHandler(fileName,true);
            logger = Logger.getLogger("test");
            logger.addHandler(handler);
            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}