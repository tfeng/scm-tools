package com.bacoder.scmtools.git;

import java.io.File;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ProgressMonitor;

public class GitConfig {

  private String branch = Constants.MASTER;

  private ObjectId commit;

  private File directory;

  private boolean includeSubmodule = true;

  private ProgressMonitor progressMonitor = NullProgressMonitor.INSTANCE;

  public String getBranch() {
    return branch;
  }

  public ObjectId getCommitRevision() {
    return commit;
  }

  public File getDirectory() {
    return directory;
  }

  public boolean getIncludeSubmodule() {
    return includeSubmodule;
  }

  public ProgressMonitor getProgressMonitor() {
    return progressMonitor;
  }

  public GitConfig setBranch(String branch) {
    this.branch = branch;
    return this;
  }

  public GitConfig setCommitRevision(ObjectId commit) {
    this.commit = commit;
    return this;
  }

  public GitConfig setDirectory(File directory) {
    this.directory = directory;
    return this;
  }

  public GitConfig setIncludeSubmodule(boolean includeSubmodule) {
    this.includeSubmodule = includeSubmodule;
    return this;
  }

  public GitConfig setProgressMonitor(ProgressMonitor progressMonitor) {
    this.progressMonitor = progressMonitor;
    return this;
  }
}
