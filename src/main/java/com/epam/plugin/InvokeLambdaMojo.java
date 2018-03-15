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
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.epam.plugin.exceptions.BadConfigurationException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.Objects;

/**
 *  Maven plugin to invoke a AWS lambda.
 */
@Mojo( name = "invokeLambda", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST )
public class InvokeLambdaMojo extends AbstractMojo
{
    private final Log logger = getLog();
    /**
     * Function name.
     */
    @Parameter( property = "lambdaName", required = true )
    private String lambdaName;

    /**
     * Version or Alias.
     */
    @Parameter( property = "qualifier", required = false)
    private String qualifier;

    /**
     * Region.
     */
    @Parameter( property = "region", required = true)
    private String region;

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
    /**
     * Function payload (json file).
     */
    @Parameter( property = "profile", required = false)
    private File payload;



    public void execute() throws MojoExecutionException{
        try {

            AWSLambdaClientBuilder.standard()
                                  .withRegion(region)
                                  .withCredentials(getCredentils())
                                  .build().invoke(createRequest());
        }catch (Exception e){
            logger.error(e);
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private InvokeRequest createRequest(){
        InvokeRequest req = new InvokeRequest();
        if (payload != null && !payload.isEmpty())
            req.setPayload(getPayload());

        if (qualifier != null && !qualifier.isEmpty())
            req.setQualifier(qualifier);

        return req;
    }

    private String getPayload() {
        return "";
    }

    private AWSCredentialsProvider getCredentils() throws BadConfigurationException {
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



}