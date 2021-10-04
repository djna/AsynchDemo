package org.djna.asynch.estate.webapp;


import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeMonitorController {
    static private Logger LOGGER = Logger.getLogger(HomeMonitorController.class);
    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        return "greeting";
    }

    @GetMapping("/monitor")
    public String monitor(
            @RequestParam(name="property", required=false, defaultValue="101") String property,
            @RequestParam(name="locationSelected", required=false, defaultValue="hall") String location,
            Model model) {
        LOGGER.info("monitor " + property + "/" + location);
        model.addAttribute("property", property);
        model.addAttribute("location", location);
        model.addAttribute("topic", "home/thermostats/" + property + "/" + location);
        return "monitor";
    }

    @GetMapping("/property")
    public String home(@RequestParam(name="property", required=false, defaultValue="101") String property, Model model) {
        model.addAttribute("property", property);
        // list of locations for this property could come fro Estate database
        String [] locations = { "hall", "basement"};
        model.addAttribute("locations", locations);
        return "property";
    }
}
