package com.polaris.resteasy;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pagehelper.util.StringUtil;
import com.polaris.core.Constant;
import com.polaris.core.config.ConfClient;
import com.polaris.core.dto.ResultDto;

@Provider  
public class RestExceptionHandler implements ExceptionMapper<Exception>{  
    
	private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);
	
    @Override  
    public Response toResponse(Exception ex) {
    	ResultDto responseDto = new ResultDto();
    	responseDto.setStatus(String.valueOf(Constant.STATUS_FAILED));
    	if (ex instanceof RuntimeException) {
        	responseDto.setMsgContent(Constant.MESSAGE_GLOBAL_ERROR);
    	} else {
    		
        	String errorCode = ex.getMessage();
        	if (StringUtil.isNotEmpty(errorCode)) {
        		if (errorCode.contains(":")) {
        			errorCode = errorCode.substring(errorCode.indexOf(":") + 1);
        		} 
        		if(errorCode.contains("\r\n")) {
        			errorCode = errorCode.substring(0,errorCode.indexOf("\r\n"));
        		}
        		if (errorCode.contains("\n")) {
        			errorCode = errorCode.substring(0,errorCode.indexOf("\n"));
        		}
        		errorCode = errorCode.trim();
            	responseDto.setMsgContent(ConfClient.get(errorCode,errorCode));
        	}
    	}
    	Response response = Response.status(200).entity(responseDto.toJSON()).build();
    	response.getHeaders().remove("Content-Type");
    	response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON);
    	logger.error(ex.getMessage());
        return response;  
    }
}  

