package asarnow.jce;

import org.biojava.bio.structure.*;
import org.biojava.bio.structure.align.model.AFPChain;
import org.biojava.bio.structure.align.util.AtomCache;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 * Created by IntelliJ IDEA.
 * User: da
 * Date: 7/8/11
 * Time: 10:42 PM
 */
public class Align implements Runnable {

    private AtomCache cache;
    private String id1,id2;
    private int alignerFlag;
    private BlockingQueue<String> outputQueue;

    public Align(AtomCache cache, String id1, String id2, int alignerFlag, BlockingQueue<String> outputQueue) {
        this.cache = cache;
        this.id1 = id1.substring(0,4).toLowerCase() + id1.substring(4).toUpperCase();
        this.id2 = id2.substring(0,4).toLowerCase() + id2.substring(4).toUpperCase();
        this.alignerFlag = alignerFlag;
        this.outputQueue = outputQueue;
    }

    public void run() {

        try {
//            System.out.println("Align:34");
			Structure structure1 = cache.getStructure( id1.substring(0,4) );
			Structure structure2 = cache.getStructure( id2.substring(0,4) );
//            System.out.println("Align:37");
            structure1 = new StructureImpl(structure1.getChainByPDB( id1.substring(4) ));
            structure2 = new StructureImpl(structure2.getChainByPDB( id2.substring(4)) );

            Atom[] ca1 = StructureTools.getAtomCAArray(structure1);
            Atom[] ca2 = StructureTools.getAtomCAArray(structure2);

            AFPChain afpChain = null;
//            StructureAlignment aligner;

            switch ( alignerFlag ) {
                case Constants.CE: afpChain = Main.useCe(ca1, ca2); break;
                case Constants.FATCAT: afpChain = Main.useFatcat(ca1, ca2); break;
                case Constants.FATCAT_RIGID: afpChain = Main.useFatcatRigid(ca1, ca2); break;

                /*case Constants.CE:
                    aligner = StructureAlignmentFactory.getAlgorithm(CeMain.algorithmName);
                    CeParameters ceParams = new CeParameters();
                    afpChain = aligner.align(ca1, ca2, ceParams);
                    break;
                case Constants.FATCAT:
                    aligner = StructureAlignmentFactory.getAlgorithm(FatCatFlexible.algorithmName);
                    FatCatParameters fatcatParams = new FatCatParameters();
                    afpChain = aligner.align(ca1, ca2, fatcatParams);
                    break;
                case Constants.FATCAT_RIGID:
                    aligner = StructureAlignmentFactory.getAlgorithm(FatCatRigid.algorithmName);
                    FatCatParameters fatcatRigidParams = new FatCatParameters();
                    afpChain = aligner.align(ca1, ca2, fatcatRigidParams);
                    break;*/

            }

//            System.out.println("Align:52");

            outputQueue.put(id1 + '\t' +
                    id2 + '\t' +
                    Double.toString(afpChain.getTotalRmsdOpt()) + '\t' +
                    Double.toString(afpChain.getAlignScore()) + '\t' +
                    Double.toString(afpChain.getProbability()) + '\t' +  // for fatcat, not a z-score!
                    Integer.toString(afpChain.getNrEQR()) + '\t' +
                    Double.toString(afpChain.getSimilarity()) +
                    System.getProperty("line.separator"));

//            System.out.println(outputQueue.size());

//			System.out.println(AfpChainWriter.toWebSiteDisplay(afpChain, ca1, ca2));
//			System.out.println(AfpChainWriter.toScoresList(afpChain));
//			System.out.println(afpChain.toRotMat());

        } catch (IOException e) {
//            System.out.println(id1 + ' ' + id2 + " IOException");
//            e.printStackTrace();
        } catch (StructureException e) {
//            System.out.println(id1 + ' ' + id2 + " StructureException");
//            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace(); //TODO Auto-generated catch block
        }
    }
}
