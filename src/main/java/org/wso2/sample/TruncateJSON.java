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
                    String result = jsonString;
                    String[] jsonPathArray = getJsonPathString().split(",");
                    if (jsonPathArray.length > 0) {
                        for (String jsonPath : jsonPathArray) {
                            if (jsonPath.startsWith("json-eval(")) {
                                jsonPath = jsonPath.substring(10, jsonPath.length() - 1);
                            }
                            try {
                                DocumentContext doc = JsonPath.parse(result);
                                doc.delete(jsonPath);
                                result = doc.jsonString();
                            } catch (PathNotFoundException ex) {
                                logger.error("Error occurred while reading the JSON payload ", ex);
                            }
                        }
                        JsonUtil.getNewJsonPayload(axis2MessageContext, result, true, true);
                        return true;
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
