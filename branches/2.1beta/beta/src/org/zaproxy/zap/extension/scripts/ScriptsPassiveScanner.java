/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.scripts;

import java.io.StringWriter;
import java.util.List;

import javax.script.Invocable;

import net.htmlparser.jericho.Source;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.PassiveScanThread;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;

public class ScriptsPassiveScanner extends PluginPassiveScanner {
	
	private ExtensionScripts extension = null;
	private PassiveScanThread parent = null;
	private String name = null;
	
	// private Logger logger = Logger.getLogger(ScriptsPassiveScanner.class);

	public ScriptsPassiveScanner() {
	}
	
	@Override
	public String getName() {
		if (name == null) {
			// Cache to prevent an NPE when unloaded
			name = Constant.messages.getString("scripts.passivescanner.title");
		}
		return name;
	}

	private ExtensionScripts getExtension() {
		if (extension == null) {
			extension = (ExtensionScripts) Control.getSingleton().getExtensionLoader().getExtension(ExtensionScripts.NAME);
		}
		return extension;
	}
	
	private int getId () {
		return 50001;
	}
	
	@Override
	public void scanHttpRequestSend(HttpMessage msg, int id) {
		// Ignore
	}

	@Override
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
		if (this.getExtension() != null) {
			List<ScriptWrapper> scripts = extension.getScripts(ScriptWrapper.Type.PASSIVE);
			for (ScriptWrapper script : scripts) {
				StringWriter writer = new StringWriter();
				try {
					if (script.isEnabled()) {
						Invocable inv = extension.invokeScript(script, writer);
						
						ScriptPScan s = inv.getInterface(ScriptPScan.class);
						
						if (s != null) {
							s.scan(this, msg, source);
							
						} else {
							writer.append(Constant.messages.getString("scripts.interface.active.error"));
							extension.setError(script, writer.toString());
							extension.setEnabled(script, false);
						}
					}
					
				} catch (Exception e) {
					writer.append(e.toString());
					extension.setError(script, e);
					extension.setEnabled(script, false);
				}
			}
		}
		
	}
	
	public void raiseAlert(int risk, int reliability, String name, String description, String uri, 
			String param, String attack, String otherInfo, String solution, HttpMessage msg) {
		
		Alert alert = new Alert(getId(), risk, reliability, name);
		     
		alert.setDetail(description, msg.getRequestHeader().getURI().toString(), 
				param, attack, otherInfo, solution, null, msg);		// Left out reference to match ScriptsActiveScanner

		this.parent.raiseAlert(this.getId(), alert);
	}


	@Override
	public void setParent(PassiveScanThread parent) {
		this.parent = parent;
	}

}