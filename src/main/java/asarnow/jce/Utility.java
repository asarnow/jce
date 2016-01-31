package asarnow.jce;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.io.FileParsingParameters;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
                if ( !line.startsWith("#") || line.trim().length() == 0 ){
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

    public static AtomCache initAtomCache(String pdbPath, FileParsingParameters params) {
        AtomCache cache = new AtomCache(pdbPath);
        cache.setFileParsingParams(params);
        return cache;
    }

    public static AtomCache initAtomCache(String pdbPath) {
        return initAtomCache(pdbPath, createFileParsingParameters());
    }

    public static FileParsingParameters createFileParsingParameters(boolean caOnly, boolean alignSeqRes, boolean secStruc, boolean chemCompInfo) {
        FileParsingParameters parameters = new FileParsingParameters();
        parameters.setAlignSeqRes(alignSeqRes);
        parameters.setParseSecStruc(secStruc);
        parameters.setLoadChemCompInfo(chemCompInfo);
        parameters.setParseCAOnly(caOnly);
        return parameters;
    }
    public static FileParsingParameters createFileParsingParameters() {
        return createFileParsingParameters(true, false, false, false);
    }

    public static FileParsingParameters createFileParsingParameters(boolean caOnly) {
        return createFileParsingParameters(caOnly, false, false, false);
    }

    public static FileParsingParameters createFileParsingParameters(boolean caOnly, boolean alignSeqRes) {
        return createFileParsingParameters(caOnly, alignSeqRes, false, false);
    }

    public static FileParsingParameters createFileParsingParameters(boolean caOnly, boolean alignSeqRes, boolean secStruc) {
        return createFileParsingParameters(caOnly, alignSeqRes, secStruc, false);
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

    public static String summarizeAfpChain(AFPChain afpChain) {
        return afpChain.getName1() + '\t' +
                afpChain.getName2() + '\t' +
                Double.toString(afpChain.getTotalRmsdOpt()) + '\t' +
                Double.toString(afpChain.getAlignScore()) + '\t' +
                Double.toString(afpChain.getProbability()) + '\t' +  // for fatcat, not a z-score!
                Integer.toString(afpChain.getNrEQR()) + '\t' +
                Double.toString(afpChain.getSimilarity()) +
                System.getProperty("line.separator");
    }

    public static Executor createThreadPool(int nproc) {
        return new ThreadPoolExecutor(
            nproc, // core size
            nproc, // max size
            60, // idle timeout
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(4096, true), // Fairness = true for FIFO
            new ThreadPoolExecutor.CallerRunsPolicy() ); // If we have to reject a task, run it in the calling thread.
    }

    public static void standardizeIds(List<String> ids) {
        for (int i=0; i < ids.size(); i++) {
            String id = ids.get(i);
            ids.set(i, standardizeId(id));
        }
    }

    public static String standardizeId(String id) {
        String newId;
        if (id.length() == 5) {
            newId = id.substring(0, 4).toLowerCase() + "." + id.substring(4).toUpperCase();
        } else {
            newId = id;
        }
        return newId;
    }
}
