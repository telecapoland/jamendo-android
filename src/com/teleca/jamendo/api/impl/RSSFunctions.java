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

package com.teleca.jamendo.api.impl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.teleca.jamendo.api.util.XMLUtil;

/**
 * @author Lukasz Wisniewski
 */
public class RSSFunctions {
	
	public static int[] getTracksIdFromRss(String rssString){
		if(rssString == null)
			return null;
		
		int[] tracks_id = null;
		
		Document responseXML = XMLUtil.stringToDocument(rssString);
		
		// node list with items
		NodeList items = responseXML.getElementsByTagName("item");
		
		// track count
		int n = items.getLength();
		
		// initialize an array
		tracks_id = new int[n];

		// iterate over items
		for(int i = 0; i < n; i++){
			Node item_node = items.item(i);
			
			if( item_node.getNodeType() == Node.ELEMENT_NODE ){
				Element item_element = (Element)item_node;
				
				// getting to link node in order to extract track id's
				Node link_node = item_element.getElementsByTagName("link").item(0);
				
				// link with track id
				String link = link_node.getFirstChild().getNodeValue();
				
				// parsing
				String trackidStr = link.replaceAll("http://www.jamendo.com/track/", "");
				tracks_id[i] = Integer.parseInt(trackidStr);
			}
		}
		
		return tracks_id;
	}
	

	
}
