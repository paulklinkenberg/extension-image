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
package org.lucee.extension.image.functions;


import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;

import org.lucee.extension.image.Image;

import lucee.runtime.type.Struct;

public class ImageInfo extends FunctionSupport implements Function {
	
	public static Struct call(PageContext pc, Object source) throws PageException {
		return Image.createImage(pc, source, true, false,true,null).info();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==1) return call(pc, args[0]);
		throw exp.createFunctionException(pc, "ImageInfo", 1, 1, args.length);
	}
}