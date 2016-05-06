package com.example.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("population")
public class PopulationController {
	//@Autowired
	//BookmarkService bookmarkService;

	final static Map<String, String> prefectures = Collections.unmodifiableMap(
			new LinkedHashMap<String, String>() {
		{
			put("北海道","01");
			put("青森県","02");
			put("岩手県","03");
			put("宮城県","04");
			put("秋田県","05");
			put("山形県","06");
			put("福島県","07");
			put("茨城県","08");
			put("栃木県","09");
			put("群馬県","10");
			put("埼玉県","11");
			put("千葉県","12");
			put("東京都","13");
			put("神奈川県","14");
			put("新潟県","15");
			put("富山県","16");
			put("石川県","17");
			put("福井県","18");
			put("山梨県","19");
			put("長野県","20");
			put("岐阜県","21");
			put("静岡県","22");
			put("愛知県","23");
			put("三重県","24");
			put("滋賀県","25");
			put("京都府","26");
			put("大阪府","27");
			put("兵庫県","28");
			put("奈良県","29");
			put("和歌山県","30");
			put("鳥取県","31");
			put("島根県","32");
			put("岡山県","33");
			put("広島県","34");
			put("山口県","35");
			put("徳島県","36");
			put("香川県","37");
			put("愛媛県","38");
			put("高知県","39");
			put("福岡県","40");
			put("佐賀県","41");
			put("長崎県","42");
			put("熊本県","43");
			put("大分県","44");
			put("宮崎県","45");
			put("鹿児島県","46");
			put("沖縄県","47");
		}
	});
		
	@RequestMapping(value = {"", "/", "index"}, method = RequestMethod.GET)
	String getIndex(Model model){
		model.addAttribute("prefectures", prefectures);
		return "population/index";
	}
}
