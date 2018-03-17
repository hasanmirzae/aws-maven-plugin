package com.epam.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 *  Goal to update a AWS lambda.
 */
@Mojo( name = "updateLambda", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST )
public class UpdateLambdaMojo extends AbstractLambdaMojo
{
    /**
     * Function description.
     */
    @Parameter( property = "description", required = false )
    private String description;
    /**
     * Alias.
     */
    @Parameter( property = "alias", required = false)
    private String alias;
    /**
     * Variables.
     */
    @Parameter( property = "alias", required = true)
    private Map variables;
    /**
     * Lambda jar file.
     */
    @Parameter( property = "path", required = true)
    private File path;

    private AWSLambda lambda;


    public void execute() throws MojoExecutionException {
        logger.info(String.format("Updating %s ...",lambdaName));
        lambda = createLambdaClient();
        String revisionId = lambda.updateFunctionConfiguration(createUpdateConfiguration()).getRevisionId();
        logger.info("New revisionId: " + revisionId);
        String version = lambda.updateFunctionCode(createUpdateRequest(revisionId)).getVersion();
        logger.info("New version: " + version);
        updateAlias(version);
    }

    private AWSLambda createLambdaClient() throws MojoExecutionException {
        return AWSLambdaClientBuilder
                .standard()
                .withCredentials(getCredentials())
                .withRegion(region)
                .build();
    }

    private void updateAlias(String version) {
        if (Objects.isNull(alias) || alias.isEmpty())
            return;
        UpdateAliasResult result = lambda.updateAlias(new UpdateAliasRequest()
                .withName(alias)
                .withFunctionName(lambdaName)
                .withFunctionVersion(version)
                .withDescription(getDescription()));
        logger.info("Update result:" + result.toString());
    }

    private String getDescription(){
        String timestamp = Instant.now().toString();
        return description == null ? timestamp : description + " " + timestamp;
    }

    private UpdateFunctionCodeRequest createUpdateRequest(String revisionId) throws MojoExecutionException {
        return new UpdateFunctionCodeRequest()
                .withFunctionName(lambdaName)
                .withRevisionId(revisionId)
                .withZipFile(getByteBuffer(path))
                .withPublish(true);
    }

    private UpdateFunctionConfigurationRequest createUpdateConfiguration(){
        logger.debug("Preparing configuration ...");
        return new UpdateFunctionConfigurationRequest()
                .withFunctionName(lambdaName)
                .withEnvironment(new Environment().withVariables(variables))
                .withDescription(description);
    }

}
