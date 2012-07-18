/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.spiderAjax;

import org.apache.log4j.Logger;
import org.openqa.selenium.chrome.ChromeDriver;
import org.parosproxy.paros.core.proxy.ProxyServer;
import com.crawljax.browser.EmbeddedBrowser.BrowserType;

public class ProxyAjax {
	
	private ProxyServer proxy = null;
	private boolean megaScan = false;
	private BrowserType browser = null;
	// default config for the new ajax proxy
	private BrowserType defaultBrowser = BrowserType.firefox;
	private AjaxProxyParam proxyParam = null;
	private ExtensionAjax extension;
	private static final Logger logger = Logger.getLogger(ProxyAjax.class);

	
	/**
	 * constructor
	 * @param e the extension
	 */
	public ProxyAjax(ExtensionAjax e) {
		extension = e;
		this.getProxy();
		this.proxy.setProxyParam(getAjaxProxyParam());
		if(!this.proxy.isAnyProxyThreadRunning()){
			this.proxy.startServer(this.getAjaxProxyParam().getProxyIp(), this.getAjaxProxyParam().getProxyPort(), false);
		}
		this.browser = defaultBrowser;
	}

	
	/**
	 * Sets the crawljax to megascan mode that is slower but more accurate
	 * @param b
	 */
	public void setMegaScan(boolean b) {
		this.megaScan = b;
	}


	/**
	 * @return true if crawljax is set to 'megascan' mode, false otherwise
	 */
	public boolean getMegaScan() {
		return this.megaScan;
	}

	
	/**
	 * stops the sever, updates the config and starts it again
	 */
	public void updateProxyConf() {
		if(this.proxy.isAnyProxyThreadRunning()){
			this.proxy.stopServer();
		}
		this.proxy.startServer(this.getProxyHost(), this.getProxyPort(), false);
		if(this.extension.getExcludeList()!=null){
			this.proxy.setExcludeList(this.extension.getExcludeList());
		}
	}


	/**
	 * stops the new ajax proxy
	 */
	public void stopServer() {
		this.getProxy().stopServer();
	}

	
	/**
	 * @return the new ajax proxy object
	 */
	public ProxyServer getProxy() {
		if (this.proxy == null) {
			this.proxy = new ProxyServer();
		}
		return proxy;
	}


	/**
	 * sets the new config for the proxy
	 * 
	 * @return the parameter for the proxy with the new config
	 */
	public BrowserType getBrowser() {
		return this.browser;
	}

	
	/**
	 * Sets the type of browser to be used
	 * @param b browser
	 */
	public void setBrowser(BrowserType b) {
		this.browser = b;
	}

	
	/**
	 * @return the current port used by the new ajax proxy
	 */
	public int getProxyPort() {
		return this.getAjaxProxyParam().getProxyPort();
	}

	
	/**
	 * @return the current host used by the new ajax proxy
	 */
	public String getProxyHost() {
		return this.getAjaxProxyParam().getProxyIp();
	}

	
	/**
	 * 
	 * @param host
	 */
	public void setProxyHost(String ip) {
		this.getAjaxProxyParam().setProxyIp(ip);
	}

	
	/**
	 *  Sets the port of the proxy
	 * @param port
	 */
	public void setProxyPort(int port) {
		this.getAjaxProxyParam().setProxyPort(port);
	}

	
	/**
	 * 
	 * @return the proxyparameter
	 */
	public AjaxProxyParam getAjaxProxyParam() {
		if (proxyParam == null) {
			proxyParam = new AjaxProxyParam();
		}
		return proxyParam;
	}


	/**
	 * This method checks if the chromedriver is available
	 * @return true if available, false otherwise.
	 */
	public boolean isChromeAvail(){
		try{
			new ChromeDriver().close();
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
		return true;
	}
}
