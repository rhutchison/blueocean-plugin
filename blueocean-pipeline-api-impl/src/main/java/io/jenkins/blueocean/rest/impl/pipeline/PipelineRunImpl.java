package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.Extension;
import hudson.model.Queue;
import hudson.model.Run;
import hudson.plugins.git.util.BuildData;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueChangeSetEntry;
import io.jenkins.blueocean.rest.model.BluePipelineNodeContainer;
import io.jenkins.blueocean.rest.model.BluePipelineStepContainer;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.Containers;
import io.jenkins.blueocean.service.embedded.rest.AbstractRunImpl;
import io.jenkins.blueocean.service.embedded.rest.BlueRunFactory;
import io.jenkins.blueocean.service.embedded.rest.ChangeSetResource;
import io.jenkins.blueocean.service.embedded.rest.QueueContainerImpl;
import io.jenkins.blueocean.service.embedded.rest.StoppableRun;
import org.jenkinsci.plugins.workflow.cps.replay.ReplayAction;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.steps.input.InputAction;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.export.Exported;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.JENKINS_WORKFLOW_RUN;

/**
 * Pipeline Run
 *
 * @author Vivek Pandey
 */
@Capability(JENKINS_WORKFLOW_RUN)
public class PipelineRunImpl extends AbstractRunImpl<WorkflowRun> {
    public PipelineRunImpl(WorkflowRun run, Link parent) {
        super(run, parent);
    }

    @Override
    public Container<BlueChangeSetEntry> getChangeSet() {
        Map<String, BlueChangeSetEntry> m = new LinkedHashMap<>();
        int cnt = 0;
        for (ChangeLogSet<? extends Entry> cs : run.getChangeSets()) {
            for (ChangeLogSet.Entry e : cs) {
                cnt++;
                String id = e.getCommitId();
                if (id == null) id = String.valueOf(cnt);
                m.put(id, new ChangeSetResource(e, this));
            }
        }
        return Containers.fromResourceMap(getLink(),m);
    }

    @Override
    public BlueRunState getStateObj() {
        InputAction inputAction = run.getAction(InputAction.class);
        if(inputAction != null && inputAction.getExecutions().size() > 0){
            return BlueRunState.PAUSED;
        }
        return super.getStateObj();
    }

    @Override
    public BlueQueueItem replay() {
        ReplayAction replayAction = run.getAction(ReplayAction.class);
        if(replayAction == null) {
            throw new ServiceException.BadRequestExpception("This run does not support replay");
        }

        Queue.Item item = replayAction.run2(replayAction.getOriginalScript(), replayAction.getOriginalLoadedScripts());

        BlueQueueItem queueItem = QueueContainerImpl.getQueuedItem(item, run.getParent());

        if(queueItem == null) {
            throw new ServiceException.UnexpectedErrorException("Run was not added to queue.");
        } else {
            return queueItem;
        }
    }

    @Override
    public BluePipelineNodeContainer getNodes() {
        if (run != null) {
            return new PipelineNodeContainerImpl(run, getLink());
        }
        return null;
    }

    @Override
    public BluePipelineStepContainer getSteps() {
        return new PipelineStepContainerImpl(run, getLink());
    }

    @Override
    public BlueRun stop(@QueryParameter("blocking") Boolean blocking, @QueryParameter("timeOutInSecs") Integer timeOutInSecs){
        return stop(blocking, timeOutInSecs, new StoppableRun() {
            @Override
            public void stop() {
                run.doStop();
            }
        });
    }


    @Exported(name = "commitId")
    public String getCommitId() {
        BuildData data = run.getAction(BuildData.class);

        if (data == null
            || data.getLastBuiltRevision() == null
            || data.getLastBuiltRevision().getSha1String() == null) {
            return null;
        } else {
            return data.getLastBuiltRevision().getSha1String();
        }
    }

    @Extension(ordinal = 1)
    public static class FactoryImpl extends BlueRunFactory {

        @Override
        public BlueRun getRun(Run run, Reachable parent) {
            if(run instanceof WorkflowRun) {
                return new PipelineRunImpl((WorkflowRun) run, parent.getLink());
            }
            return null;
        }
    }

}
