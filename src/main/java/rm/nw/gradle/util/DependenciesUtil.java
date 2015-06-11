package rm.nw.gradle.util;

import java.io.File;
import java.util.Collections;
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

public class DependenciesUtil {

  /**
   * Setup the the 'providedCompile' and 'providedRuntime' configurations, just like War.
   * TODO See if we can recursively get all the dependent projects and apply it to them too. 
   * But it would have to be a future action.
   */
  public static void configureProvidedConfigurations(final Project project) {
    ConfigurationContainer configurationContainer = project.getConfigurations();
    Configuration provideCompileConfiguration = configurationContainer.findByName(WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME);
    if (provideCompileConfiguration==null) {
      provideCompileConfiguration = configurationContainer.create(WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME)
          .setVisible(false)
          .setDescription("Additional compile classpath for libraries that should not be part of the archive.");
      configurationContainer.getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME).extendsFrom(provideCompileConfiguration);
    }
    Configuration provideRuntimeConfiguration = configurationContainer.findByName(WarPlugin.PROVIDED_RUNTIME_CONFIGURATION_NAME);
    if (provideRuntimeConfiguration==null) {
      provideRuntimeConfiguration = configurationContainer.create(WarPlugin.PROVIDED_RUNTIME_CONFIGURATION_NAME)
          .setVisible(false)
          .extendsFrom(provideCompileConfiguration)
          .setDescription("Additional runtime classpath for libraries that should not be part of the archive.");
      configurationContainer.getByName(JavaPlugin.RUNTIME_CONFIGURATION_NAME).extendsFrom(provideRuntimeConfiguration);
    }
  }
  
  /**
   * 'compile' (dependencies and sourceSet) minus ('providedCompile' and 'provided')
   */
  public static Set<File> getCompileFilesMinusProvided(final Project project) {
    Set<File> files = getCompileClasspath(project).getFiles();
    files.removeAll(getAllProvidedFilesRecursively(project));
    return files;
  }
  
  private static FileCollection getCompileClasspath(final Project project) {
    return getSourceSet(project, SourceSet.MAIN_SOURCE_SET_NAME)
        .getCompileClasspath();
  }

  /**
   * Helper to get the source set by name
   */
  private static SourceSet getSourceSet(final Project project, final String name) {
    return project.getConvention().getPlugin(JavaPluginConvention.class)
           .getSourceSets().getByName(name);
  }

  /**
   * Get the 'providedCompile' configuration from the project and its project dependencies.
   */
  private static Set<File> getAllProvidedFilesRecursively(final Project project) {
    Set<Project> projectDependencies = getProjectTypeDependencies(project, true); //recursive=true
    Set<File> files = getProvidedXFiles(project);
    for (Project dependecyProject : projectDependencies) {
      files.addAll(getProvidedXFiles(dependecyProject));
    }
    return files;
  }
  
  /**
   * Helper to get project dependent, optionally recursively.
   */
  private static Set<Project> getProjectTypeDependencies(final Project project, boolean recursive) {
    //System.out.println("--------getProjectDependencies--------");
    Set<Project> result = new HashSet<Project>();
    //Set<ProjectDependency> set = new HashSet<ProjectDependency>();
    DomainObjectSet<ProjectDependency> directProjectTypeDependent = getDirectProjectTypeDependencies(project);
    //System.out.println("--directDependentProjects.size(): " + directDependentProjects.size());
    for (ProjectDependency projectTypeDependency : directProjectTypeDependent) {
      Project dependentProject = projectTypeDependency.getDependencyProject();
      //System.out.println("------project: " + dependentProject);
      if (dependentProject!=null) {
        result.add(dependentProject);
        if (recursive) {
          result.addAll(getProjectTypeDependencies(dependentProject, recursive));
        }
      }
    }
    return result;
  }
  
  /**
   * Helper to get direct project type dependencies
   */
  private static DomainObjectSet<ProjectDependency> getDirectProjectTypeDependencies(final Project project) {
    return project.getConfigurations()
           .getByName(JavaPlugin.RUNTIME_CONFIGURATION_NAME)
           .getAllDependencies().withType(ProjectDependency.class);
  }
  
  /**
   * Helper to get all the provided, providedRuntime, and provdedCompile files (jars)
   * from a project. non-recursively
   */
  private static Set<File> getProvidedXFiles(final Project project) {
    Set<File> set = getConfigurationFiles(project, WarPlugin.PROVIDED_RUNTIME_CONFIGURATION_NAME);
    set.addAll(getConfigurationFiles(project, "provided")); //from propdeps and maven
    set.addAll(getConfigurationFiles(project, WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME));
    return set;
  }
  
  /**
   * Helper to get the configuration files by name, and provide an empty collection if nothing was found.
   */
  private static Set<File> getConfigurationFiles(final Project project, final String name) {
    Configuration configuration = project.getConfigurations().findByName(name);
    if (configuration != null) {
      return configuration.getFiles();
    }
    else {
      //does not work in a ternary expression
      return Collections.emptySet();
    }
  }
  
///**
// * 'runtime' (dependencies and sourceSet) minus 'providedRuntime'
// */
//private static FileCollection getRuntimeClasspathWithoutProvided(final Project project) {
//  return getRuntimeClasspath(project).minus(getProvidedRuntimeClasspath(project));
//}

//    public static void getOwnMainSources(final Project project) {
//        SourceSet mainSet = project.getConvention().getPlugin(JavaPluginConvention.class)
//            .getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
//        System.out.println("MainSet Sources: " + mainSet.getAllSource().getFiles());
//        System.out.println("MainSet Java: " + mainSet.getAllJava().getFiles());
//        System.out.println("MainSet Compile CP: " + mainSet.getCompileClasspath().getFiles());
//    }

//  private static FileCollection getRuntimeClasspath(final Project project) {
//    return getSourceSet(project, SourceSet.MAIN_SOURCE_SET_NAME)
//        .getRuntimeClasspath();
//  }

//  /**
//   * Get the 'providedRuntime' configuration of the specified project.
//   */
//  private static FileCollection getProvidedRuntimeClasspath(final Project project) {
//    return getConfigurationCollection(project, WarPlugin.PROVIDED_RUNTIME_CONFIGURATION_NAME);
//  }

//  /**
//   * Get the 'providedCompile' configuration of the specified project.
//   */
//  private static FileCollection getProvidedCompileCollection(final Project project) {
//    return getConfigurationCollection(project, WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME);
//  }

//  /**
//   * Helper to get the configuration by name, and provide an empty collection if nothing was found.
//   */
//  private static FileCollection getConfigurationCollection(final Project project, final String name) {
//    Configuration configuration = project.getConfigurations().findByName(name);
//    return configuration != null ? configuration : EmptyFileCollection.getInstance();
//  }

}
