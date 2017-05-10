package com.bankbitcoinow.controllers;

import com.bankbitcoinow.bitcoinj.BitcoinjFacade;
import com.bankbitcoinow.bitcoinj.EncryptedKey;
import com.bankbitcoinow.models.Address;
import com.bankbitcoinow.models.User;
import com.bankbitcoinow.services.UserService;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.bankbitcoinow.services.AddressService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;

@RestController
public class AddressController {

    private static final Logger LOG = LoggerFactory.getLogger(AddressController.class);

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserService userService;

    @Autowired
    private BitcoinjFacade bitcoinjFacade;

    @Autowired
    private NetworkParameters networkParameters;

    @RequestMapping(method = RequestMethod.POST, value="/dodaj_adres")
    public void doTransaction(@RequestBody Address address) {
        addressService.addAddress(address);

    }

    @RequestMapping(method = RequestMethod.POST, value="/generuj_adres")
    public Address generateAddress(@RequestBody Map<String, String> input) throws IOException {
        String email = input.get("email");
        String password = input.get("password");

        Assert.hasText(email, "Email cannot be empty");
        Assert.hasText(password, "Password cannot be empty");

        User user = userService.findByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("User " + email + " was not found");
        }

        EncryptedKey encryptedKey = bitcoinjFacade.generateNewKey(password);

        Address address = new Address();
        address.setUser(user);
        address.setAddress(encryptedKey.getAddress(networkParameters));
        address.setPrivateKey(encryptedKey.toByteArray());
        address.setBalance(new BigDecimal(0));
        address.setCreated_at(new Timestamp(System.currentTimeMillis()));
        address = addressService.addAddress(address);

        LOG.info("Address created. ID: {}", address.getId());

        return address;
    }
}
