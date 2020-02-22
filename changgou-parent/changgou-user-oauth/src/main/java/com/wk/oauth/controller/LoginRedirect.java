package com.wk.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("oauth")
public class LoginRedirect {

    /**
     * 跳转到登录页面
     * @return
     */
    @GetMapping("login")
    public String login(@RequestParam(value = "from",required = false,defaultValue = "")String from, Model model){
        //将用户在登录前要访问的页面存储到Model中
        model.addAttribute("from",from);
        return "login";
    }

}
