package asarnow.jce;

import asarnow.jce.job.DaliImportDCCPJob;
import asarnow.jce.job.DaliListAlignmentJobSeries;
import asarnow.jce.job.FileOutputJob;
import asarnow.jce.job.JobSeries;
import asarnow.jce.job.PairwiseAlignmentJobSeries;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureImpl;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.align.StructureAlignment;
import org.biojava.nbio.structure.align.StructureAlignmentFactory;
import org.biojava.nbio.structure.align.ce.CeMain;
import org.biojava.nbio.structure.align.ce.CeParameters;
import org.biojava.nbio.structure.align.fatcat.FatCatFlexible;
import org.biojava.nbio.structure.align.fatcat.FatCatRigid;
import org.biojava.nbio.structure.align.fatcat.calc.FatCatParameters;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.structure.io.PDBFileReader;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Daniel Asarnow
 */
public class Align {


    static int alignPair(String pdb1, String pdb2, String pdbDir, boolean divided, int alignerFlag){
        //TODO use AtomCache and getStructure
        String chainId1 = pdb1.substring(4);
        String chainId2 = pdb2.substring(4);

        FileParsingParameters fparams = new FileParsingParameters();
        fparams.setParseCAOnly(true);

//		AtomCache cache = new AtomCache(pdbFilePath, false);
//		cache.setFileParsingParameters(fparams);

        PDBFileReader reader = new PDBFileReader();
        if (pdbDir != null) reader.setPath(pdbDir);
//        reader.setPdbDirectorySplit(divided);
        reader.setFileParsingParameters(fparams);

        Structure structure1;
        Structure structure2;

        try {
//			structure1 = cache.getStructure(pdb1);
//			structure2 = cache.getStructure(pdb2);

            if (pdbDir != null) {
                structure1 = reader.getStructureById(pdb1);
                structure2 = reader.getStructureById(pdb2);
            } else {
                structure1 = reader.getStructure(pdb1);
                structure2 = reader.getStructure(pdb2);
            }

            pdb1 = structure1.getPDBCode();
            pdb2 = structure2.getPDBCode();

            structure1 = new StructureImpl(structure1.getChainByPDB(chainId1));
            structure2 = new StructureImpl(structure2.getChainByPDB(chainId2));

            Atom[] ca1 = StructureTools.getAtomCAArray(structure1);
            Atom[] ca2 = StructureTools.getAtomCAArray(structure2);

            AFPChain afpChain;

            switch ( alignerFlag ) {
//                case Constants.CE: afpChain = useCe(ca1, ca2); break;
                case Constants.FATCAT: afpChain = useFatcat(ca1, ca2); break;
                case Constants.FATCAT_RIGID: afpChain = useFatcatRigid(ca1, ca2); break;
                default: afpChain = useCe(ca1, ca2); break;
            }

//			afpChain.setName1(pdb1);
//			afpChain.setName2(pdb2);

            System.out.println( pdb1 + chainId1 + '\t' +
                    pdb2 + chainId2 + '\t' +
                    Double.toString(afpChain.getTotalRmsdOpt()) + '\t' +
                    Double.toString(afpChain.getAlignScore()) + '\t' +
                    Double.toString(afpChain.getProbability()) + '\t' +  // for fatcat, not a z-score!
                    Integer.toString(afpChain.getNrEQR()) + '\t' +
                    Double.toString(afpChain.getSimilarity())
            );

//			System.out.println(AfpChainWriter.toWebSiteDisplay(afpChain, ca1, ca2));
//			System.out.println(AfpChainWriter.toScoresList(afpChain));
//			System.out.println(afpChain.toRotMat());

        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    static int alignList(List<String> ids, String pdbDir, String outFile, Boolean divided, Integer nproc, int alignerFlag) /*throws IOException, InterruptedException*/ {

        BlockingQueue<String> outputQueue = new ArrayBlockingQueue<>(128);

        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                nproc, // core size
                nproc, // max size
                60, // idle timeout
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(4096, true), // Fairness = true for FIFO
                new ThreadPoolExecutor.CallerRunsPolicy() ); // If we have to reject a task, run it in the calling thread.

        JobSeries jobSeries;
        Thread outputThread;
        switch (alignerFlag) {
            case Constants.DALI:
                DaliImportDCCPJob daliImportDCCPJob = new DaliImportDCCPJob(outputQueue, outFile);
                outputThread = new Thread(daliImportDCCPJob);
                jobSeries = new DaliListAlignmentJobSeries(ids, outputQueue);
                break;
            default:
                AtomCache cache = Utility.initAtomCache(pdbDir, divided);
                FileOutputJob fileOutputJobRunnable = new FileOutputJob(outputQueue, outFile);
                outputThread = new Thread(fileOutputJobRunnable);
                jobSeries = new PairwiseAlignmentJobSeries(ids, alignerFlag, cache, outputQueue);
                break;
        }

        outputThread.start();
        // threadPool.execute( outputJob );

        while (jobSeries.hasNext()) {
            Runnable job = jobSeries.next();
            if (job!=null) threadPool.execute(job);
        }

        // threadPool.execute( new PoisonPill() );

        threadPool.shutdown();
        try {
            threadPool.awaitTermination(21, TimeUnit.DAYS);
            outputQueue.put("STOP");
            outputThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static AFPChain useFatcat(Atom[] ca1, Atom[] ca2) throws StructureException {
        StructureAlignment aligner = StructureAlignmentFactory.getAlgorithm(FatCatFlexible.algorithmName);
        FatCatParameters params = new FatCatParameters();
//	    params.setShowAFPRanges(true);
        return aligner.align(ca1, ca2, params);
    }

    public static AFPChain useCe(Atom[] ca1, Atom[] ca2) throws StructureException {
        StructureAlignment aligner = StructureAlignmentFactory.getAlgorithm(CeMain.algorithmName);
        CeParameters params = new CeParameters();
        return aligner.align(ca1, ca2, params);
    }

    public static AFPChain useFatcatRigid(Atom[] ca1, Atom[] ca2) throws StructureException {
        StructureAlignment aligner = StructureAlignmentFactory.getAlgorithm(FatCatRigid.algorithmName);
        FatCatParameters params = new FatCatParameters();
//	    params.setShowAFPRanges(true);
        return aligner.align(ca1, ca2, params);
    }
}
