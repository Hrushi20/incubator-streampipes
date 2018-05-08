/*
 * Copyright 2018 FZI Forschungszentrum Informatik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.streampipes.wrapper.flink;

import java.io.Serializable;

public class FlinkDeploymentConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jarFile;
	private String host;
	private int port;
	
	public FlinkDeploymentConfig(String jarFile, String host, int port) {
		super();
		this.jarFile = jarFile;
		this.host = host;
		this.port = port;
	}
	
	public String getJarFile() {
		return jarFile;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}

}
