package asarnow.jce;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.io.FileParsingParameters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: da
 * Date: 7/8/11
 * Time: 10:24 PM
 */
public class Utility {

    public static List<String> listFromFile(String file) {
        return listFromFile(new File(file));
    }

    public static List<String> listFromFile(File listFilePath){
        List<String> list = new ArrayList<>();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void listToFile(String filePath, List<String> lines) throws IOException {
        listToFile(new File(filePath),lines);
    }

    public static void listToFile(File filePath, List<String> lines) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        for (String line : lines.subList(0,lines.size()-1)) {
            writer.write(line);
            writer.newLine();
        }
        writer.write(lines.get(lines.size()-1));
        writer.close();
    }

    public static AtomCache initAtomCache(String pdbPath, Boolean divided, FileParsingParameters params) {
        AtomCache cache = new AtomCache(pdbPath, divided);
        cache.setFileParsingParams(params);
        return cache;
    }

    public static AtomCache initAtomCache(String pdbPath, Boolean divided) {
        return initAtomCache(pdbPath, divided, createFileParsingParameters());
    }

    public static FileParsingParameters createFileParsingParameters(boolean parseAllAtoms) {
        FileParsingParameters parameters = new FileParsingParameters();
        parameters.setAlignSeqRes(false);
        parameters.setParseSecStruc(false);
        parameters.setLoadChemCompInfo(false);
        parameters.setParseCAOnly(!parseAllAtoms);
        return parameters;
    }


    public static FileParsingParameters createFileParsingParameters() {
        return createFileParsingParameters(true);
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

    public static void writePDB(Structure structure, String extractDir) throws IOException {
        writePDB(structure,extractDir,true);
    }

    public static void writePDB(Structure structure, String extractDir, boolean compressed) throws IOException {
        String name = structure.getName() + ".ent";
        String path = extractDir + File.separator + name;

        if (compressed) {
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(path+".gz"));
            zipOutputStream.putNextEntry(new ZipEntry(name));
            zipOutputStream.write( structure.toPDB().getBytes() );
            zipOutputStream.closeEntry();
            zipOutputStream.close();
        } else {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            fileOutputStream.write( structure.toPDB().getBytes() );
        }

    }

    public static String readFile(File dccp) {
        return null;
    }


}
