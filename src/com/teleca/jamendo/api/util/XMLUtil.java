/*
 * Copyright (C) 2009 Teleca Poland Sp. z o.o. <android@teleca.com>
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

package com.teleca.jamendo.api.util;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Lukasz Wisniewski
 */
public class XMLUtil {

	/**
	 * singleton
	 */
	private static DocumentBuilderFactory documentBuilderFactory = null;

	/**
	 * DocumentBuilderFactory instance (lazy initialization)
	 * 
	 * @return
	 */
	private static DocumentBuilderFactory getDocumentBuilderFactory(){
		if(documentBuilderFactory == null){
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
		}
		return documentBuilderFactory;
	}

	/**
	 * DocumentBuilder instance
	 * 
	 * @return
	 */
	private static DocumentBuilder getDocumentBuilder(){
		try {
			return getDocumentBuilderFactory().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			return null;
		}
	}

	/**
	 * Converts String containing XML code to Document
	 * 
	 * @param xmlString
	 * @return <code>Document</code> interface
	 */
	public static Document stringToDocument(String xmlString){
		if(xmlString == null)
			return null;
		
		DocumentBuilder documentBuilder = getDocumentBuilder();
		InputSource inputSource = new InputSource(new StringReader(xmlString));
		try {
			return documentBuilder.parse(inputSource);
		} catch (SAXException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
