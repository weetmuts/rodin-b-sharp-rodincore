/*******************************************************************************
 * Copyright (c) 2008 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.rodinp.internal.core.index;

import org.rodinp.core.IFileElementType;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.index.IIndexer;
import org.rodinp.core.index.IIndexingToolkit;

public class FileIndexingManager {

	private final IndexersManager indexersManager;

	public FileIndexingManager(IndexersManager indManager) {
		this.indexersManager = indManager;
	}

	public IRodinFile[] getDependencies(IRodinFile file) {
		final IFileElementType<? extends IRodinFile> fileType = file
				.getElementType();
		if (!indexersManager.isIndexable(fileType)) {
			throw new IllegalArgumentException("No known indexers for file: "
					+ file.getPath());
		}

		final IIndexer indexer = indexersManager.getIndexerFor(fileType);

		if (IndexManager.VERBOSE) {
			System.out.println("INDEXER: Extracting dependencies for file "
					+ file.getPath() + " with indexer " + indexer.getId());
		}
		final IRodinFile[] result = indexer.getDependencies(file);
		if (IndexManager.DEBUG) {
			System.out.println("INDEXER: Dependencies for file "
					+ file.getPath() + " are:");
			for (IRodinFile dep : result) {
				System.out.println("\t" + dep.getPath());
			}
		}
		return result;
	}

	public void doIndexing(IRodinFile file, IIndexingToolkit indexingToolkit) {
		final IFileElementType<? extends IRodinFile> fileType = file
				.getElementType();
		final IIndexer indexer = indexersManager.getIndexerFor(fileType);
		if (indexer == null) {
			return;
		}
		if (IndexManager.VERBOSE) {
			System.out.println("INDEXER: Indexing file " + file.getPath()
					+ " with indexer " + indexer.getId());
		}
		indexer.index(file, indexingToolkit);
	}
}
