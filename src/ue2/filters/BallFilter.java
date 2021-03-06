package ue2.filters;

import java.awt.image.renderable.ParameterBlock;

/** please do not use strg+shift+f in eclipse because of kernel values format **/

import java.security.InvalidParameterException;

import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.PlanarImage;

import interfaces.Readable;
import interfaces.Writeable;
import ue2.helpers.ImageSaver;
import ue2.helpers.ImageViewer;

public class BallFilter extends MyAbstractFilter<PlanarImage>{

	private static final int _kernelSize = 10;
	
	/** please do not use strg+shift+f in eclipse because of kernel values format **/
	/* should be a circle */
	private static final float[] _kernelValues = new float[]{
			0,0,0,0,0,0,0,0,0,0,
			0,0,0,1,1,1,1,0,0,0,
			0,0,1,1,1,1,1,1,0,0,
			0,1,1,1,1,1,1,1,1,0,
			0,1,1,1,1,1,1,1,1,0,
			0,1,1,1,1,1,1,1,1,0,
			0,1,1,1,1,1,1,1,1,0,
			0,0,1,1,1,1,1,1,0,0,
			0,0,0,1,1,1,1,0,0,0,
			0,0,0,0,0,0,0,0,0,0
	};
	
	private static final KernelJAI _kernel = new KernelJAI(_kernelSize, _kernelSize, _kernelValues);
	
	
	public BallFilter(Readable<PlanarImage> input, Writeable<PlanarImage> output) throws InvalidParameterException {
		super(input, output);
	}
	
	public BallFilter(Readable<PlanarImage> input) throws InvalidParameterException {
		super(input);
	}
	
	public BallFilter(Writeable<PlanarImage> output) throws InvalidParameterException {
		super(output);
	}

	@Override
	protected PlanarImage process(PlanarImage image) {
		
	    ParameterBlock pb = prepareParameterBlock(image);
	    
	    //erode image
	    PlanarImage outputImage = JAI.create("erode", pb);
	    
	    //dilate image
	    pb = prepareParameterBlock(outputImage);
	    outputImage = JAI.create("dilate",pb);
		
	    ImageSaver.save(outputImage, "BallFilter");
	    ImageViewer.show(outputImage, "BallFilter");
	    
	    return outputImage;
	}
	
	private ParameterBlock prepareParameterBlock(PlanarImage image){
		
		ParameterBlock pb = new ParameterBlock();
	    pb.addSource(image);
	    pb.add(_kernel);
	    
	    return pb;
	}
	
	public PlanarImage getBallImage(PlanarImage image){
		ParameterBlock pb = prepareParameterBlock(image);
	    
	    //erode image
	    PlanarImage outputImage = JAI.create("erode", pb);
	    
	    //dilate image
	    pb = prepareParameterBlock(outputImage);
	    outputImage = JAI.create("dilate",pb);
	    
	    return outputImage;
	}

}
