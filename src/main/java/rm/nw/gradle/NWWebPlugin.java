/**
 * Copyright (C) 2015 Ryan Murphy
 */
package rm.nw.gradle;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.internal.reflect.Instantiator;

import rm.nw.gradle.NWEar;


/**
 * NWWebPlugin class.
 * Automatically applies the NWEarPlugin (by extending) as well as the War plugin.
 *
 * @version 1.0.0
 */
public class NWWebPlugin extends NWEarPlugin {

  @Inject
  public NWWebPlugin(Instantiator instantiator, FileResolver fileResolver) {
    super(instantiator, fileResolver);
  }

  @Override
  public void apply(final Project project) {
    //System.out.println("------Applying NWWebPlugin to " + project.getName());
    super.apply(project);

    project.getPlugins().apply(WarPlugin.class);

    NWEar earTask = super.getEarTask();
    Task warTask = project.getTasks().findByName("war");
    earTask.dependsOn(warTask);

    //defer adding the war name to the 'from' list
    //since the build.gradle may change it still
    Action<NWEar> beforeCopy = new Action<NWEar>() {
      @Override
      public void execute(NWEar earTask) {
        //System.out.println("----------deferred--------");
        Task warTask = earTask.getProject().getTasks().getByName("war");
        if (warTask instanceof AbstractArchiveTask) {
          AbstractArchiveTask archiveTask = (AbstractArchiveTask)warTask;
          System.out.println("from : " + project.file(archiveTask.getArchivePath()));
          earTask.from(project.file(archiveTask.getArchivePath()));
        }
      }
    };

    earTask.addBeforeCopyAction(beforeCopy);
  }

}
