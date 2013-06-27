package asarnow.jce;

import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.io.FileParsingParameters;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: da
 * Date: 7/8/11
 * Time: 10:24 PM
 */
public class Utility {

    public static List<String> listFromFile(String listFilePath){
        List<String> list = new ArrayList<String>();
        try{
            BufferedReader br;
            br = new BufferedReader(new FileReader(listFilePath));
            String line;
            while ( ( line = br.readLine() ) != null ) {
                if ( !line.startsWith("#") ){
                    list.add(line);
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static AtomCache initAtomCache(String pdbPath, Boolean divided) {
        AtomCache cache = new AtomCache(pdbPath, divided);
        FileParsingParameters params = new FileParsingParameters();
        params.setAlignSeqRes(false); // Aligns real sequence with crystal sequence with performance cost
        params.setParseSecStruc(false);
        params.setLoadChemCompInfo(false);
        params.setParseCAOnly(true);
        cache.setFileParsingParams(params);
        return cache;
    }

    public static boolean pdbFileExists(String id, String pdbDir, boolean divided){
        String path;
        if (!divided){
            if ( pdbDir.endsWith(System.getProperty("file.separator")) ){
                path = pdbDir + "pdb" + id.substring(0,4).toLowerCase() + ".ent.gz";
            } else {
                path = pdbDir + System.getProperty("file.separator") + "pdb" + id.substring(0,4).toLowerCase() + ".ent.gz";
            }
        } else {
            String sepDir = id.substring(1,3).toLowerCase();
            if ( pdbDir.endsWith(System.getProperty("file.separator")) ){
                path = pdbDir + sepDir + System.getProperty("file.separator") + "pdb" + id.substring(0,4).toLowerCase() + ".ent.gz";
            } else {
                path = pdbDir + System.getProperty("file.separator") + sepDir + "/pdb" + id.substring(0,4).toLowerCase() + ".ent.gz";
            }
        }

        return new File(path).exists();
    }

}
