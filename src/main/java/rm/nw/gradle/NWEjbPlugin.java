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
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.internal.reflect.Instantiator;

import rm.nw.gradle.util.DependenciesUtil;

/**
 * NWWebPlugin class.
 *
 * @version 1.0.0
 */
public class NWEjbPlugin implements Plugin<Project> {
  
  private static final String MANIFEST_MF = "MANIFEST.MF";
  private static final String META_INF = "META-INF";
  
  
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
    configureJarMetaInfCopy(project);
    

    //Tells SAPManifest to include the dependencies section. Needed for ejb ears, but not web.
    //this is configuration time when they apply the plugin, so the user may still override it in build.gradle
    //after applying the plugin
    //System.out.println("earTask.getSapManifest():"+earTask.getSapManifest());
    earTask.getSapManifest().setIncludeDependencies(true);
  }
  
  private static String getMetaInfFolderInPath(String filePath) {
    int indexOf = filePath.indexOf(META_INF);
    if (indexOf == -1) {
      return null;
    }
    return filePath.substring(0, indexOf + META_INF.length());
  }
  
  /**
   * NW EJB Ear files contain the jar. So tell gradle to build the jar first, and add it to the copyspec.
   * Use an action for the copyspec in case the jar name is changed in build.gradle.
   */
  private void configureJarTaskDependency(final Project project, final NWEar earTask) {
    Jar jarTask = (Jar)project.getTasks().findByName("jar");
    earTask.dependsOn(jarTask);
    
    Action<Task> beforeEarCopy = new Action<Task>() {
      @Override
      public void execute(Task task) {
        AbstractArchiveTask jarTask = (AbstractArchiveTask)earTask.getProject().getTasks().getByName("jar");
        ((NWEar)earTask).from(project.file(jarTask.getArchivePath()));
      }
    };
    earTask.doFirst(beforeEarCopy);
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
            Set<File> jars = DependenciesUtil.getCompileFilesMinusProvided(project);
            task.from(jars);
            return null;
          }
        });
      }
    });
  }

  /**
   * Searches the project for the META-INF folder and configures it in the jar task.
   * Using an Action to run after all build.gradle sourceSets are processed.
   */
  private void configureJarMetaInfCopy(final Project project) {
    project.getTasks().findByName("jar")
    .doFirst(new Action<Task>() {
      @Override
      public void execute(Task task) {
        Jar jarTask = (Jar)task;
        SourceSet mainSourceSet = DependenciesUtil.getMainSourceSet(project);
        
        Set<File> files = mainSourceSet.getAllSource().getFiles();
        boolean metaInfFound = false;
        boolean manifestFound = false;
        for (File file : files) {
          String path = file.getPath();
          if (!metaInfFound && path.contains(META_INF)) {
            metaInfFound = true;
            String metaInfPath = getMetaInfFolderInPath(path);
            jarTask.getMetaInf().from(metaInfPath)
              //jar creates its own metainf.mf first, so this would be a duplicate
              .exclude(MANIFEST_MF) 
              .setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
          }
          else if (!manifestFound && path.contains(MANIFEST_MF)) {
            manifestFound = true;
            //tell jar to use our manifest instead of creating one 
            jarTask.getManifest().from(path);
          }
          else if (metaInfFound && manifestFound) {
            break; //we found both. nothing else to do
          }
        }
      }
    });
  }

}
