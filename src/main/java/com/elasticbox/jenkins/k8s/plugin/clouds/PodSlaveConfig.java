package com.elasticbox.jenkins.k8s.plugin.clouds;

import com.google.inject.Inject;

import com.elasticbox.jenkins.k8s.auth.Authentication;
import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.plugin.util.PluginHelper;
import com.elasticbox.jenkins.k8s.repositories.ChartRepository;
import com.elasticbox.jenkins.k8s.repositories.PodRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import hudson.Extension;
import hudson.RelativePath;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.List;
import java.util.logging.Logger;

public class PodSlaveConfig implements Describable<PodSlaveConfig> {
    private static final Logger LOGGER = Logger.getLogger(PodSlaveConfig.class.getName());

    private final String id;
    private final String description;
    private final String podYaml;

    @DataBoundConstructor
    public PodSlaveConfig(String id, String description, String podYaml) {
        this.id = id;
        this.description = description;
        this.podYaml = podYaml;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getPodYaml() {
        return podYaml;
    }

    @Override
    public Descriptor<PodSlaveConfig> getDescriptor() {
        final Jenkins instance = Jenkins.getInstance();
        return (instance != null) ? instance.getDescriptor(getClass() ) : null;
    }

    @Override
    public String toString() {
        return "PodSlaveConfig [" + getDescription() + "] + " + getPodYaml();
    }


    @Extension
    public static class DescriptorImpl extends Descriptor<PodSlaveConfig> {

        private static final String POD_SLAVE_CONFIGURATION = "Pod Slave Configuration";

        @Inject
        transient PodRepository podRepository;

        @Override
        public String getDisplayName() {
            return POD_SLAVE_CONFIGURATION;
        }

        public FormValidation doTestYaml(@QueryParameter String podYaml,
                                         @RelativePath("..") @QueryParameter String name,
                                         @RelativePath("..") @QueryParameter String namespace) {

            if (PluginHelper.anyOfThemIsBlank(podYaml, name, namespace) ) {
                return FormValidation.error("Required fields not provided");
            }

            try {
                final boolean validYaml = podRepository.testYaml(name, namespace, podYaml);

                if (validYaml) {
                    return FormValidation.ok("Validation successful");
                } else {
                    return FormValidation.error("Invalid Pod Yaml definition");
                }
            } catch (RepositoryException excep) {
                return FormValidation.error("Validation error - " + excep.getCausedByMessages() );
            }
        }

        public ListBoxModel doFillCredentialsIdItems(@QueryParameter String endpointUrl) {
            return PluginHelper.doFillCredentialsIdItems(endpointUrl);
        }
    }

}
