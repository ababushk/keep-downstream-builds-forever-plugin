package io.jenkins.plugins.keepdownstreambuilds;

import com.axis.system.jenkins.plugins.downstream.cache.BuildCache;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.Run;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;

public class KeepDownstreamBuildsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(KeepDownstreamBuildsAction.class.getName());

    private transient Job<?, ?> project;
    private transient Run<?, ?> build;

    public KeepDownstreamBuildsAction(Run<?, ?> build, Job<?, ?> project) {
        this.build = build;
        this.project = project;
    }

    public void doIndex(StaplerRequest request, StaplerResponse response) throws IOException, ServletException, InterruptedException {
        Boolean keep = !build.isKeepLog();
        build.keepLog(keep);
        keepDownstreamBuildTree(build, keep);
        response.sendRedirect("../");
    }

    public void keepDownstreamBuildTree(Run build, Boolean keep) throws IOException {
        for (Run downstreamBuild : BuildCache.getCache().getDownstreamBuilds(build)) {
            if (keep) {
                logger.info("Keep downstream builds of " + downstreamBuild.getFullDisplayName());
            } else {
                logger.info("Don't keep downstream builds of " + downstreamBuild.getFullDisplayName());
            }
            downstreamBuild.keepLog(keep);
            keepDownstreamBuildTree(downstreamBuild, keep);
        }
    }

    public boolean projectHasDiscarderProperty() {
        JobProperty discarderProperty = project.getProperty(jenkins.model.BuildDiscarderProperty.class);

        return discarderProperty != null;
    }

    public boolean buildHasDownstreamBuilds() {
        return !BuildCache.getCache().getDownstreamBuilds(build).isEmpty();
    }

    @Override
    public String getIconFileName() {
        if (projectHasDiscarderProperty() && buildHasDownstreamBuilds()) {
            if (build.isKeepLog()) {
                return "folder-delete.png";
            } else {
                return "save.png";
            }
        } else {
            return null;
        }
    }

    @Override
    public String getDisplayName() {
        if (build.isKeepLog()) {
            return "Don't keep downstream builds forever";
        } else {
            return "Keep downstream builds forever";
        }
    }

    @Override
    public String getUrlName() {
        return "keepdownstreambuilds";
    }
}
