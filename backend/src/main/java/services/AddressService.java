package services;


import models.Address;
import org.springframework.beans.factory.annotation.Autowired;
import repository.AddressRepository;

import java.util.ArrayList;
import java.util.List;

public class AddressService {
    @Autowired
    private AddressRepository addressRepository;

    public void addAddress(Address address){
        addressRepository.save(address);
    }

    public void updateAddress(Address address){
        addressRepository.save(address);
    }

    public void getAddress(Long id){
        addressRepository.findOne(id);
    }

    public void deleteAddress(Long id){
        addressRepository.delete(id);
    }

    public List<Address> getAllAddresses(){
        List<Address> addresses = new ArrayList<>();
        addressRepository.findAll().forEach(addresses::add);
        return  addresses;
    }

}
