package by.matrosov.demodemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;

@Controller
public class SimpleController {

    @RequestMapping(value = {"/", "/login"}, method = RequestMethod.GET)
    public String index(){
        return "index";
    }

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String hello(Model model, Principal principal){
        model.addAttribute("hi", "You are logged in as " + principal.getName());
        return "home";
    }
}