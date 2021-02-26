/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.streampipes.model.pipeline;

public class PipelineElementStatus {

	private String elementId;
	private String elementName;
	private String optionalMessage;
	private String operation;
	private String elementNode;
	
	private boolean success;

	public PipelineElementStatus() {
	}

	public PipelineElementStatus(String elementId, String elementName, boolean success, String optionalMessage)
	{
		this.elementId = elementId;
		this.elementName = elementName;
		this.optionalMessage = optionalMessage;
		this.success = success;
	}

	public String getElementId() {
		return elementId;
	}

	public String getOptionalMessage() {
		return optionalMessage;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getElementName() {
		return elementName;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	public void setOptionalMessage(String optionalMessage) {
		this.optionalMessage = optionalMessage;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getElementNode() {
		return elementNode;
	}

	public void setElementNode(String elementNode) {
		this.elementNode = elementNode;
	}
}
