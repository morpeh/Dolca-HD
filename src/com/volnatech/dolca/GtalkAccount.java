/*Copyright (C) 2011 by Alexander Voloshyn

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.volnatech.dolca;

import java.io.IOException;

public class GtalkAccount extends XMPPAccount {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	GtalkAccount(){
		this.setServerAddress("talk.google.com");
		this.setServerPort(5222);
		this.setService("gmail.com");
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		this.setServerAddress("talk.google.com");
		this.setServerPort(5222);
		this.setService("gmail.com");
	}
	
	@Override
	public void setUserName(String userName) {
		if (userName.contains("@"))
			userName = userName.split("@")[0];
		super.setUserName(userName);
	}
}
