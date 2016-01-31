package asarnow.jce;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.io.FileParsingParameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Daniel Asarnow
 */
public class Data {

    public static List<String> extractStructures(List<String> list, String pdbDir, Boolean divided, String extractDir, Boolean compressed) {
        return extractStructures(list, pdbDir, divided, Utility.createFileParsingParameters(), extractDir, compressed);
    }

    public static List<String> extractStructures(List<String> list, String pdbDir, Boolean divided, FileParsingParameters parameters, String extractDir, Boolean compressed) {
        AtomCache cache = Utility.initAtomCache(pdbDir,parameters);
        List<String> newlist = new ArrayList<>();
        for (String item : list) {
            try {
                Structure structure = cache.getStructure(item);

                Utility.writePDB(structure, extractDir, compressed);
                newlist.add(structure.getName());
            } catch (IOException | StructureException e) {
                e.printStackTrace();
            }
        }
        return newlist;

    }

    public static String extractStructure(String structureSpec, String pdbDir, Boolean divided, String extractDir, Boolean compressed) {
        return extractStructure(structureSpec, pdbDir, divided, Utility.createFileParsingParameters(), extractDir, compressed);
    }

    public static String extractStructure(String structureSpec, String pdbDir, Boolean divided, FileParsingParameters parameters, String extractDir, Boolean compressed) {
        AtomCache cache = Utility.initAtomCache(pdbDir,parameters);
        try {
            Structure structure = cache.getStructure(structureSpec);
            Utility.writePDB(structure, extractDir, compressed);
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
        } else {
            extractPath = Files.createTempDirectory(Constants.TEMP_DIR_PREFIX);
        }
        return extractPath.toString();
    }

    public static String getStructureInfo(String id) {
        return null;
    }

    public static double[] distanceMatrix(List<String> ids, String alignFilePath, int field) {
        Map<String,Integer> index = new HashMap<>( ids.size() );
        for (int i = 0; i < ids.size(); i++) {
            index.put(ids.get(i),i);
        }
        int n = ids.size();
        double[] matrix = new double[choose2(n)];
        Arrays.fill(matrix, Double.NaN);
        File alignFile = new File(alignFilePath);
        try (BufferedReader bis = new BufferedReader(new FileReader(alignFile))){
            String line;
            while ((line = bis.readLine())!=null) {
                String[] tok = line.split("\t");
                String id1 = tok[0];
                String id2 = tok[1];
                if (!id1.equals(id2)) {
                    Double val = Double.valueOf(tok[field]);
                    int c = index.get(id1) < index.get(id2) ? index.get(id1) : index.get(id2);
                    int r = index.get(id1) < index.get(id2) ? index.get(id2) : index.get(id1);
                    matrix[ sub2idxflat(c,r,n) ] = val;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matrix;
    }

    public static int choose2(int n) {
        return n*(n-1)/2;
    }

    public static int sub2idxflat(int c, int r, int n) {
        int nch2 = choose2(n);
        int ncch2 = choose2(n-c);
        return nch2 - ncch2 + (r-c-1);
    }

    public static String[] ints2strings(int[] ints) {
        String[] strings = new String[ints.length];
        for (int i=0; i<ints.length; i++) strings[i] = Integer.toString(ints[i]);
        return strings;
    }

    public static String[] doubles2strings(double[] ints) {
        String[] strings = new String[ints.length];
        for (int i=0; i<ints.length; i++) strings[i] = Double.toString(ints[i]);
        return strings;
    }

    public static void printStructureInfo(List<String> ids, String pdbDir, Boolean divided, FileParsingParameters parameters) {
        AtomCache cache = Utility.initAtomCache(pdbDir);
        for (String id : ids) {
            try {
                Structure structure = cache.getStructure(id);
                String info = "Structure specification " + id + " yields\n" +
                        structure.getPDBHeader().toPDB() + "\n";
                System.out.print(info);
            } catch (IOException | StructureException e) {
                System.out.println("Exception on structure specification " + id);
                e.printStackTrace();
            }
        }
    }

    public static int selectRepresentative(double[] matrix) {
        int n = (int)(-0.5 + Math.sqrt(0.25 + 2 * matrix.length));
        double[] means = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                int c = i < j ? i : j;
                int r = i < j ? j : j;
                means[i] += matrix[ sub2idxflat(c,r,matrix.length) ];
            }
            means[i] /= n;
        }
        return argmax(means);
    }

    public static int argmax(double[] arr) {
        double curmax = Double.MIN_VALUE;
        int idx = -1;
        for (int i=0; i<arr.length; i++) {
            if (arr[i] > curmax) {
                curmax = arr[i];
                idx = i;
            }
        }
        return idx;
    }

    public static int argmin(double[] arr) {
        double curmin = Double.MAX_VALUE;
        int idx = -1;
        for (int i=0; i<arr.length; i++) {
            if (arr[i] < curmin) {
                curmin = arr[i];
                idx = i;
            }
        }
        return idx;
    }
}
