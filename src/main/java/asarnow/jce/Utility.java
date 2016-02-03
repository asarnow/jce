package asarnow.jce;

import asarnow.jce.job.AlignmentResult;
import org.apache.log4j.Logger;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.align.gui.DisplayAFP;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.align.model.AfpChainWriter;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.structure.io.LocalPDBDirectory;
import org.biojava.nbio.structure.io.PDBFileReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: da
 * Date: 7/8/11
 * Time: 10:24 PM
 */
public class Utility {
    private static Logger logger = Logger.getLogger(Utility.class);

    public static List<String> listFromFile(String file) {
        return listFromFile(new File(file));
    }

    public static List<String> listFromFile(File listFilePath) {
        List<String> list = new ArrayList<>();
        try {
            BufferedReader br;
            br = new BufferedReader(new FileReader(listFilePath));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#") && line.trim().length() > 0) {
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
        listToFile(new File(filePath), lines);
    }

    public static void listToFile(File filePath, List<String> lines) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        for (String line : lines.subList(0, lines.size() - 1)) {
            writer.write(line);
            writer.newLine();
        }
        writer.write(lines.get(lines.size() - 1));
        writer.close();
    }

    public static AtomCache initAtomCache(String pdbPath,
                                          FileParsingParameters params,
                                          LocalPDBDirectory.ObsoleteBehavior obsoleteBehavior,
                                          LocalPDBDirectory.FetchBehavior fetchBehavior) {
        AtomCache cache = new AtomCache(pdbPath);
        cache.setFileParsingParams(params);
        cache.setObsoleteBehavior(obsoleteBehavior);
        cache.setFetchBehavior(fetchBehavior);
        return cache;
    }

    public static AtomCache initAtomCache(String pdbPath, FileParsingParameters params, LocalPDBDirectory.ObsoleteBehavior behavior) {
        return initAtomCache(pdbPath, params, behavior, LocalPDBDirectory.FetchBehavior.DEFAULT);
    }

    public static AtomCache initAtomCache(String pdbPath, FileParsingParameters params) {
        return initAtomCache(pdbPath, params, LocalPDBDirectory.ObsoleteBehavior.DEFAULT);
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

    public static boolean pdbFileExists(String id, String pdbDir, boolean divided) {
        String path;
        if (!divided) {
            if (pdbDir.endsWith(System.getProperty("file.separator"))) {
                path = pdbDir + "pdb" + id.substring(0, 4).toLowerCase() + ".ent.gz";
            } else {
                path = pdbDir + System.getProperty("file.separator") + "pdb" + id.substring(0, 4).toLowerCase() + ".ent.gz";
            }
        } else {
            String sepDir = id.substring(1, 3).toLowerCase();
            if (pdbDir.endsWith(System.getProperty("file.separator"))) {
                path = pdbDir + sepDir + System.getProperty("file.separator") + "pdb" + id.substring(0, 4).toLowerCase() + ".ent.gz";
            } else {
                path = pdbDir + System.getProperty("file.separator") + sepDir + "/pdb" + id.substring(0, 4).toLowerCase() + ".ent.gz";
            }
        }

        return new File(path).exists();
    }

    public static void writePDB(Structure structure, String extractDir) throws IOException {
        writePDB(structure, extractDir, true);
    }

    public static void writePDB(Structure structure, String extractDir, boolean compressed) throws IOException {
        String name = compressed ? structure.getName() + ".ent.gz" : structure.getName() + ".ent";
        String path = extractDir + File.separator + name;
        logger.debug("Writing " + structure.getName() + " to " + path);
        if (compressed) {
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(path));
            zipOutputStream.putNextEntry(new ZipEntry(name));
            zipOutputStream.write(structure.toPDB().getBytes());
            zipOutputStream.closeEntry();
            zipOutputStream.close();
        } else {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            fileOutputStream.write(structure.toPDB().getBytes());
            fileOutputStream.close();
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
                new ThreadPoolExecutor.CallerRunsPolicy()); // If we have to reject a task, run it in the calling thread.
    }

    public static List<String> standardizeIds(List<String> ids) {
        Set<String> uniqueIds = new LinkedHashSet<>();
        for (String id : ids) {
            uniqueIds.add(standardizeId(id));
        }
        return new ArrayList<>(uniqueIds);
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

    public static List<String> expandStructures(List<String> list2align,
                                                String pdbDir,
                                                LocalPDBDirectory.ObsoleteBehavior obsoleteBehavior) {
        FileParsingParameters params = new FileParsingParameters();
        params.setAlignSeqRes(false);
        params.setParseCAOnly(true);
        AtomCache cache = initAtomCache(pdbDir, params, obsoleteBehavior);
        List<String> newList = new ArrayList<>();
        for (String id : list2align) {
            try {
                String newId;
                Structure s = cache.getStructure(id);
                for (Chain ch : s.getChains()) {
                    newId = s.getPdbId() + "." + ch.getChainID();
                    newList.add(newId);
                }
            } catch (IOException | StructureException e) {
                e.printStackTrace();
            }
        }
        return newList;
    }

    public static List<String> expandStructuresAsync(List<String> list2align,
                                                String pdbDir,
                                                LocalPDBDirectory.ObsoleteBehavior obsoleteBehavior,
                                                Executor executor) {
        class ExpandStructure implements Callable<List<String>> {
            String id;
            AtomCache cache;

            ExpandStructure(String id, AtomCache cache) {
                this.id = id;
                this.cache = cache;
            }

            @Override
            public List<String> call() throws Exception {
                Structure s = cache.getStructure(id);
                List<String> newList = new ArrayList<>();
                for (Chain ch : s.getChains()) {
                    String newId = s.getPdbId() + "." + ch.getChainID();
                    newList.add(newId);
                }
                return newList;
            }
        }
        FileParsingParameters params = new FileParsingParameters();
        params.setAlignSeqRes(false);
        params.setParseCAOnly(true);
        AtomCache cache = initAtomCache(pdbDir, params, obsoleteBehavior);
        CompletionService<List<String>> executorService = new ExecutorCompletionService<List<String>>(executor);
        List<String> newList = new ArrayList<>();
        int queued = 0;
        for (String id : list2align) {
            executorService.submit(new ExpandStructure(id, cache));
            queued++;
        }
        while (queued > 0) {
            try {
                newList.addAll(executorService.take().get());
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e);
            } finally {
                queued--;
            }
        }
        return newList;
    }

    public static List<String> firstChainOnly(List<String> list2align,
                                                String pdbDir,
                                                LocalPDBDirectory.ObsoleteBehavior obsoleteBehavior) {
        FileParsingParameters params = new FileParsingParameters();
        params.setAlignSeqRes(false);
        params.setParseCAOnly(true);
        AtomCache cache = initAtomCache(pdbDir, params, obsoleteBehavior);
        List<String> newList = new ArrayList<>();
        for (String id : list2align) {
            try {
                String newId;
                Structure s = cache.getStructure(id);
                if (s.getChains().size() > 1) {
                    newId = s.getPdbId() + "." + s.getChain(0).getChainID();
                    newList.add(newId);
                } else {
                    newList.add(id);
                }
            } catch (IOException | StructureException e) {
                e.printStackTrace();
            }
        }
        return newList;
    }

    public static List<String> firstChainOnlyAsync(List<String> list2align,
                                                   String pdbDir,
                                                   LocalPDBDirectory.ObsoleteBehavior obsoleteBehavior,
                                                   Executor executor) {

        class FirstChain implements Callable<String> {
            String id;
            AtomCache cache;
            FirstChain(String id, AtomCache cache){
                this.id = id;
                this.cache = cache;
            }
            @Override
            public String call() throws Exception {
                String newId;
                Structure s = cache.getStructure(id);
                if (s.getChains().size() > 1) {
                    newId = s.getPdbId() + "." + s.getChain(0).getChainID();
                } else {
                    newId = id;
                }
                return newId;
            }
        }
        FileParsingParameters params = new FileParsingParameters();
        params.setAlignSeqRes(false);
        params.setParseCAOnly(true);
        AtomCache cache = initAtomCache(pdbDir, params, obsoleteBehavior);
        List<String> newList = new ArrayList<>();
        CompletionService<String> executorService = new ExecutorCompletionService<>(executor);
        int queued = 0;
        for (String id : list2align) {
            executorService.submit(new FirstChain(id, cache));
            queued++;
        }
        while (queued > 0) {
            try {
                newList.add(executorService.take().get());
            } catch (ExecutionException | InterruptedException e) {
                logger.error(e);
            } finally {
                queued--;
            }
        }
        return newList;
    }

    public static void printStructureInfo(AtomCache cache, String id, int verbosity) {
        try {
            Structure s = cache.getStructure(id);
            System.out.println("Query: " + id + " Name: " + s.getName() +
                    " Ident: " + s.getIdentifier() + " PdbId: " + s.getPdbId() + " PdbCode: " + s.getPDBCode());
            if (verbosity > 1) System.out.println(s.getPDBHeader());
            if (verbosity > 0) {
                for (Chain ch : s.getChains()) {
                    System.out.print("Chain " + ch.getChainID() + ": " + ch.getAtomLength() + " residues");
                    if (ch.getSeqResSequence() != null) System.out.print(" (" + ch.getSeqResLength() + " by SeqRes)");
                    System.out.print(System.lineSeparator());
                }
            }
        } catch (IOException | StructureException e) {
            logger.error("Error loading " + id, e);
        }

    }

    public static List<String> prefetchStructures(List<String> list,
                                                 String pdbDir,
                                                 FileParsingParameters parameters,
                                                 LocalPDBDirectory.ObsoleteBehavior behavior) {
        LocalPDBDirectory reader = new PDBFileReader(pdbDir);
        reader.setFileParsingParameters(parameters);
        reader.setObsoleteBehavior(behavior);
        reader.setFetchBehavior(LocalPDBDirectory.FetchBehavior.DEFAULT);
        List<String> newlist = new ArrayList<>();
        for (String item : list) {
            try {
                reader.prefetchStructure(item);
                newlist.add(item);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newlist;
    }

    public static List<String> extractStructures(List<String> list,
                                                 AtomCache cache,
                                                 String extractDir,
                                                 Boolean compressed) {
        List<String> newlist = new ArrayList<>();
        for (String item : list) {
            try {
                Structure structure = cache.getStructure(item);
                if (structure.getName() == null || structure.getName().equals("")) structure.setName(item);
                writePDB(structure, extractDir, compressed);
                newlist.add(structure.getName());
            } catch (IOException | StructureException | NullPointerException e) {
                logger.error("Error extracting " + item, e);
            }
        }
        return newlist;
    }

    public static String extractStructure(String structureSpec, String pdbDir, Boolean divided, String extractDir, Boolean compressed) {
        return extractStructure(structureSpec, pdbDir, divided, createFileParsingParameters(), extractDir, compressed);
    }

    public static String extractStructure(String structureSpec, String pdbDir, Boolean divided, FileParsingParameters parameters, String extractDir, Boolean compressed) {
        AtomCache cache = initAtomCache(pdbDir,parameters);
        try {
            Structure structure = cache.getStructure(structureSpec);
            if (structure.getName() == null || structure.getName().equals("")) structure.setName(structureSpec);
            writePDB(structure, extractDir, compressed);
            return structure.getName();
        } catch (IOException | StructureException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String createExtractDir(String pathname) throws IOException {
        Path extractPath;
        if (pathname!=null) {
            extractPath = Paths.get(pathname);
            if (!extractPath.toFile().exists()) {
                logger.debug("mkdir :" + extractPath.toString());
                extractPath.toFile().mkdirs();
            }
        } else {
            extractPath = Files.createTempDirectory(Constants.TEMP_DIR_PREFIX);
        }
        return extractPath.toString();
    }

    public static Structure createArtificialStructure(AlignmentResult result) {
        Structure artificial = null;
        try {
            artificial = DisplayAFP.createArtificalStructure(result.getAfpChain(), result.getCa1(), result.getCa2());
        } catch (StructureException e) {
            logger.error(e);
        }
        return artificial;
    }

    public static String createAlignmentText(AlignmentResult result) {
        return AfpChainWriter.toFatCat(result.getAfpChain(), result.getCa1(), result.getCa2());
    }
}
