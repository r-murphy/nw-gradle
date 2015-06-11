package rm.nw.gradle


/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.gradle.api.Action
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.internal.file.collections.FileTreeAdapter
import org.gradle.api.internal.file.collections.MapFileTree
import org.gradle.api.internal.file.copy.CopySpecInternal
import org.gradle.plugins.ear.Ear

import rm.nw.gradle.descriptor.SAPManifest
import rm.nw.gradle.descriptor.SdaDd

/**
 * Assembles a NW EAR archive.
 * Similar to an EAR, but includes SAP_MANIFEST.MF and sda-dd.xml
 */
class NWEar extends Ear {
	
	/**
	 * The SAP_MANIFEST.MF configuration
	 */
	SAPManifest sapManifest
	
	/**
	 * The sda-dd.xml configuration
	 */
	SdaDd sdaDd = new SdaDd()
	
	/**
	 * Allows the plugin to specify Action to call before the copy.
	 * Allows other plugins beside NWEarPlugin to control the actions,
	 * such as NWWebPlugin specifying to load the war file.
	 */
	protected final def beforeCopyActions = []
	
	/**
	 * NWEar Constructor
	 * Note to self: groovy will automatically call super()
	 */
	NWEar() {
		mainSpec.eachFile { FileCopyDetails details ->
			println('---'+details.getPath())
			if (this.sapManifest && details.name.equalsIgnoreCase(this.sapManifest.fileName)) {
				//SAP_MANIFEST.MF already exists in app dir. Don't generate.
				this.sapManifest = null
			}
			else if (this.sdaDd && details.name.equals(this.sdaDd.fileName)) {
				//sda-dd.xml already exists in app dir. Don't generate.
				this.sdaDd = null
			}
		}
		
		// create our own metaInf spec which runs after mainSpec's files
		// Needs to be 'after' in order to see if SAP_MANIFEST.MF and sda-dd.xml already exist
		def metaInf = mainSpec.addChild().into('META-INF')
		
		metaInf.addChild().from {
			//println '***sm1: ' + this.getProject().getName();
			MapFileTree temporarySource = new MapFileTree(getTemporaryDirFactory(), getFileSystem())
			final SAPManifest sapManifest = sapManifest
			if (sapManifest) {
				//println '***sm2 add: ' + this.getProject().getName() + '~' + sapManifest.fileName
				temporarySource.add(sapManifest.fileName, new Action<OutputStream>() {
					void execute(OutputStream outputStream) {
						//println '***sm3 writeTo: ' + this.getProject().getName() + '~' + sapManifest.fileName
						sapManifest.writeTo(new OutputStreamWriter(outputStream))
					}
				});
				return new FileTreeAdapter(temporarySource)
			}
			return null
		}
		
		metaInf.addChild().from {
			MapFileTree temporarySource = new MapFileTree(getTemporaryDirFactory(), getFileSystem())
			final SdaDd sdaDd = sdaDd //very important to store this local ref to avoid null pointer
			if (this.sdaDd) {
				//println 'no sda-dd.xml'
				//println '***sda2 add: ' + this.getProject().getName() + '~' + sdaDd.fileName
				temporarySource.add(sdaDd.fileName, new Action<OutputStream>() {
					void execute(OutputStream outputStream) {
						//println '***sda3 writeTo: ' + this.getProject().getName() + '~' + sdaDd.fileName
						sdaDd.writeTo(new OutputStreamWriter(outputStream));
					}
				});
				return new FileTreeAdapter(temporarySource)
			}
			return null
		}
	}

	protected addBeforeCopyAction(Action action) {
		this.beforeCopyActions.add(action)
	}
	
	/**
	 * Override the default copy() task
	 * to call our beforeCopyActions
	 */
	@Override
	protected void copy() {
		for (action in this.beforeCopyActions) {
			action.execute(this);
		}
		super.copy();
	}
	
}
