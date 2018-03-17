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
import com.amazonaws.util.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 *  Abstract goal to be implemented.
 */
public abstract class AbstractLambdaMojo extends AbstractMojo
{
    protected final Log logger = getLog();
    /**
     * Function name.
     */
    @Parameter( property = "lambdaName", required = true )
    protected String lambdaName;
    /**
     * Region.
     */
    @Parameter( property = "region", required = true)
    protected String region;
    /**
     * Aws client key.
     */
    @Parameter( property = "accessKey", required = false)
    protected String accessKey;
    /**
     * AWS client secret.
     */
    @Parameter( property = "secretKey", required = false)
    protected String secretKey;
    /**
     * AWS credential profile.
     */
    @Parameter( property = "credentialProfile", required = false)
    protected String credentialProfile;



    protected AWSCredentialsProvider getCredentials() throws MojoExecutionException {
        if (!Objects.isNull(accessKey)&&!Objects.isNull(secretKey)){
           return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
        }else{
            if(Objects.isNull(credentialProfile) || credentialProfile.isEmpty())
                throw new MojoExecutionException("\nAWS credential profile not set.\n "
                        + "You can add them via <accessKey>, <secretKey> in configuration or put them in ~/.aws/credentials and set a proper <profile> in configuration");
            return new ProfileCredentialsProvider(credentialProfile);
        }
    }

    protected ByteBuffer getByteBuffer(File path) throws MojoExecutionException {
        try(FileInputStream fis = new FileInputStream(path)){
            return ByteBuffer.wrap(IOUtils.toByteArray(fis));
        }catch (Exception e){
            throw new MojoExecutionException(e.getMessage());
        }
    }
}
