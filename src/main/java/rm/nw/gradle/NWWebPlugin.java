/**
 * Copyright (C) 2015 Ryan Murphy
 */
package rm.nw.gradle;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.internal.reflect.Instantiator;


/**
 * NWWebPlugin class.
 * Automatically applies the NWEarPlugin (by extending) as well as the War plugin.
 *
 * @version 1.0.0
 */
public class NWWebPlugin implements Plugin<Project> {

  @Inject
  public NWWebPlugin(Instantiator instantiator, FileResolver fileResolver) {
    //super(instantiator, fileResolver);
  }

  @Override
  public void apply(final Project project) {
    //project.getLogger().info("{}: Applying NWWebPlugin plugin", project);
    project.getPlugins().apply(WarPlugin.class);
    project.getPlugins().apply(NWEarPlugin.class);
    NWEar earTask = (NWEar)project.getTasks().findByName("nwear");

    Task warTask = project.getTasks().findByName("war");
    earTask.dependsOn(warTask);

    //Action to copy the war file into the ear file.
    //Use a beforeCopy action to defer adding the war name
    //to the copy 'from' list in case build.gradle changes the name
    Action<NWEar> beforeCopy = new Action<NWEar>() {
      @Override
      public void execute(NWEar earTask) {
        //System.out.println("----------deferred--------");
        Task warTask = earTask.getProject().getTasks().getByName("war");
        if (warTask instanceof AbstractArchiveTask) {
          AbstractArchiveTask archiveTask = (AbstractArchiveTask)warTask;
          //System.out.println("from : " + project.file(archiveTask.getArchivePath()));
          earTask.from(project.file(archiveTask.getArchivePath()));
        }
      }
    };

    earTask.addBeforeCopyAction(beforeCopy);
  }

}
