/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
/*
*

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.lucee.extension.image.filter;import java.awt.image.BufferedImage;




import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;

import org.lucee.extension.image.ImageUtil;

import lucee.runtime.type.Struct;


/**
 * A filter which subtracts Gaussian blur from an image, sharpening it.
 *
 */
public class UnsharpFilter extends GaussianFilter  implements DynFiltering {

	private float amount = 0.5f;
	private int threshold = 1;
	
	public UnsharpFilter() {
		radius = 2;
	}
	
	/**
     * Set the threshold value.
     * @param threshold the threshold value
     * @see #getThreshold
     */
	public void setThreshold( int threshold ) {
		this.threshold = threshold;
	}
	
	/**
     * Get the threshold value.
     * @return the threshold value
     * @see #setThreshold
     */
	public int getThreshold() {
		return threshold;
	}
	
	/**
	 * Set the amount of sharpening.
	 * @param amount the amount
     * @min-value 0
     * @max-value 1
     * @see #getAmount
	 */
	public void setAmount( float amount ) {
		this.amount = amount;
	}
	
	/**
	 * Get the amount of sharpening.
	 * @return the amount
     * @see #setAmount
	 */
	public float getAmount() {
		return amount;
	}
	
    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        int width = src.getWidth();
        int height = src.getHeight();

        if ( dst == null )
            dst = createCompatibleDestImage( src, null );

        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        src.getRGB( 0, 0, width, height, inPixels, 0, width );

		if ( radius > 0 ) {
			convolveAndTranspose(kernel, inPixels, outPixels, width, height, alpha, alpha && premultiplyAlpha, false, CLAMP_EDGES);
			convolveAndTranspose(kernel, outPixels, inPixels, height, width, alpha, false, alpha && premultiplyAlpha, CLAMP_EDGES);
		}

        src.getRGB( 0, 0, width, height, outPixels, 0, width );

		float a = 4*amount;

		int index = 0;
		for ( int y = 0; y < height; y++ ) {
			for ( int x = 0; x < width; x++ ) {
				int rgb1 = outPixels[index];
				int r1 = (rgb1 >> 16) & 0xff;
				int g1 = (rgb1 >> 8) & 0xff;
				int b1 = rgb1 & 0xff;

				int rgb2 = inPixels[index];
				int r2 = (rgb2 >> 16) & 0xff;
				int g2 = (rgb2 >> 8) & 0xff;
				int b2 = rgb2 & 0xff;

				if ( Math.abs( r1 -  r2 ) >= threshold )
					r1 = PixelUtils.clamp( (int)((a+1) * (r1-r2) + r2) );
				if ( Math.abs( g1 -  g2 ) >= threshold )
					g1 = PixelUtils.clamp( (int)((a+1) * (g1-g2) + g2) );
				if ( Math.abs( b1 -  b2 ) >= threshold )
					b1 = PixelUtils.clamp( (int)((a+1) * (b1-b2) + b2) );

				inPixels[index] = (rgb1 & 0xff000000) | (r1 << 16) | (g1 << 8) | b1;
				index++;
			}
		}

        dst.setRGB( 0, 0, width, height, inPixels, 0, width );
        return dst;
    }

	@Override
	public String toString() {
		return "Blur/Unsharp Mask...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("Amount")))!=null)setAmount(ImageFilterUtil.toFloatValue(o,"Amount"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("Threshold")))!=null)setThreshold(ImageFilterUtil.toIntValue(o,"Threshold"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("Radius")))!=null)setRadius(ImageFilterUtil.toFloatValue(o,"Radius"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("EdgeAction")))!=null)setEdgeAction(ImageFilterUtil.toString(o,"EdgeAction"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("UseAlpha")))!=null)setUseAlpha(ImageFilterUtil.toBooleanValue(o,"UseAlpha"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("PremultiplyAlpha")))!=null)setPremultiplyAlpha(ImageFilterUtil.toBooleanValue(o,"PremultiplyAlpha"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+eng.getListUtil().toList(parameters.keys(),", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Amount, Threshold, Radius, Kernel, EdgeAction, UseAlpha, PremultiplyAlpha]",null);
		}

		return filter(src, dst);
	}
}