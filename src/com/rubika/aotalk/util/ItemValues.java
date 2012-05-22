package com.rubika.aotalk.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

public class ItemValues {
	private static Map<Integer, String> expansionList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 1L;
		{
			put(1, "Notum Wars");
			put(2, "Shadowlands");
			put(8, "Alien Invasion");
			put(32, "Lost Eden");
			put(128, "Legacy of Xan");
		}
	};
	
	private static Map<Integer, String> factionList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 2L;
		{
			put(0, "Neutral");
			put(1, "Clan");
			put(2, "Omni");
		}
	};
	
	private static Map<Integer, String> breedList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 3L;
		{
			put(1, "Solitus");
			put(2, "Opifex");
			put(3, "Nanomage");
			put(4, "Atrox");
		}
	};
	
	private static Map<Integer, String> expansionPlayfieldList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 4L;
		{
			put(0, "Rubi-Ka");
			put(1, "ShadowLands");
		}
	};
	
	private static Map<Integer, String> nanoStrainList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 5L;
		{
			put(8, "DOT_Nanotechnician");
			put(10, "DOT_NanotechnicianTypeB");
			put(13, "Blindness");
			put(135, "Trader_SkillTransferTargetDebuff_Deprive");
			put(136, "Trader_SkillTransferTargetDebuff_Ransack");
			put(145, "Snare");
			put(146, "Root");
			put(147, "MezzStun");
			put(223, "Adventurer_Polymorphs");
			put(239, "NanoShutdownDebuff");
			put(282, "CompleteHealingLine");
			put(679, "Freak Strength Self Stun");
			put(703, "Path of Darkness Debuff");
			put(705, "Road To Darkness Debuff");
			put(707, "The Choice Debuff (Omni)");
			put(710, "Slam of Darkness Debuff");
			put(833, "Restrict Movement1*");
			put(834, "Restrict Movement2*");
			put(883, "PvP Fear*");
			put(884, "PvP Knockback*");
			put(902, "Veterans L33t*");
			put(910, "PvP Enabled*");
		}
	};
	
	private static Map<Integer, String> perkList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 6L;
		{
			put(1250, "Alien Technology Expertise 1");
			put(1251, "Alien Technology Expertise 2");
			put(1252, "Alien Technology Expertise 3");
		}
	};
	
	private static Map<Integer, String> wornItemList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 7L;
		{
			put(16, "GridArmor");
			put(64, "Profession_Nanodeck");
		}
	};
	
	private static Map<Integer, String> slotWeaponsList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 8L;
		{
			put(0, "Whoops");
			put(1, "HUD 2");
			put(2, "HUD 3");
			put(3, "Utils 1");
			put(4, "Utils 2");
			put(5, "Utils 3");
			put(6, "Right hand");
			put(7, "Deck");
			put(8, "Left hand");
			put(9, "Deck 1");
			put(10, "Deck 2");
			put(11, "Deck 3");
			put(12, "Deck 4");
			put(13, "Deck 5");
			put(14, "Deck 6");
			put(15, "HUD 1");
		}
	};
	
	private static Map<Integer, String> slotClothingList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 11L;
		{
			put(0, "Neck");
			put(1, "Head");
			put(2, "Back");
			put(3, "Left shoulder");
			put(4, "Chest");
			put(5, "Right shoulder");
			put(6, "Right arm");
			put(7, "Hands");
			put(8, "Left arm");
			put(9, "Right wrist");
			put(10, "Legs");
			put(11, "Left wrist");
			put(12, "Right finger");
			put(13, "Feet");
			put(14, "Left finger");
		}
	};
	
	private static Map<Integer, String> slotImplantsList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 12L;
		{
			put(0, "Eyes");
			put(1, "Head");
			put(2, "Ears");
			put(3, "Right arm");
			put(4, "Chest");
			put(5, "Left arm");
			put(6, "Right wrist");
			put(7, "Waist");
			put(8, "Left wrist");
			put(9, "Right hand");
			put(10, "Legs");
			put(11, "Left hand");
			put(12, "Feet");
		}
	};
	
	private static Map<Integer, String> professionList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 9L;
		{
			put(1, "Soldier");
			put(2, "Martial Artist");
			put(3, "Engineer");
			put(4, "Fixer");
			put(5, "Agent");
			put(6, "Adventurer");
			put(7, "Trader");
			put(8, "Bureaucrat");
			put(9, "Enforcer");
			put(10, "Doctor");
			put(11, "Nano-Technician");
			put(12, "Meta-Physicist");
			put(14, "Keeper");
			put(15, "Shade");
		}
	};
	
	private static Map<Integer, String> skillList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 10L;
		{
			put(1, "MaxHealth");
			put(16, "Strength");
			put(17, "Agility");
			put(18, "Stamina");
			put(19, "Intelligence");
			put(20, "Sense");
			put(21, "Psychic");
			put(45, "BeltSlots");
			put(90, "ProjectileAC");
			put(91, "MeleeAC");
			put(92, "EnergyAC");
			put(93, "ChemicalAC");
			put(94, "RadiationAC");
			put(95, "ColdAC");
			put(96, "PoisonAC");
			put(97, "FireAC");
			put(100, "MartialArts");
			put(101, "MultiMelee");
			put(102, "1hBlunt");
			put(103, "1hEdged");
			put(104, "MeleeEnergy");
			put(105, "2hEdged");
			put(106, "Piercing");
			put(107, "2hBlunt");
			put(108, "SharpObjects");
			put(109, "Grenade");
			put(110, "HeavyWeapons");
			put(111, "Bow");
			put(112, "Pistol");
			put(113, "Rifle");
			put(114, "MG/SMG");
			put(115, "Shotgun");
			put(116, "AssaultRifle");
			put(117, "VehicleWater");
			put(118, "MeleeInit");
			put(119, "RangedInit");
			put(120, "PhysicalInit");
			put(121, "BowSpecialAttack");
			put(122, "SensoryImprovement");
			put(123, "FirstAid");
			put(124, "Treatment");
			put(125, "MechanicalEngineering");
			put(126, "ElectricalEngineering");
			put(127, "MaterialMetamorphose");
			put(128, "BiologicalMetamorphose");
			put(129, "PsychologicalModification");
			put(130, "MaterialCreation");
			put(131, "SpaceTime");
			put(132, "NanoPool");
			put(133, "RangedEnergy");
			put(134, "MultiRanged");
			put(135, "TrapDisarm");
			put(136, "Perception");
			put(137, "Adventuring");
			put(138, "Swimming");
			put(139, "VehicleAir");
			put(140, "MapNavigation");
			put(141, "Tutoring");
			put(142, "Brawl");
			put(143, "Riposte");
			put(144, "Dimach");
			put(145, "Parry");
			put(146, "SneakAttack");
			put(147, "FastAttack");
			put(148, "Burst");
			put(149, "NanoInit");
			put(150, "FlingShot");
			put(151, "AimedShot");
			put(152, "BodyDevelopment");
			put(153, "DuckExplosions");
			put(154, "DodgeRanged");
			put(155, "EvadeClose");
			put(156, "RunSpeed");
			put(157, "QuantumFT");
			put(158, "WeaponSmithing");
			put(159, "Pharmaceuticals");
			put(160, "NanoProgramming");
			put(161, "ComputerLiteracy");
			put(162, "Psychology");
			put(163, "Chemistry");
			put(164, "Concealment");
			put(165, "BreakingEntry");
			put(166, "VehicleGround");
			put(167, "FullAuto");
			put(168, "NanoResist");
			put(181, "MaxNCU");
			put(207, "ReflectEnergyAC");
			put(221, "MaxNanoEnergy");
			put(226, "ShieldProjectileAC");
			put(227, "ShieldMeleeAC");
			put(228, "ShieldEnergyAC");
			put(229, "ShieldChemicalAC");
			put(230, "ShieldRadiationAC");
			put(231, "ShieldColdAC");
			put(233, "ShieldFireAC");
			put(234, "ShieldPoisonAC");
			put(276, "AddAllOffense");
			put(277, "AddAllDefense");
			put(278, "ProjectileDamageModifier");
			put(280, "EnergyDamageModifier");
			put(281, "ChemicalDamageModifier");
			put(282, "RadiationDamageModifier");
			put(279, "MeleeDamageModifier");
			put(311, "ColdDamageModifier");
			put(316, "FireDamageModifier");
			put(317, "PoisonDamageModifier");
			put(318, "NanoCost");
			put(319, "XPModifier");
			put(343, "HealDelta");
			put(360, "Scale");
			put(364, "NanoDelta");
			put(379, "CriticalIncrease");
			put(380, "WeaponRange");
			put(381, "NanoRange");
			put(382, "SkillLockModifier");
			put(383, "NanoInterruptModifier");
			put(391, "CriticalResistance");
			put(477, "MaxReflectedEnergyAC");
			put(535, "HealModifier");
			put(536, "NanoDamage");
		}
	};
	
	public static String getNanoStrain(int id) {
		if (nanoStrainList.containsKey(id)) {
			return nanoStrainList.get(id);
		} else {
			return String.valueOf(id);
		}		
	}
	
	public static String getSkill(int id) {
		if (skillList.containsKey(id)) {
			return skillList.get(id);
		} else {
			return String.valueOf(id);
		}
	}
	
	public static String getExpansion(int id) {
		if (expansionList.containsKey(id)) {
			return expansionList.get(id);
		} else {
			return String.valueOf(id);
		}
	}
	
	public static String padLeft(String s, int n) {
	    return String.format("%1$#" + n + "s", s).replaceAll(" ", "0");  
	}
	
	public static String getSlot(int id, int page) {
		String binary = new StringBuffer(String.format("%16s", Integer.toBinaryString(id)).replace(' ', '0')).reverse().toString();
		String slots = "";
		
		if (page == 2 || page == 3) {
			binary = binary.substring(1);
		}
		
		char[] ids = binary.toCharArray();
		
		Logging.log("ItemValues", "Slots: " + binary + ", on page: " + page);
		int slotCounter = 0;
		
		if (page == 1) {
			for (char i : ids) {
				Logging.log("ItemValues", "Val: " + i + ", counter: " + slotCounter);
				if (MathUtils.stringToBool(Character.toString(i))) {
					if (slots.length() > 0) {
						slots += ", ";
					}
					slots += slotWeaponsList.get(slotCounter);
				}
				slotCounter++;
			}
		} else if (page == 2) {
			for (char i : ids) {
				Logging.log("ItemValues", "Val: " + i + ", counter: " + slotCounter);
				if (MathUtils.stringToBool(Character.toString(i))) {
					if (slots.length() > 0) {
						slots += ", ";
					}
					slots += slotClothingList.get(slotCounter);
				}
				slotCounter++;
			}
		} else if (page == 3) {
			for (char i : ids) {
				Logging.log("ItemValues", "Val: " + i + ", counter: " + slotCounter);
				if (MathUtils.stringToBool(Character.toString(i))) {
					if (slots.length() > 0) {
						slots += ", ";
					}
					slots += slotImplantsList.get(slotCounter);
				}
				slotCounter++;
			}
		}
		
		return slots;
	}
	
	public static String getFaction(int id) {
		if (factionList.containsKey(id)) {
			return factionList.get(id);
		} else {
			return String.valueOf(id);
		}
	}
	
	public static String getBreed(int id) {
		if (breedList.containsKey(id)) {
			return breedList.get(id);
		} else {
			return String.valueOf(id);
		}
	}
	
	public static String getProfession(int id) {
		if (professionList.containsKey(id)) {
			return professionList.get(id);
		} else {
			return String.valueOf(id);
		}
	}
	
	public static String getPerk(int id) {
		if (perkList.containsKey(id)) {
			return perkList.get(id);
		} else {
			return String.valueOf(id);
		}
	}
	
	public static String getExpansionPlayfield(int id) {
		if (expansionPlayfieldList.containsKey(id)) {
			return expansionPlayfieldList.get(id);
		} else {
			return String.valueOf(id);
		}
	}

	public static String getWornItem(int id) {
		if (wornItemList.containsKey(id)) {
			return wornItemList.get(id);
		} else {
			return String.valueOf(id);
		}
	}
	
	public static String getHasNotWornItem(int id) {
		return lookupItemName(id);
		/*
		if (hasNotWornItemList.containsKey(id)) {
			return hasNotWornItemList.get(id);
		} else {
			return String.valueOf(id);
		}
		*/
	}

	public static String lookupItemName(int id) {
    	String xml = null;
        Document doc = null;
        
        String name = "";

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(String.format("http://itemxml.xyphos.com/?id=%s", id));
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
            Pattern pattern = Pattern.compile("<description>(.*?)</description>");
            Matcher matcher = pattern.matcher(xml);
            
	        while(matcher.find()) {
	        	xml = xml.replace(matcher.group(1), matcher.group(1).replace("<", "&lt;").replace(">", "&gt;"));
	        }	        
        	
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            
            try {
                DocumentBuilder db = dbf.newDocumentBuilder();
     
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xml));
                doc = db.parse(is); 
            } catch (ParserConfigurationException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            } catch (SAXException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            } catch (IOException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            }
        }
        
        if (doc != null) {
        	NodeList nl = doc.getElementsByTagName("item");
            
            for (int i = 0; i < nl.getLength(); i++) {
                Element e = (Element) nl.item(i);
                name = getValue(e, "name");
            }
        }
        
        if (name.equals("")) {
        	return String.valueOf(id);
        } else {
        	return "<a href=\"itemref://" + id + "/0/0\">" + name + "</a>";
        }
	}
	
    private static String getValue(Element item, String str) {
    	NodeList n = item.getElementsByTagName(str);
    	return getElementValue(n.item(0));
    }

    private static final String getElementValue( Node elem ) {
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
    
    public static String getNano(int id) {
        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), 0);
        HttpResponse response = null;
        
        JSONObject json = new JSONObject();
        
        try {
            HttpPost post = new HttpPost(String.format("http://109.74.0.178/nano.php?id=%d", id));
            post.setHeader("Content-Type", "application/json; charset=utf-8");
            
            json.put("platformType", "android");
			
            StringEntity se = new StringEntity(json.toString());  
            
            post.setEntity(se);
            response = client.execute(post);
        } catch(Exception e){
            e.printStackTrace();
        }
        
        if (response != null) {
            InputStream in;
            
			try {
				in = response.getEntity().getContent();
				String result = RestClient.convertStreamToString(in);
				                
    			if (result != null) {
    				JSONObject jsondata = new JSONObject(result);
                    
                    if(jsondata != null) {
                    	if (!jsondata.isNull("name")) {
                    		return jsondata.getString("name");
                    	}
                    }
    			}
			} catch (IllegalStateException e) {
	            e.printStackTrace();
			} catch (IOException e) {
	            e.printStackTrace();
			} catch (JSONException e) {
	            e.printStackTrace();
			}
        }
        
    	return String.valueOf(id);
    }
}
