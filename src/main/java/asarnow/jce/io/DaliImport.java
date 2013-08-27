package asarnow.jce.io;

import com.ichemlabs.FortranFormat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Asarnow
 */
public class DaliImport {

    public static List<String> parseDCCP(File file) throws IOException, ParseException {
        BufferedReader dccpReader = new BufferedReader(new FileReader(file));
        List<String> list = new ArrayList<>();
        String line;
        while ( (line = dccpReader.readLine()) != null ) {
            if (line.startsWith(" DCCP"))
            list.add(parseDCCPLine(line));
        }
        dccpReader.close();
        return list;
    }

    public static List<String> parseDCCP(String file) throws IOException, ParseException {
        return parseDCCP(new File(file));
    }

    public static String parseDCCPLine(String line) throws IOException, ParseException {
//		  BreakIterator wordIterator = BreakIterator.getWordInstance();
//		  BreakIterator lineIterator = BreakIterator.getLineInstance();

//		  DCCP   1    100.0 4.5  52     0.3          13      6                1x5xA 1x5mA
//		  String specificationString = "(1X,A9,F8.1,F4.1,I4,16X,I4,4X,I3,16X,A5,1X,A5)";
        String specificationString = "(1X,A9,F8.1,F4.1,I4,F8.1,8X,I4,4X,I3,16X,A5,1X,A5)";
        //		  String specificationString = "(1X,A9)";
        FortranFormat formatter = new FortranFormat(specificationString);
        List<Object> inputObjects = formatter.parse(line);
        String rawScore = Double.toString((Double) inputObjects.get(1));
        String rmsd = Double.toString((Double) inputObjects.get(2));
        String numEquivRes = Integer.toString((Integer) inputObjects.get(3));
        String zScore = Double.toString((Double) inputObjects.get(4));
        String seqId = Integer.toString((Integer) inputObjects.get(5));
        String numAlignBlks = Integer.toString((Integer) inputObjects.get(6));
        String mol1 = ((String) inputObjects.get(7));
        String mol2 = ((String) inputObjects.get(8));

        String returnData = mol1 + "\t" + mol2 + "\t" + rmsd + "\t" +
                rawScore + "\t" + zScore + "\t" + numEquivRes + "\t" +
                seqId + "\t" + numAlignBlks;

        return returnData;
    }


}
