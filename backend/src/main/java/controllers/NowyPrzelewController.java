package controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by MICHALLL on 23.04.2017.
 */
@Controller
public class NowyPrzelewController {
    @RequestMapping(value = "/nowyprzelew", method = RequestMethod.GET)
    public String showPage() {
        return "nowy przelew";
    }
}
