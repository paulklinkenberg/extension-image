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
 * A filter which uses the brightness of each pixel to lookup a color from a colormap. 
 */
public class LookupFilter extends PointFilter  implements DynFiltering {
	
	private Colormap colormap = new Gradient();
	
	/**
     * Construct a LookupFilter.
     */
    public LookupFilter() {
		canFilterIndexColorModel = true;
	}

	/**
     * Construct a LookupFilter.
     * @param colormap the color map
     */
	public LookupFilter(Colormap colormap) {
		canFilterIndexColorModel = true;
		this.colormap = colormap;
	}

    /**
     * Set the colormap to be used for the filter.
     * @param colormap the colormap
     * @see #getColormap
     */
	public void setColormap(Colormap colormap) {
		this.colormap = colormap;
	}

    /**
     * Get the colormap to be used for the filter.
     * @return the colormap
     * @see #setColormap
     */
	public Colormap getColormap() {
		return colormap;
	}

	@Override
	public int filterRGB(int x, int y, int rgb) {
//		int a = rgb & 0xff000000;
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		rgb = (r + g + b) / 3;
		return colormap.getColor(rgb/255.0f);
	}

	@Override
	public String toString() {
		return "Colors/Lookup...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("Colormap")))!=null)setColormap(ImageFilterUtil.toColormap(o,"Colormap"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("Dimensions")))!=null){
			int[] dim=ImageFilterUtil.toDimensions(o,"Dimensions");
			setDimensions(dim[0],dim[1]);
		}

		// check for arguments not supported
		if(parameters.size()>0) {
			throw eng.getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+eng.getListUtil().toList(parameters.keys(),", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Colormap, Dimensions]",null);
		}

		return filter(src, dst);
	}
}