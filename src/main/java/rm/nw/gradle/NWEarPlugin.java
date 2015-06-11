package rm.nw.gradle;

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


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.publish.ArchivePublishArtifact;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.plugins.DefaultArtifactPublicationSet;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.java.archives.Manifest;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.plugins.ear.descriptor.DeploymentDescriptor;

import rm.nw.gradle.descriptor.SAPManifest;

/**
 * <p>
 * A {@link Plugin} with tasks which assemble a web application into a EAR file.
 * </p>
 */
public class NWEarPlugin implements Plugin<Project> {

  public static final String PLUGIN_GROUP = "SAP NetWeaver";
  public static final String NWEAR_TASK_NAME = "nwear";
  public static final String NWEAR_TASK_DESC = "Generates an SAP NetWeaver ear file";

  private final Instantiator instantiator;
  private final FileResolver fileResolver;
  private NWEar earTask;

  public NWEar getEarTask() {
    return earTask;
  }

  @Inject
  public NWEarPlugin(Instantiator instantiator, FileResolver fileResolver) {
    this.instantiator = instantiator;
    this.fileResolver = fileResolver;
  }

  @Override
  public void apply(final Project project) {
    project.getPlugins().apply(BasePlugin.class);

    final NWEarPluginConvention earPluginConvention = instantiator.newInstance(NWEarPluginConvention.class, fileResolver, instantiator);
    project.getConvention().getPlugins().put(NWEAR_TASK_NAME, earPluginConvention);
    earPluginConvention.setAppDirName("EarContent"); //default

    this.earTask = setupEarTask(project, earPluginConvention);
    wireEarTaskConventions(project, earPluginConvention);
    configureAppDirSources(project, earPluginConvention);
    configureDeploymentDescriptor(project, earPluginConvention);
    configureSapManifest(project, earPluginConvention);
    configureManifest(project, earPluginConvention, earTask);
  }

  /**
   * Setup the NW EAR Task.
   * Uses some magic to set up the default archive conventions, using the same name as the task (nwear).
   * For example nwear.archiveName = ''
   */
  private NWEar setupEarTask(final Project project, NWEarPluginConvention convention) {
    NWEar ear = project.getTasks().create(NWEAR_TASK_NAME, NWEar.class);
    ear.setDescription(NWEAR_TASK_DESC);
    ear.setGroup(PLUGIN_GROUP);
    project.getExtensions()
    .getByType(DefaultArtifactPublicationSet.class)
    .addCandidate(new ArchivePublishArtifact(ear));
    return ear;
  }

  /**
   * Configure the task to copy the contents of 'appDirName' into the archive.
   */
  private void configureAppDirSources(final Project project, final NWEarPluginConvention earPluginConvention) {
    //use a closure to defer the resolution of getAppDirName() to give build.gradle a chance to change it
    this.earTask.from(new Callable<FileCollection>() {
      public FileCollection call() throws Exception {
        return project.fileTree(earPluginConvention.getAppDirName());
      }
    });
  }

  /**
   * Configure the deployment descriptor convention with the defaults.
   */
  private void configureDeploymentDescriptor(final Project project, final NWEarPluginConvention convention) {
    DeploymentDescriptor deploymentDescriptor = convention.getDeploymentDescriptor();
    if (deploymentDescriptor != null) {
      if (deploymentDescriptor.getDisplayName() == null) {
        deploymentDescriptor.setDisplayName(project.getName());
      }
    }
  }

  /**
   * Configures new MANIFEST.MF with some defaults. Can be overridden in build.gradle
   */
  private void configureManifest(final Project project, final NWEarPluginConvention earPluginConvention, final NWEar earTask) {
    JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
    Manifest manifest = javaConvention.manifest(); //generates new DefaultManifest
    Attributes attributes = manifest.getAttributes();
    String nowString = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    attributes.put("Implementation-Version", nowString);
    attributes.put("Implementation-Title", project.getName());
    attributes.put("Implementation-Vendor-Id", project.getGroup().toString());
    attributes.put("Specification-Vendor", "SAP AG");
    earTask.setManifest(manifest);
  }

  /**
   * Configures new SAP_MANIFEST.MF with some defaults. Can be overridden in build.gradle
   */
  private void configureSapManifest(final Project project, final NWEarPluginConvention convention) {
    SAPManifest sapManifest = convention.getSapManifest();
    sapManifest.updateProjectDetails(project);
    //System.out.println("xxxxxx:" + sapManifest);
  }

  /**
   * Wire up the conventions.
   */
  private void wireEarTaskConventions(final Project project, final NWEarPluginConvention earConvention) {
    project.getTasks().withType(NWEar.class, new Action<NWEar>() {
      public void execute(NWEar task) {
        task.getConventionMapping().map("libDirName", new Callable<String>() {
          public String call() throws Exception { return earConvention.getLibDirName(); }
        });
        task.getConventionMapping().map("deploymentDescriptor", new Callable<DeploymentDescriptor>() {
          public DeploymentDescriptor call() throws Exception { return earConvention.getDeploymentDescriptor(); }
        });
        task.getConventionMapping().map("sapManifest", new Callable<SAPManifest>() {
          public SAPManifest call() throws Exception { return earConvention.getSapManifest(); }
        });
      }
    });
  }

}
