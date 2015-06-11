package rm.nw.gradle.util;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.SourceSet;

public class ClasspathUtil {

  /**
   * Setup the the 'providedCompile' and 'providedRuntime' configurations, just like War.
   * TODO See if we can recursively get all the dependent projects and apply it to them too.
   */
  public static void configureProvidedConfigurations(final Project project) {
    ConfigurationContainer configurationContainer = project.getConfigurations();
    //TODO conditionally add using either maybeCreate or findByName
    Configuration provideCompileConfiguration = configurationContainer.create(WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME)
                                                .setVisible(false)
                                                .setDescription("Additional compile classpath for libraries that should not be part of the archive.");
    Configuration provideRuntimeConfiguration = configurationContainer.create(WarPlugin.PROVIDED_RUNTIME_CONFIGURATION_NAME)
                                                .setVisible(false)
                                                .extendsFrom(provideCompileConfiguration)
                                                .setDescription("Additional runtime classpath for libraries that should not be part of the archive.");
    configurationContainer.getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME).extendsFrom(provideCompileConfiguration);
    configurationContainer.getByName(JavaPlugin.RUNTIME_CONFIGURATION_NAME).extendsFrom(provideRuntimeConfiguration);
  }

  public static Set<Project> getAllDependentProjects(final Project project) {
    //System.out.println("--------getAllDependentProjects--------");
    Set<Project> allDependentProjects = new HashSet<Project>();
    //Set<ProjectDependency> set = new HashSet<ProjectDependency>();
    DomainObjectSet<ProjectDependency> directDependentProjects = getDirectDependentProjects(project);
    //System.out.println("--directDependentProjects.size(): " + directDependentProjects.size());
    for (ProjectDependency projectDependency : directDependentProjects) {
      Project dependentProject = projectDependency.getDependencyProject();
      //System.out.println("------project: " + dependentProject);
      allDependentProjects.add(dependentProject);
      if (dependentProject!=null) {
        allDependentProjects.addAll(getAllDependentProjects(dependentProject));
      }
    }
    return allDependentProjects;
  }

  //	def getAllDependentProjects(project) {
  //        def projectDependencies = project.configurations.runtime.getAllDependencies().withType(ProjectDependency)
  //        //println '****' + (projectDependencies)
  //        def dependentProjects = projectDependencies*.dependencyProject
  //        if (dependentProjects.size > 0) {
  //            dependentProjects.each { dependentProjects += getAllDependentProjects(it) }
  //        }
  //        return dependentProjects.unique()
  //    }

  public static DomainObjectSet<ProjectDependency> getDirectDependentProjects(final Project project) {
    return project.getConfigurations()
           .getByName(JavaPlugin.RUNTIME_CONFIGURATION_NAME)
           .getAllDependencies().withType(ProjectDependency.class);
  }

  public static FileCollection getRuntimeClasspathWithoutProvided(final Project project) {
    return getRuntimeClasspath(project).minus(getProvidedRuntimeClasspath(project));
  }

  public static Set<File> getCompileClasspathWithoutProvidedFiles(final Project project) {
    Set<File> files = getCompileClasspath(project).getFiles();
    files.removeAll(getRecursiveProvidedCompileClasspathFiles(project));
    return files;
  }

  //	public static void getOwnMainSources(final Project project) {
  //		SourceSet mainSet = project.getConvention().getPlugin(JavaPluginConvention.class)
  //			.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
  //		System.out.println("MainSet Sources: " + mainSet.getAllSource().getFiles());
  //		System.out.println("MainSet Java: " + mainSet.getAllJava().getFiles());
  //		System.out.println("MainSet Compile CP: " + mainSet.getCompileClasspath().getFiles());
  //	}

  public static FileCollection getRuntimeClasspath(final Project project) {
    return getSourceSet(project, SourceSet.MAIN_SOURCE_SET_NAME)
        .getRuntimeClasspath();
  }

  public static FileCollection getCompileClasspath(final Project project) {
    return getSourceSet(project, SourceSet.MAIN_SOURCE_SET_NAME)
        .getCompileClasspath();
  }
  
  /**
   * Helper to get the source set by name
   */
  public static SourceSet getSourceSet(final Project project, final String name) {
    return project.getConvention().getPlugin(JavaPluginConvention.class)
           .getSourceSets().getByName(name);
  }
  
  /**
   * Get the 'providedRuntime' configuration of the specified project.
   */
  public static FileCollection getProvidedRuntimeClasspath(final Project project) {
    return getConfiguration(project, WarPlugin.PROVIDED_RUNTIME_CONFIGURATION_NAME);
  }
  
  /**
   * Get the 'providedCompile' configuration of the specified project.
   */
  public static FileCollection getProvidedCompileClasspath(final Project project) {
    Configuration configuration = project.getConfigurations().findByName(WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME);
    System.out.println(WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME + ":" + configuration);
    return configuration != null ? configuration : EmptyFileCollection.getInstance();
  }
  
  /**
   * Helper to get the configuration by name, and provide an empty collection if nothing was found. 
   */
  public static FileCollection getConfiguration(final Project project, final String name) {
    Configuration configuration = project.getConfigurations().findByName(name);
    return configuration != null ? configuration : EmptyFileCollection.getInstance();
  }
  
  /**
   * Get the 'providedCompile' configuration from the project and its project dependencies.
   */
  public static Set<File> getRecursiveProvidedCompileClasspathFiles(final Project project) {
    Set<Project> allDependentProjects = getAllDependentProjects(project);
    Set<File> files = getProvidedCompileClasspath(project).getFiles();
    for (Project dependentProject : allDependentProjects) {
      FileCollection dependentCompileProject = getProvidedCompileClasspath(dependentProject);
      files.addAll(dependentCompileProject.getFiles());
    }
    return files;
  }
  
}
