package com.bankbitcoinow.services;


import com.bankbitcoinow.models.Address;
import com.bankbitcoinow.models.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bankbitcoinow.repository.AddressRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    public Address addAddress(Address address){
        return addressRepository.save(address);
    }

    public void updateAddress(Address address){
        addressRepository.save(address);
    }

    public Address getAddress(Long id){
        return addressRepository.findOne(id);
    }

    public Address findByAddress(String address) {
        return addressRepository.findByAddress(address);
    }

    public void deleteAddress(Long id){
        addressRepository.delete(id);
    }

    public List<Address> getAllAddresses(){
        List<Address> addresses = new ArrayList<>();
        addressRepository.findAll().forEach(addresses::add);
        return  addresses;
    }

    public List<Address> getUserAddresses(Long user_id){
        List<Address> userAddresses = new ArrayList<>();

        List<Address> allAddresses = getAllAddresses();
        for(Address address : allAddresses){
            if (address.getUser().getId().equals(user_id)){    /// ==   (?)
                userAddresses.add(address);
            }
        }

        return userAddresses;
    }

}
