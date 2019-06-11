package com.jk.dartdemo;

import com.jk.dartdemo.entity.Menu;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class MenuController {

    private static final String[] BREAKFAST_NAME = {"南瓜稀饭", "奶油馒头", "白鸡蛋", "牛奶", "八宝粥",
            "肉末花卷", "大饼", "卤鸡蛋", "五香鸡蛋", "香辣黄瓜丁"};
    private static final String[] BREAKFAST_URL = {"ngxf.jpg", "nymt.jpg", "bjd.jpg", "nn.jpg", "bbz.jpg",
            "rmhj.jpg", "db.jpg", "rjd.jpg", "wxjd.jpg", "xlhgd.jpg"};

    @GetMapping("/todayMenu")
    @ResponseBody
    public List<Menu> todayMenu(@RequestParam(value = "type", defaultValue = "1") String type) {
        if (!"1".equals(type)) {
            throw new RuntimeException("22");
        }
        int size = 10;
        List<Menu> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new Menu("http://192.168.1.106:8080/menu/" + BREAKFAST_URL[i], BREAKFAST_NAME[i]));
        }
        return list;
    }
}
