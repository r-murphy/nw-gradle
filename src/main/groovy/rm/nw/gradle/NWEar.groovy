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
import rm.nw.gradle.descriptor.SdaDD

/**
 * Assembles a NW EAR archive.
 * Similar to an EAR, but includes SAP_MANIFEST.MF and sda-dd.xml
 *
 * The ordering of the task execution is important.
 * - Run the before copy actions.
 * - Perform the copy spec, keeping track of what files were copied.
 * - Create the SAP_MANIFEST.MF, if it wasn't copied.
 * - Create the sda-dd.xml (or similar) if it wasn't copied.
 *
 */
class NWEar extends Ear {

  private static final String APP_J2EE__ENGINE_XML_NAME = 'application-j2ee-engine.xml';
  String applicationJ2eeEngineXml;

  /**
   * The SAP_MANIFEST.MF configuration
   */
  SAPManifest sapManifest;

  /**
   * The sda-dd.xml configuration
   */
  SdaDD sdaDd;

  /**
   * Allows the plugin to specify Action to call before the copy.
   * Allows other plugins beside NWEarPlugin to control the actions,
   * such as NWWebPlugin specifying to load the war file.
   */
  protected final def beforeCopyActions = [];

  /**
   * NWEar Constructor
   * Note: groovy will automatically call super()
   */
  NWEar() {
    //monitor all the files getting copied, to see if an SAP_MANIFEST.MF or sda-dd.xml are included
    //note that after those files are added automatically below to the MetaInfSpec, this closure still runs
    mainSpec.eachFile { FileCopyDetails details ->
      //println('---'+details.getPath())
      if (this.sapManifest && this.sapManifest.fileName.equalsIgnoreCase(details.name)) {
        //SAP_MANIFEST.MF already exists in app dir. Don't generate.
        this.sapManifest = null;
      }
      else if (this.sdaDd && this.sdaDd.fileName.equals(details.name)) {
        //sda-dd.xml already exists in app dir. Don't generate.
        this.sdaDd = null;
      }
      else if (details.name==APP_J2EE__ENGINE_XML_NAME) {
        this.applicationJ2eeEngineXml = details.file;
        sapManifest.applicationJ2eeEngineFile = details.file;
      }
    }

    // create our own metaInf spec which runs after mainSpec's files
    // Needs to be 'after' in order to see if SAP_MANIFEST.MF and sda-dd.xml already exist
    def metaInfSpec = mainSpec.addChild().into('META-INF');

    metaInfSpec.addChild().from {
      //println '***sm1: ' + this.getProject().getName();
      MapFileTree temporarySource = new MapFileTree(getTemporaryDirFactory(), getFileSystem());
      final SAPManifest sapManifest = sapManifest; //very important to store this local ref as final to avoid null pointer
      if (sapManifest) {
        //println '***sm2 add: ' + this.getProject().getName() + '~' + sapManifest.fileName
        temporarySource.add(sapManifest.fileName, new Action<OutputStream>() {
          void execute(OutputStream outputStream) {
            //println '***sm3 writeTo: ' + this.getProject().getName() + '~' + sapManifest.fileName
            sapManifest.writeTo(new OutputStreamWriter(outputStream))
          }
        });
        return new FileTreeAdapter(temporarySource);
      }
      return null;
    }

    metaInfSpec.addChild().from {
      MapFileTree temporarySource = new MapFileTree(getTemporaryDirFactory(), getFileSystem());
      final SdaDD sdaDd = sdaDd; //very important to store this local ref as final to avoid null pointer
      if (this.sdaDd) {
        //println 'no sda-dd.xml'
        //println '***sda2 add: ' + this.getProject().getName() + '~' + sdaDd.fileName
        temporarySource.add(sdaDd.fileName, new Action<OutputStream>() {
          void execute(OutputStream outputStream) {
            //println '***sda3 writeTo: ' + this.getProject().getName() + '~' + sdaDd.fileName
            sdaDd.writeTo(new OutputStreamWriter(outputStream));
          }
        });
        return new FileTreeAdapter(temporarySource);
      }
      return null;
    }

    doLast {
      if (!this.applicationJ2eeEngineXml) {
        this.getLogger().warn("WARNING: No {} file found. Your application may encounter ClassNotFoundException on deploy.", APP_J2EE__ENGINE_XML_NAME);
      }
    }
  }

  protected addBeforeCopyAction(Action action) {
    this.beforeCopyActions.add(action);
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
