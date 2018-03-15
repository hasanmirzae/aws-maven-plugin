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

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.*;
import com.amazonaws.util.IOUtils;
import com.epam.plugin.exceptions.BadConfigurationException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 *  Goal to update a AWS lambda.
 */
@Mojo( name = "updateLambda", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST )
public class UpdateLambdaMojo extends AbstractMojo
{
    private final Log logger = getLog();
    /**
     * Function name.
     */
    @Parameter( property = "lambdaName", required = true )
    private String lambdaName;
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
     * Region.
     */
    @Parameter( property = "region", required = true)
    private String region;
    /**
     * Lambda jar file.
     */
    @Parameter( property = "path", required = true)
    private File path;
    /**
     * Aws client key.
     */
    @Parameter( property = "accessKey", required = false)
    private String accessKey;
    /**
     * AWS client secret.
     */
    @Parameter( property = "secretKey", required = false)
    private String secretKey;
    /**
     * AWS credential profile.
     */
    @Parameter( property = "profile", required = false)
    private String profile;

    private AWSLambda lambda;


    public void execute() throws MojoExecutionException{
        try {
            lambda = createLambdaClient();
            String revisionId = lambda.updateFunctionConfiguration(createUpdateConfiguration()).getRevisionId();
            logger.info("New revisionId: " + revisionId);
            String version = lambda.updateFunctionCode(createUpdateRequest(revisionId)).getVersion();
            logger.info("New version: " + version);
            updateAlias(version);

        }catch (IOException | BadConfigurationException e){
            logger.error(e);
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private AWSLambda createLambdaClient() throws BadConfigurationException {
        return AWSLambdaClientBuilder
                .standard()
                .withCredentials(getCredentils())
                .withRegion(region)
                .build();
    }

    private AWSCredentialsProvider getCredentils() throws BadConfigurationException {
        logger.debug("Getting credentials ...");
        if (!Objects.isNull(accessKey)&&!Objects.isNull(secretKey)){
           return new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(accessKey, secretKey));
        }else{
            if(Objects.isNull(profile) || profile.isEmpty())
                throw new BadConfigurationException("\nAWS credential profile not set.\n "
                        + "You can add them via <accessKey>, <secretKey> in configuration or put them in ~/.aws/credentials and set a proper <profile> in configuration");
            return new ProfileCredentialsProvider(profile);
        }
    }

    private void updateAlias(String version) {
        if (Objects.isNull(alias) || alias.isEmpty())
            return;
        UpdateAliasResult result = lambda.updateAlias(new UpdateAliasRequest()
                .withName(alias)
                .withFunctionName(lambdaName)
                .withFunctionVersion(version)
                .withDescription(getDescription()));
        logger.info(result.toString());

    }

    private String getDescription(){
        String timestamp = Instant.now().toString();
        return description == null ? timestamp : description + " " + timestamp;
    }

    private UpdateFunctionCodeRequest createUpdateRequest(String revisionId) throws IOException {
        return new UpdateFunctionCodeRequest()
                .withFunctionName(lambdaName)
                .withRevisionId(revisionId)
                .withZipFile(getByteBuffer())
                .withPublish(true);
    }

    private UpdateFunctionConfigurationRequest createUpdateConfiguration(){
        logger.info("Preparing configuration ...");
        return new UpdateFunctionConfigurationRequest()
                .withFunctionName(lambdaName)
                .withEnvironment(new Environment().withVariables(variables))
                .withDescription(description);
    }


    private ByteBuffer getByteBuffer() throws IOException {
        try(FileInputStream fis = new FileInputStream(path)){
            return ByteBuffer.wrap(IOUtils.toByteArray(fis));
        }
    }
}
