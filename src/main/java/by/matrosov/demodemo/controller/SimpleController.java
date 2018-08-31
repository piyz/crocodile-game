package by.matrosov.demodemo.controller;

import by.matrosov.demodemo.exception.UserExistException;
import by.matrosov.demodemo.model.User;
import by.matrosov.demodemo.service.UserService;
import by.matrosov.demodemo.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.security.Principal;

@Controller
public class SimpleController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = {"/", "/login"}, method = RequestMethod.GET)
    public String index(){
        return "index";
    }

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String hello(Model model, Principal principal){
        model.addAttribute("hi", "You are logged in as " + principal.getName());
        return "home";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String registration(Model model){
        User user = new User();
        model.addAttribute("user", user);
        return "registration";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public ModelAndView registerUserAccount(@ModelAttribute("user") @Valid User user, BindingResult result,
                                            Errors errors){
        User registered = new User();

        if (!result.hasErrors()){
            registered = createUserAccount(user, result);
        }
        if (registered == null){
            result.rejectValue("username", "message.regError");
        }
        if (result.hasErrors()){
            return new ModelAndView("registration", "user", user);
        }else {
            return new ModelAndView("successRegister", "user", user);
        }
    }

    private User createUserAccount(User user, BindingResult result){
        User registered = null;
        try{
            registered = userService.registerNewUserAccount(user);
        }catch (UserExistException e){
            return null;
        }
        return registered;
    }
}