/**
 * Copyright (c) 2010 The Plaid Group (see AUTHORS file)
 * 
 * This file is part of Plaid Programming Language.
 *
 * Plaid Programming Language is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 *  Plaid Programming Language is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Plaid Programming Language.  If not, see <http://www.gnu.org/licenses/>.
 */

package build;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import plaid.compilerjava.CompilerConfiguration;
import plaid.compilerjava.CompilerCore;

public class PlaidStandardLibrary {
//	private File f;
//	public PlaidStandardLibrary(File f) {
//		this.f = f;
//	}
	
	@Test
	public void BuildPlaidStandardLibrary () throws FileNotFoundException {
		CompilerConfiguration cc = new CompilerConfiguration();
		cc.setOutputDir("src");
		for (File f : inputFiles())
			cc.addInputFile(f);
		cc.setKeepTemporaryFiles(true);
		cc.setInvokeCompiler(false);
		cc.setDebugMode(true);
		cc.setPrettyPrint(true);
		cc.setCopyrightHeader(true);
		cc.addToPlaidPath("bin/");
		CompilerCore compiler = new CompilerCore(cc);
		compiler.compile();
	}

	private static List<File> inputFiles() {
		List<File> results = new ArrayList<File>();
	    String currentdir = System.getProperty("user.dir") + System.getProperty("file.separator"); // should be work space by default
	    File cur = new File(currentdir);
	    assertTrue( cur.isDirectory() );
	    for (File f : cur.listFiles()) {
			if ( f.getName().startsWith("plaid")) {
				List<File> files = new ArrayList<File>();
				findPlaidFile(f, files);
				for (File file : files) {
					File f1 = new File(file.getAbsolutePath().substring(currentdir.length()));
					results.add(f1);
				}
			}
		}
	    return results;
	}
	
	private static void findPlaidFile(File f, List<File> files) {
		if ( f.isFile() ) {
			if ( f.getName().endsWith(".plaid")) {
				files.add(f);
			}
		} else if ( f.isDirectory() ) {		
			for (File file : f.listFiles()) {
				findPlaidFile(file, files);
			}
		}
	}
	
}
