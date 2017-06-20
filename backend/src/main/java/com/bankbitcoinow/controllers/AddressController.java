package com.bankbitcoinow.controllers;

import com.bankbitcoinow.bitcoinj.BitcoinjFacade;
import com.bankbitcoinow.bitcoinj.EncryptedKey;
import com.bankbitcoinow.models.Address;
import com.bankbitcoinow.models.User;
import com.bankbitcoinow.services.TransactionService;
import com.bankbitcoinow.services.UserService;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import com.bankbitcoinow.services.AddressService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@RestController
public class AddressController {

    private static final Logger LOG = LoggerFactory.getLogger(AddressController.class);

    @Autowired
    private AddressService addressService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @Autowired
    private BitcoinjFacade bitcoinjFacade;

    @Autowired
    private NetworkParameters networkParameters;


    @RequestMapping(method = RequestMethod.POST, value="/deleteWallet")
    public void deleteAddress(@RequestBody Map<String, String> input) {

        Long id = Long.valueOf(input.get("id"));

        //TODO sprawdzenie hasła użytkownika...
//        String password = input.get("password");
        addressService.deleteAddress(id);
    }


    private void updateAddressesBalances(List<Address> userAddresses) {
        for(Address address : userAddresses){
            double addressBalance = transactionService.getAddressBalance(address.getId());
            address.setBalance(new BigDecimal(addressBalance));
            addressService.updateAddress(address);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value="/getUserAddresses")
    public @ResponseBody List<Address> getUserAddresses(@RequestBody Map<String, String> input) {

        Long user_id = userService.findByEmail(input.get("email")).getId();
        List<Address> userAddresses = addressService.getUserAddresses(user_id);

        updateAddressesBalances(userAddresses);
        return userAddresses;
    }

    @RequestMapping(method = RequestMethod.POST, value="/createWallet")
    public Address generateAddress(@RequestBody Map<String, String> input) throws IOException {
        String alias = input.get("alias");
        String email = input.get("email");
        String password = input.get("password");

        Assert.hasText(alias, "Alias cannot be empty");
//        Assert.hasText(email, "Email cannot be empty");
        Assert.hasText(password, "Password cannot be empty");

        User user = userService.findByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("User " + email + " was not found");
        }

        EncryptedKey encryptedKey = bitcoinjFacade.generateNewKey(password);

        Address address = new Address();
        address.setAlias(alias);
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
