package io.jenkins.plugins.keepdownstreambuilds;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Queue;
import hudson.model.Run;
import jenkins.model.TransientActionFactory;

import javax.annotation.Nonnull;
import java.util.Collection;

import static java.util.Collections.*;

/* Setup KeepDownstreamBuildsAction for every build in Jenkins */
@Extension
public class KeepDownstreamBuildsActionFactory extends TransientActionFactory<Run> {
    @Override
    public Class<Run> type() {
        return Run.class;
    }

    @Nonnull
    @Override
    public Collection<? extends Action> createFor(@Nonnull Run build) {
        if (!(build.getParent() instanceof Queue.Task)) {
            return emptyList();
        }
        return singleton(new KeepDownstreamBuildsAction(build, build.getParent()));
    }
}
