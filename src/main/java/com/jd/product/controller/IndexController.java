package com.jd.product.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author chenYongJin
 * @Date 2021-05-17
 */
@Controller
public class IndexController {

    @RequestMapping("/")
    public String get() {
        return "download";
    }
}
