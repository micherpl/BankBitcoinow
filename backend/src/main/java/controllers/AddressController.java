package controllers;

import models.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import services.AddressService;

@RestController
public class AddressController {
    @Autowired
    private AddressService addressService;

    @RequestMapping(method = RequestMethod.POST, value="/dodaj_adres")
    public void doTransaction(@RequestBody Address address) {
        addressService.addAddress(address);

    }
}
