package org.wso2.sample;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

import java.io.IOException;

/**
 * A class mediator to remove part of JSON payload using a JsonPath expression.
 */
public class TruncateJSON extends AbstractMediator {

    private static Log logger = LogFactory.getLog(TruncateJSON.class.getName());

    public String getJsonPathString() {

        return jsonPathString;
    }

    public void setJsonPathString(String jsonPathString) {

        this.jsonPathString = jsonPathString;
    }

    private String jsonPathString;

    public boolean mediate(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        if (JsonUtil.hasAJsonPayload(axis2MessageContext)) {
            try {
                String jsonString = IOUtils.toString(JsonUtil.getJsonPayload(axis2MessageContext));
                if (!StringUtils.isEmpty(jsonString)) {
                    DocumentContext doc = JsonPath.parse(jsonString);
                    String jsonPath = getJsonPathString();
                    if (jsonPath.startsWith("json-eval(")) {
                        jsonPath = jsonPath.substring(10, jsonPath.length() - 1);
                    }
                    try {
                        doc.delete(jsonPath);
                        JsonUtil.getNewJsonPayload(axis2MessageContext, doc.jsonString(), true, true);
                        return true;
                    } catch (PathNotFoundException ex) {
                        logger.error("Error occurred while reading the JSON payload ", ex);
                    }
                    // setting the payload as it is when an error occurs.
                    JsonUtil.getNewJsonPayload(axis2MessageContext, jsonString, true, true);
                }
            } catch (IOException ex) {
                logger.error("Error occurred while reading the JSON payload ", ex);
            }
        }
        return true;
    }
}
