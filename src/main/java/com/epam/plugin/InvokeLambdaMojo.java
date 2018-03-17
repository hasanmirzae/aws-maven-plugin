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

import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 *  Goal to invoke a AWS lambda.
 */
@Mojo( name = "invokeLambda", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST )
public class InvokeLambdaMojo extends AbstractLambdaMojo
{
    private static final Integer STATUS_SUCCESS = 200;
    /**
     * Version or Alias.
     */
    @Parameter( property = "qualifier", required = false)
    private String qualifier;
    /**
     * Function payload (json file).
     */
    @Parameter( property = "payloaJson", required = false)
    private File payloadJson;


    public void execute() throws MojoExecutionException {
        logger.info(String.format("Invoking %s ...",lambdaName));
        InvokeResult result = AWSLambdaClientBuilder.standard()
                .withRegion(region)
                .withCredentials(getCredentials())
                .build().invoke(createRequest());
        handleResult(result);
    }

    private void handleResult(InvokeResult result) throws MojoExecutionException {
        logger.info("Invocation result: "+result.toString());
        if (!STATUS_SUCCESS.equals(result.getStatusCode())){
            throw new MojoExecutionException("FunctionError: " + result.getFunctionError());
        }
    }

    private InvokeRequest createRequest() throws MojoExecutionException {
        InvokeRequest req = new InvokeRequest().withFunctionName(lambdaName);

        if (payloadJson != null)
            req.setPayload(getByteBuffer(payloadJson));

        if (qualifier != null && !qualifier.isEmpty())
            req.setQualifier(qualifier);

        return req;
    }


}
