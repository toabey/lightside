package edu.cmu.side.export;

import java.io.File;
import java.io.FileWriter;
import weka.core.Instances;
import java.util.Collection;


import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.feature.Feature.Type;

public class ARFFExporter {


	/**
	 * Outputs a feature table to a file that can be read by some other software package,
	 * based on some format selected elsewhere in the GUI (as of 10/3/11, only does ARFF format).
	 */
	public static void export(FeatureTable ft, File out){
		try{
			if (!out.getName().endsWith(".feature"))
				out = new File(out.getAbsolutePath() + ".feature");
			FileWriter outf = new FileWriter(out);
			
			for (int i=0; i<ft.getInstanceNumber(); i++){
				Collection<FeatureHit> hits = ft.getHitsForDocument(i);
				for (FeatureHit hit: hits)
					outf.write(hit.getFeature().toString()+": "+hit.getValue().toString()+"\t");
				outf.write("\n");
			}
			outf.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
