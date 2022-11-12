package io.jenkins.plugins.devopsportal;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;

@Extension
public class JobListener extends RunListener<AbstractBuild<?, ?>> {

    public BuildStatus.DescriptorImpl getBuildStatusDescriptor() {
        return Jenkins.get().getDescriptorByType(BuildStatus.DescriptorImpl.class);
    }

    @Override
    public void onCompleted(AbstractBuild build, @NonNull TaskListener listener) {
        Result result = build.getResult();
        String projectName = build.getProject().getDisplayName();
        int number = build.getNumber();
        if (projectName != null && number > 0 && result != null && getBuildStatusDescriptor() != null) {
            getBuildStatusDescriptor().update(projectName, number, record -> {
                record.setBuildResult(result);
            });
        }
    }

    @Override
    public void onFinalized(AbstractBuild r) {
    }

}
