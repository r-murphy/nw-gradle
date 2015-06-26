/**
 * Copyright (C) 2015 Ryan Murphy
 */
package rm.nw.gradle;

import java.io.File;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.internal.reflect.Instantiator;

import rm.nw.gradle.util.DependenciesUtil;

/**
 * NWWebPlugin class.
 *
 * @version 1.0.0
 */
public class NWEjbPlugin implements Plugin<Project> {

  @Inject
  public NWEjbPlugin(Instantiator instantiator, FileResolver fileResolver) {
    //super(instantiator, fileResolver);
  }

  @Override
  public void apply(final Project project) {
    project.getPlugins().apply(JavaPlugin.class);
    project.getPlugins().apply(NWEarPlugin.class);
    //    super.apply(project);
    //NWEar earTask = super.getEarTask();
    NWEar earTask = (NWEar)project.getTasks().findByName("nwear");
    DependenciesUtil.configureProvidedConfigurations(project);
    configureJarTaskDependency(project, earTask);
    configureDependencyJarFileSources(project, earTask);

    //Tells SAPManifest to include the dependencies section. Needed for ejb ears, but not web.
    //this is configuration time when they apply the plugin, so the user may still override it in build.gradle
    //after applying the plugin
    //System.out.println("earTask.getSapManifest():"+earTask.getSapManifest());
    earTask.getSapManifest().setIncludeDependencies(true);
  }

  /**
   * NW EJB Ear files contain the jar. So tell gradle to build the jar first, and add it to the copyspec.
   */
  private void configureJarTaskDependency(final Project project, final NWEar earTask) {
    Task jarTask = project.getTasks().findByName("jar");
    earTask.dependsOn(jarTask);

    //defer adding the jar name to the 'from' list
    //since the build.gradle may change it still
    Action<NWEar> beforeEarCopy = new Action<NWEar>() {
      @Override
      public void execute(NWEar earTask) {
        Task jarTask = earTask.getProject().getTasks().getByName("jar");
        if (jarTask instanceof AbstractArchiveTask) {
          AbstractArchiveTask archiveTask = (AbstractArchiveTask)jarTask;
          //System.out.println("from : " + project.file(archiveTask.getArchivePath()));
          earTask.from(project.file(archiveTask.getArchivePath()));
        }
      }
    };
    earTask.addBeforeCopyAction(beforeEarCopy);
  }

  /**
   * Configures which jar files to include in the archive.
   * Based on the dependencies and sourceSets.
   *
   * Included files are:
   *  runtimeJars(includes providedJars) minus providedJars
   */
  private void configureDependencyJarFileSources(final Project project, final NWEar earTask) {
    /**
     * Add a dependsOn callback to the task, which will get executed
     * once gradle has resolved all the dependencies.
     */
    project.getTasks().withType(NWEar.class, new Action<NWEar>() {
      @Override
      public void execute(final NWEar task) {
        task.dependsOn(new Callable<Object>() {
          public Object call() throws Exception {
            //System.out.println("-------NWEar.dependsOn.execute()----------------");
            Set<File> jars = DependenciesUtil.getCompileFilesMinusProvided(project);
            task.from(jars);
            return null;
          }
        });
      }
    });
  }

}
