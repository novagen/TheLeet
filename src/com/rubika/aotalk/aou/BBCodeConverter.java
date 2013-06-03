package com.rubika.aotalk.aou;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rubika.aotalk.util.Logging;

public class BBCodeConverter {
	private static final String APP_TAG = "--> The Leet :: BBCodeConverter";

    public static String convertString(String bbCodeString)
    {
        //bbCodeString = TextUtils.htmlEncode(bbCodeString);
        for(BBCodeTag tag : BBCodeTag.values())
        {
            bbCodeString = tag.convert(bbCodeString);
        }
        return bbCodeString;
    }

    private enum BBCodeTag
    {
        ANCHOR("\\[anchor=(.*?)\\]","<span name=\"anchor_$1\"></span>", null),
        GOTO("\\[goto=(.*?)\\](.*?)\\[/goto\\]","<a href=\"#anchor_$1\">$2</a>", "[/goto]"),
        B("\\[b\\](.*?)\\[/b\\]", "<strong>$1</strong>", "[/b]"),
        I("\\[i\\](.*?)\\[/i\\]", "<italic>$1</italic>", "[/i]"),
        U("\\[u\\](.*?)\\[/u\\]", "<u>$1</u>", "[/u]"),
        NEWLINE("\n", "<br/>", null),
        IMG("\\[img\\](.*?)\\[/img\\]", "<img src=\"http://www.ao-universe.com/$1\" />", "[/img]"),
        URL("\\[url=(.*?)\\](.*?)\\[/url\\]", "<a href=\"http://www.ao-universe.com/$1\">$2</a>", "[/url]"),
        BR("\\[br\\]","<br />", null),
        ITEM("\\[item\\](.*?)\\[/item\\]","<a href=\"itemref://$1/0/0\">$1</a>", "[/item]"),
        ITEMNAME("\\[itemname\\](.*?)\\[/itemname\\]","<a href=\"itemref://$1/0/0\" class=\"nameonly\">$1</a>", "[/itemname]"),
        ITEMICON("\\[itemicon\\](.*?)\\[/itemicon\\]","<a href=\"itemref://$1/0/0\" class=\"icononly\">$1</a>", "[/itemicon]"),
        UL("\\[ul\\](.*?)\\[/ul\\]","<ul>$1</ul>", "[/ul]"),
        OL("\\[ol\\](.*?)\\[/ol\\]","<ol>$1</ol>", "[/ol]"),
        LI("\\[li\\](.*?)\\[/li\\]","<li>$1</li>", "[/li]"),
        CENTER("\\[center\\](.*?)\\[/center\\]","<center>$1</center>", "[/center]"),
        CENTER2("\\[center\\]","", null),
        CENTER3("\\[/center\\]","", null),
        SIZE("\\[size=(.*?)\\](.*?)\\[/size\\]","<font size=\"$1%\">$2</font>", "[/size]"),
        COLOR("\\[color=(.*?)\\](.*?)\\[/color\\]","<font color=\"$1\">$2</font>", "[/color]"),
        TABLE("\\[ct(.*?)\\](.*?)\\[/ct\\]","<table>$2</table>", "[/ct]"),
        TR("\\[cttr(.*?)\\](.*?)\\[/cttr\\]","<tr>$2</tr>", "[/cttr]"),
        TD("\\[cttd(.*?)\\](.*?)\\[/cttd\\]","<td>$2</td>", "[/cttd]"),
        TR2("\\[cttr(.*?)\\]","", null),
        TD2("\\[cttd(.*?)\\]","", null),
        TS_T("\\[ts_t(.*?)\\](.*?)\\[/ts_t\\]","$2", "[/ts_t]"),
        TS_TS("\\[ts_ts\\]"," + ", null),
        TS_TS2("\\[ts_ts2\\]"," = ", null),
        FONT_SIZE("<font size=\"(.*?)%\">(.*?)</font>","<font size=\"$1px\">$2</font>", null)
        ;
        private String tagPattern;
        private String htmlConversion;
        private String lookFor;

        private BBCodeTag(String tagPattern, String htmlConversion, String lookFor) {
            this.htmlConversion = htmlConversion;
            this.tagPattern = tagPattern;
            this.lookFor = lookFor;
        }

        public String convert(String bbCodeTag)
        {
			Logging.log(APP_TAG, "Converting tag " + tagPattern);
            
            String replaced = bbCodeTag;
            
        	Pattern pattern = Pattern.compile(tagPattern, Pattern.DOTALL);
			Logging.log(APP_TAG, "pattern compiled");
           
            Matcher matcher = pattern.matcher(bbCodeTag);
			Logging.log(APP_TAG, "matcher done");
            
            
            if (lookFor != null) {
            	if (bbCodeTag.contains(lookFor)) {
                    replaced = matcher.replaceAll(htmlConversion);
    				Logging.log(APP_TAG, "replace done");
            	}
            } else {
                replaced = matcher.replaceAll(htmlConversion);
				Logging.log(APP_TAG, "replace done");
            }
            
            return replaced;
        }
    }

}
