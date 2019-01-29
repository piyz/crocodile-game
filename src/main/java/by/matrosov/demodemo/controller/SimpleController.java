package by.matrosov.demodemo.controller;

import by.matrosov.demodemo.exception.UserExistException;
import by.matrosov.demodemo.model.Room;
import by.matrosov.demodemo.model.User;
import by.matrosov.demodemo.service.rooms.RoomService;
import by.matrosov.demodemo.service.user.UserService;
import by.matrosov.demodemo.validation.UserValidator;
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
import java.util.List;

@Controller
public class SimpleController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserValidator userValidator;

    @RequestMapping(value = {"/", "/login"}, method = RequestMethod.GET)
    public String index(){
        return "index";
    }

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String hello(Model model, Principal principal){
        String username = principal.getName();

        if (username == null || username.isEmpty()){
            return "redirect:/login";
        }

        List<Room> rooms = roomService.getRooms();
        model.addAttribute("listRooms", rooms);
        model.addAttribute("username", username);

        return "home";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String registration(Model model){
        model.addAttribute("user", new User());
        return "registration";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String registerUserAccount(@ModelAttribute("user") User user, BindingResult result){

        userValidator.validate(user, result);
        if (result.hasErrors()){
            return "registration";
        }

        userService.save(user);
        return "success-register";
    }
}