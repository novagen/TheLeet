/*
 * ItemRef.java
 *
 *************************************************************************
 * Copyright 2010 Christofer Engel
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
 */
package com.rubika.aotalk.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ItemRef {
	protected static final String APP_TAG = "--> AOTalk::ItemRef";
	private String retval = "";
	
	public List<Object> getData(String lowId, String highId, String ql) {
        List<Object> result = new ArrayList<Object>();

        String xml = null;
        Document doc = null;
        
        String icon = "";
        String name = "";
        String description = "";
        String flaglist = "";
        String flags = "";
        String requirements = "";
        String can = "";
        String events = "";
        String attributes = "";
        String attacks = "";
        String defenses = "";
        String level = "";
        
        int lowQL = 0;
        int highQL = 0;

        String xyphosUrl = String.format("http://itemxml.xyphos.com/?id=%s&ql=%s", lowId, ql);
        Logging.log(APP_TAG, xyphosUrl);
        
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(xyphosUrl);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            xml = EntityUtils.toString(httpEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (xml != null) {
        	if (xml.contains("<item>")) {
        		xml = xml.substring(xml.indexOf("<item>"));
        	}
        	
        	Pattern pattern = Pattern.compile("<description>(.*?)</description>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(xml);

            while(matcher.find()) {
	        	xml = xml.replace(matcher.group(1), matcher.group(1).replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
            }
            
        	pattern = Pattern.compile(" name=\"(.*?)\" />", Pattern.DOTALL);
            matcher = pattern.matcher(xml);

            while(matcher.find()) {
	        	xml = xml.replace(matcher.group(1), matcher.group(1).replaceAll("<b>", "").replaceAll("</b>", ""));
            }
            
            Logging.log(APP_TAG, xml);
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            
            try {
                DocumentBuilder db = dbf.newDocumentBuilder();
     
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xml));
                doc = db.parse(is);
            } catch (ParserConfigurationException e) {
            	Logging.log(APP_TAG, e.getMessage());
                return null;
            } catch (SAXException e) {
            	Logging.log(APP_TAG, e.getMessage());
                return null;
            } catch (IOException e) {
            	Logging.log(APP_TAG, e.getMessage());
                return null;
            }
        
	        if (doc != null) {
	        	NodeList nl = doc.getElementsByTagName("item");
	            
	            for (int i = 0; i < nl.getLength(); i++) {
	                Element e = (Element) nl.item(i);
	                
	                name = getValue(e, "name");
	                description = getValue(e, "description").replaceAll("\n", "<br />");
	            }
	            
	        	nl = doc.getElementsByTagName("low");
	            for (int x = 0; x < nl.getLength(); x++) {
	                Element item = (Element) nl.item(x);
	                lowQL = Integer.parseInt(item.getAttribute("ql"));
	            }	            
	            
	        	nl = doc.getElementsByTagName("high");
	            for (int x = 0; x < nl.getLength(); x++) {
	                Element item = (Element) nl.item(x);
	                highQL = Integer.parseInt(item.getAttribute("ql"));
	            }	            
	            
	        	nl = doc.getElementsByTagName("attribute");
	            
	            int equipmentPageID = -1;
	            for (int x = 0; x < nl.getLength(); x++) {
	                Element item = (Element) nl.item(x);
	                int value = Integer.parseInt(item.getAttribute("value"));
	                
	                if (item != null) {
	                	if (item.getAttribute("stat") != null) {
	                		if (item.getAttribute("stat").equals("79")) {
			                	icon = item.getAttribute("value");
	                		} else if (item.getAttribute("stat").equals("76")) {
	                			equipmentPageID = value;
	                		} else if (item.getAttribute("stat").equals("298")) {
		                		String slots = ItemValues.getSlot(value, equipmentPageID);
		                		
	                			if (slots.length() > 0) {
	                				attributes += item.getAttribute("name") + " <b>" + slots + "</b><br />";
	                			}
			                } else if (item.getAttribute("stat").equals("0")) {
			                	flaglist = item.getAttribute("extra") + "<br />";
			                } else if (item.getAttribute("stat").equals("30")) {
			                	can = item.getAttribute("extra");
			                } else if (item.getAttribute("stat").equals("54")) {
			                	level = String.valueOf(value);
			                } else {
			                	if (!item.getAttribute("stat").equals("88")
					                && !item.getAttribute("stat").equals("2")
					                && !item.getAttribute("stat").equals("209")
					                && !item.getAttribute("stat").equals("353")
				                	&& !item.getAttribute("stat").equals("12")
			                		&& !item.getAttribute("stat").equals("74")
			                		&& !item.getAttribute("stat").equals("272")
			                		&& !item.getAttribute("stat").equals("270")
			                		&& !item.getAttribute("stat").equals("269")
			                		&& !item.getAttribute("stat").equals("428")
			                		&& !item.getAttribute("stat").equals("419")
			                	) {
				                	if (item.getAttribute("extra").equals("")) {
				                		attributes += item.getAttribute("name") + " " + item.getAttribute("value") + "<br />";
				                	} else {
				                		attributes += item.getAttribute("name") + " " + item.getAttribute("extra") + "<br />";
				                	}
			                	}
			                }
	                	}
	                }
	            }
	            
	        	nl = doc.getElementsByTagName("attack");
	        	
	            for (int i = 0; i < nl.getLength(); i++) {
	                Element item = (Element) nl.item(i);
	                attacks += item.getAttribute("name") + " " + item.getAttribute("percent") + "%<br />";
	            }
	            
	        	nl = doc.getElementsByTagName("defense");
	        	
	            for (int i = 0; i < nl.getLength(); i++) {
	                Element item = (Element) nl.item(i);
	                defenses += item.getAttribute("name") + " " + item.getAttribute("percent") + "%<br />";
	            }
	
	        	nl = doc.getElementsByTagName("action");
	        	
	            for (int i = 0; i < nl.getLength(); i++) {
	                Element item = (Element) nl.item(i);
		        	NodeList nreq = item.getElementsByTagName("requirement");
		            
		        	requirements += "<br /><b><i>" + item.getAttribute("name") + "</i></b><br />";
		        	
		            for (int p = 0; p < nreq.getLength(); p++) {
		                Element ritem = (Element) nreq.item(p);
		                NodeList reqList = ritem.getChildNodes();
		                
		                String req = "%s %s %s %s %s<br />";
		                String target = "";
		                String stat = "";
		                String op = "";
		                String value = "";
		                String subop = "";
		               
		                for (int y = 0; y < reqList.getLength(); y++) {
		                    Node reqItem = reqList.item(y);
		                    
		                    if (!(reqItem instanceof Text)) {
		                        
		                        Element e = (Element) reqItem;
		                        
		                        if (e.getNodeName().equals("target")) {
		    		                target = e.getAttribute("name");
		                    	}
		                        
		                    	if (e.getNodeName().equals("stat")) {
		    		                stat = e.getAttribute("name");
		                    	}
		                    	
		                    	if (e.getNodeName().equals("op")) {
		    		                op = e.getAttribute("name");
		                    	}
		                    	
		                    	if (e.getNodeName().equals("value")) {
		    		                value = e.getAttribute("num");
		    		                
		                    		if (stat.equals("WornItem")) {
		                    			value = ItemValues.getWornItem(Integer.parseInt(value));
		                    		}
		
		                    		if (stat.equals("Expansion")) {
		                    			value = ItemValues.getExpansion(Integer.parseInt(value));
		                    		}
		                    		
		                    		if (stat.equals("Breed")) {
		                    			value = ItemValues.getBreed(Integer.parseInt(value));
		                    		}
		                    		
		                    		if (stat.equals("ExpansionPlayfield")) {
		                    			value = ItemValues.getExpansionPlayfield(Integer.parseInt(value));
		                    		}
		                    		
		                    		if (stat.equals("CurrentPlayfield")) {
		                    			value = ItemValues.getCurrentPlayfield(Integer.parseInt(value));
		                    		}
		                    		
		                    		if (stat.equals("Faction")) {
		                    			value = ItemValues.getFaction(Integer.parseInt(value));
		                    		}
		                    		
		                    		if (stat.equals("Flags")) {
		                    			if(op.equals("HasPerk")) {
		                        			value = ItemValues.getPerk(Integer.parseInt(value));
		                    			}
		                    			if(op.equals("HasNotWornItem")) {
		                        			value = ItemValues.getHasNotWornItem(Integer.parseInt(value));
		                    			}
		                    		}
		                    		
		                    		if (stat.equals("Profession") || stat.equals("VisualProfession")) {
		                    			value = ItemValues.getProfession(Integer.parseInt(value));
		                    		}
		                    	}
		                    	
		                    	if (e.getNodeName().equals("subop")) {
		    		                subop = e.getAttribute("name");
		                    	}
		                    }
		                }
		                
		                requirements += String.format(req, target, stat, op, value, subop);
		            }
	        	}
	            
	        	nl = doc.getElementsByTagName("event");
	            
	            for (int x = 0; x < nl.getLength(); x++) {
	                Element item = (Element) nl.item(x);
	                
	                if (item != null) {
	            		events += "<br /><b><i>" + item.getAttribute("name") + "</i></b><br />";
	            		
	            		NodeList nwear = item.getElementsByTagName("function");
	                	String funcName = "";
	                	
	                    for (int y = 0; y < nwear.getLength(); y++) {
	                        Element wear = (Element) nwear.item(y);
	                        NodeList ntarget = wear.getElementsByTagName("target");
	                        NodeList nfunc = wear.getElementsByTagName("func");
	                        NodeList nparam = wear.getElementsByTagName("parameters");
	                		
	                        for (int z = 0; z < ntarget.getLength(); z++) {
		                        Element target = (Element) ntarget.item(z);
		                        events += target.getAttribute("name") + " ";
	                        }
	                        
	                        for (int z = 0; z < nfunc.getLength(); z++) {
		                        Element func = (Element) nfunc.item(z);
		                        events += func.getAttribute("name") + " ";
		                        funcName = func.getAttribute("name");
	                        }
	                        
	                        for (int z = 0; z < nparam.getLength(); z++) {
	                        	Element params = (Element) nparam.item(z);
	                        	NodeList nparams = params.getElementsByTagName("param");
	                        	
	                            for (int i = 0; i < nparams.getLength(); i++) {
	                            	Element param = (Element) nparams.item(i);
	
	    	                        if (nparams.getLength() > 1) {
	    		                        if (i == 0) {
	    		                        	if (funcName.equals("Modify") || funcName.equals("LockSkill") || funcName.equals("Skill") || funcName.equals("Hit") || funcName.equals("ChangeVariable") || funcName.equals("TimedEffect")) {
	    		                        		events += ItemValues.getSkill(Integer.parseInt(param.getFirstChild().getNodeValue()));
	    		                        	} else if (funcName.equals("CastStunNano") || funcName.equals("AreaCastNano")) {
	    		                        		events += ItemValues.lookupItemName(Integer.parseInt(param.getFirstChild().getNodeValue()));
	        		                        } else if (funcName.equals("ResistNanoStrain")) {
	    		                        		events += ItemValues.getNanoStrain(Integer.parseInt(param.getFirstChild().getNodeValue()));
		    	                        	} else if (funcName.equals("SetFlag")) {
	    		                        		events += ItemValues.getFlag(Integer.parseInt(param.getFirstChild().getNodeValue()));
	    		                        	} else {
	        		                        	events += param.getFirstChild().getNodeValue();
	    		                        	}
	    		                        } else if (i == 3) {
	    		                        	if (funcName.equals("Teleport")) {
	    		                        		events += ItemValues.getCurrentPlayfield(Integer.parseInt(param.getFirstChild().getNodeValue()));
	    		                        	} else if (funcName.equals("HasNotFormula")) {
				                        		events += ItemValues.getNano(Integer.parseInt(param.getFirstChild().getNodeValue()));
	    		                        	} else {
	    		                        		events += param.getFirstChild().getNodeValue();
	    		                        	}
	    		                        } else {
	    		                        	if (funcName.equals("AddSkill") || funcName.equals("ChangeVariable") || funcName.equals("Set")) {
	        		                        	int num = Integer.parseInt(param.getFirstChild().getNodeValue());
	    		                        		events += ItemValues.getSkill(num);
	    		                        	} else {
	    		                        		events += param.getFirstChild().getNodeValue();
	    		                        	}
	    		                        }
	    	                        } else {
	    	                        	if (funcName.equals("UploadNano") || funcName.equals("CastNano") || funcName.equals("TeamCastNano")) {
			                        		events += ItemValues.lookupItemName(Integer.parseInt(param.getFirstChild().getNodeValue()));
	    	                        	} else if (funcName.equals("RemoveNanoStrain")) {
    		                        		events += ItemValues.getNanoStrain(Integer.parseInt(param.getFirstChild().getNodeValue()));
			                        	} else {
			                        		events += param.getFirstChild().getNodeValue();
			                        	}
	    	                        }
	    	                        
	    	                        events += " ";
	                            }
	                        }
	                        
	                        events += "<br />";
	                    }
	                }
	            }
	        }
	        
	    	if(flaglist.contains("NoDrop")) {
	    		flags += "NODROP";
	    	}
	    	
	    	if(flaglist.contains("Unique")) {
	    		if (flags.length() > 0) {
	    			flags += ", ";
	    		}
	    		flags += "UNIQUE";
	    	}
	    	
	    	if(!flags.equals("")) {
	    		flags = "<br /><font color=#999999>" + flags + "</font>";
	    	}
	        
	    	retval += 
	    		"<img src=\"" +  Statics.ICON_PATH  + icon + "\" class=\"item\">"
	    		+ "<b>" + name + "</b>"
	    		+ flags
	    		+ "<br /><br />"
	    		+ "<b>Quality level:</b> " + level
	    		+ "<br /><br />"
	    		+ "<b>Description</b><br />" + description;
	    	
	    	if (can.length() > 0) {
	    		retval += "<br /><br /><b>Can</b><br />" + can;
	    	}
	    	
	    	if (flaglist.length() > 0) {
	    		retval += "<br /><br /><b>Flags</b><br />" + flaglist;
	    	}
	    	
	    	if (attributes.length() > 0) {
	        	retval += "<br /><b>Attributes</b><br />";
	        	retval += attributes;
	    	}
	    	
	    	if (attacks.length() > 0) {
	        	retval += "<br /><b>Attacks</b><br />";
	        	retval += attacks;
	    	}
	    	
	    	if (defenses.length() > 0) {
	        	retval += "<br /><b>Defenses</b><br />";
	        	retval += defenses;
	    	}
	   	
	    	if (requirements.length() > 0) {
	    		requirements = requirements
	    			.replace("EqualTo", "=")
	    			.replace("NotBitAnd", "!=")
	    			.replace("BitAnd", "=")
	    			.replace("Unequal", "!=")
	    			.replace("LessThan", "<")
	    			.replace("GreaterThan", ">");
	    		retval += "<br /><b>Reqirements</b><br />" + requirements;
	    	}
	    	
	    	if (events.length() > 0) {
		    	retval += "<br /><b>Events</b><br />";
		    	retval += events;
	    	}
	    	
	    	/*
	    	retval += "<br /><br />";
	    	retval += "<font color=#999999>Guides from AO-Universe</font>";
	    	retval += "<br />";
	    	retval += "<a href=\"gitem://" + name + "\">Check for guides</a>";
	    	retval += "<br /><br />";
	    	retval += "<font color=#999999>Recipes from AO RecipeBook</font>";
	    	retval += "<br />";
	    	retval += "<a href=\"aorbid://" + lowId + "\">Check for recipes</a>";
	        */
	    	retval += "<br /><br />";
	    	retval += "<font color=#999999>Data from Xyphos.org</font>";
	    	retval += "<br />";
	    	retval += "<a href=\"chatcmd:///start http://www.xyphos.com/ao/aodb.php?id=" + lowId + "&ql=" + ql + "\">Show on web page</a>";
	    	
	        if(retval.startsWith("<br />")) {
	        	retval = retval.replaceFirst("<br />", "");
	        }
        }
        
        result.add(retval);
        result.add(lowQL);
        result.add(highQL);
        result.add(Integer.parseInt(level));
        result.add(name);
        
		return result;
	}
	
    private String getValue(Element item, String str) {
    	NodeList n = item.getElementsByTagName(str);
    	return getElementValue(n.item(0));
    }

    private final String getElementValue( Node elem ) {
    	Node child;

    	if (elem != null) {
    		if (elem.hasChildNodes()) {
    			for (child = elem.getFirstChild(); child != null; child = child.getNextSibling()) {
    				if (child.getNodeType() == Node.TEXT_NODE) {
    					return child.getNodeValue();
    				}
    			}
    		}
    	}

    	return "";
    }
}
