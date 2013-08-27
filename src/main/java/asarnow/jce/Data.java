package asarnow.jce;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.io.FileParsingParameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Asarnow
 */
public class Data {

    public static List<String> extractStructures(List<String> list, String pdbDir, Boolean divided, String extractDir, Boolean compressed) {
        return extractStructures(list, pdbDir, divided, Utility.createFileParsingParameters(), extractDir, compressed);
    }

    public static List<String> extractStructures(List<String> list, String pdbDir, Boolean divided, FileParsingParameters parameters, String extractDir, Boolean compressed) {
        AtomCache cache = Utility.initAtomCache(pdbDir,divided,parameters);
        List<String> newlist = new ArrayList<>();
        for (String item : list) {
            try {
                Structure structure = cache.getStructure(item);

                Utility.writePDB(structure, extractDir, compressed);
                newlist.add(structure.getName());
            } catch (IOException | StructureException e) {
                //TODO auto-generated catch block
                e.printStackTrace();
            }
        }
        return newlist;

    }

    public static String extractStructure(String structureSpec, String pdbDir, Boolean divided, String extractDir, Boolean compressed) {
        return extractStructure(structureSpec, pdbDir, divided, Utility.createFileParsingParameters(), extractDir, compressed);
    }

    public static String extractStructure(String structureSpec, String pdbDir, Boolean divided, FileParsingParameters parameters, String extractDir, Boolean compressed) {
        AtomCache cache = Utility.initAtomCache(pdbDir,divided,parameters);
        try {
            Structure structure = cache.getStructure(structureSpec);
            Utility.writePDB(structure, extractDir, compressed);
            return structure.getName();
        } catch (IOException | StructureException e) {
            //TODO auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static String createExtractDir(String pathname) throws IOException {
        Path extractPath;
        if (pathname!=null) {
            extractPath = Paths.get(pathname);
        } else {
            extractPath = Files.createTempDirectory(Constants.TEMP_DIR_PREFIX);
        }
        return extractPath.toString();
    }

    public static String getStructureInfo(String id) {
        return null;
    }
}
