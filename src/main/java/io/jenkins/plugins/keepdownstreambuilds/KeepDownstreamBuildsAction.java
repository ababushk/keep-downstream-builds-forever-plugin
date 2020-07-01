package io.jenkins.plugins.keepdownstreambuilds;

import com.axis.system.jenkins.plugins.downstream.cache.BuildCache;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Run;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;

@Extension
public class KeepDownstreamBuildsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(KeepDownstreamBuildsAction.class.getName());
    private transient Run<?, ?> build;

    public Run getCurrentBuild() {
        if (build != null) {
            return build.getParent();
        }

        Job currentProject = null;
        StaplerRequest request = Stapler.getCurrentRequest();
        if (request != null) {
            currentProject = request.findAncestorObject(Job.class);
        }

        return currentProject;
    }

    public void doIndex(StaplerRequest request, StaplerResponse response) throws IOException, ServletException, InterruptedException {
        logger.info("Keep build tree forever action clicked");
        Run currentBuild = request.findAncestorObject(Run.class);
        if (currentBuild != null) {
            currentBuild.keepLog(true);
            keepDownstreamBuildTree(currentBuild);

        }
        response.sendRedirect("../");
    }

    public void keepDownstreamBuildTree(Run build) throws IOException {
        for (Run downstreamBuild : BuildCache.getCache().getDownstreamBuilds(build)) {
            logger.info("Keep downstream builds of a " + downstreamBuild.getFullDisplayName());
            downstreamBuild.keepLog(true);
            keepDownstreamBuildTree(downstreamBuild);
        }
    }

    public void dontKeepDownstreamBuildTree(Run build) throws IOException {
        for (Run downstreamBuild : BuildCache.getCache().getDownstreamBuilds(build)) {
            logger.info("Don't keep downstream builds of a " + downstreamBuild.getFullDisplayName());
            downstreamBuild.keepLog(false);
            dontKeepDownstreamBuildTree(downstreamBuild);
        }
    }

    @Override
    public String getIconFileName() {

        return "save.png";
    }

    @Override
    public String getDisplayName() {
        return "Keep build tree forever";
    }

    @Override
    public String getUrlName() {
        return "keepdownstreambuilds";
    }
}
